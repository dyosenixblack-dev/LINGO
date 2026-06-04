package com.example.data.api

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.HttpException
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import org.json.JSONObject

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun translateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    @Volatile
    private var appPackageName: String? = null

    @Volatile
    private var appCertFingerprint: String? = null

    fun initialize(context: android.content.Context) {
        if (appPackageName != null) return // Already initialized
        
        appPackageName = context.packageName
        try {
            val pm = context.packageManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val packageInfo = pm.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
                )
                val signingInfo = packageInfo.signingInfo
                val signatures = if (signingInfo != null) {
                    if (signingInfo.hasMultipleSigners()) {
                        signingInfo.apkContentsSigners
                    } else {
                        signingInfo.signingCertificateHistory
                    }
                } else {
                    null
                }
                val signature = signatures?.firstOrNull()
                if (signature != null) {
                    val md = java.security.MessageDigest.getInstance("SHA-1")
                    val publicKey = md.digest(signature.toByteArray())
                    appCertFingerprint = publicKey.joinToString(":") { String.format("%02X", it) }
                }
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = pm.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNATURES
                )
                @Suppress("DEPRECATION")
                val signature = packageInfo.signatures?.firstOrNull()
                if (signature != null) {
                    val md = java.security.MessageDigest.getInstance("SHA-1")
                    val publicKey = md.digest(signature.toByteArray())
                    appCertFingerprint = publicKey.joinToString(":") { String.format("%02X", it) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()

            val pkg = appPackageName
            val cert = appCertFingerprint

            if (pkg != null) {
                requestBuilder.header("X-Android-Package", pkg)
            }
            if (cert != null) {
                requestBuilder.header("X-Android-Cert", cert)
            }

            chain.proceed(requestBuilder.build())
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val apiService: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    private fun parseHttpError(e: HttpException, stepDesc: String): String {
        val code = e.code()
        val rawBody = try {
            e.response()?.errorBody()?.string()
        } catch (ex: Exception) {
            null
        }

        if (rawBody.isNullOrBlank()) {
            return "فشل $stepDesc: خطأ في الاتصال بالخادم (HTTP $code)"
        }

        return try {
            val root = JSONObject(rawBody)
            val errorObj = root.optJSONObject("error")
            val message = errorObj?.optString("message") ?: ""
            val status = errorObj?.optString("status") ?: ""

            var localizedMessage = when {
                code == 403 -> {
                    if (message.contains("API key restricted", ignoreCase = true) || message.contains("restriction", ignoreCase = true)) {
                        "تم رفض الطلب (HTTP 403): مفتاح API الخاص بك مقيّد بتطبيق أو باقة معينة. يرجى تعديل قيود المفتاح في لوحة تحكم Google Cloud Console أو التأكد من إرفاق حزم Android بشكل سليم."
                    } else if (message.contains("location", ignoreCase = true) || message.contains("geographically", ignoreCase = true) || message.contains("region", ignoreCase = true)) {
                        "تم رفض الطلب (HTTP 403): خدمة الذكاء الاصطناعي (Gemini) غير متاحة جغرافياً في منطقتك الحالية حالياً بدون استخدام proxy/VPN أو تهيئة مخصصة."
                    } else {
                        "تم رفض الطلب المالي/الخاص بالخادم (HTTP 403): يرجى تفعيل واجهة Generative Language API ومراجعة إعدادات صلاحية مفتاح الـ API الخاص بك."
                    }
                }
                code == 400 -> "خطأ في بنية الطلب (HTTP 400). يرجى التأكد من الموديل وصيغة البيانات."
                code == 404 -> "العنصر أو الموديل المستهدف غير موجود على هذا الخادم (HTTP 404)."
                code == 429 -> "تجاوزت حد معدل الاستهلاك المسموح به مجاناً (HTTP 429). الرجاء الانتظار لبضع ثوانٍ ثم إعادة المحاولة."
                else -> message.ifBlank { "رمز الخطأ: $status" }
            }

            if (localizedMessage.isNotBlank()) {
                "فشل $stepDesc: $localizedMessage"
            } else {
                "فشل $stepDesc (HTTP $code): $message"
            }
        } catch (ex: Exception) {
            "فشل $stepDesc (HTTP $code): $rawBody"
        }
    }

    suspend fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return "ملاحظة: مفتاح API غير مهيأ. يرجى وضعه في لوحة Secrets في AI Studio لاستخدام الترجمة الحية بالذكاء الاصطناعي."
        }

        val prompt = """
            أنت مترجم سياحي ذكي فائق الاحترافية. قم بترجمة النص التالي من لغة ($sourceLanguage) إلى لغة ($targetLanguage).
            
            النص الأصلي:
            $text
            
            الشروط:
            1. ترجم بدقة وبأسلوب طبيعي ومفهوم للمسافرين والسياح.
            2. لا تضف أي تعليقات أو نصوص خارجية، فقط أرسل النص المترجم مباشرة ومصقول بالكامل.
            3. إذا كان هناك مصطلح هام محلي، يمكنك توضيحه.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.3f),
            systemInstruction = Content(parts = listOf(Part(text = "أنت مترجم سياحي متخصص يترجم بدقة ويرسل النتيجة المترجمة مباشرة دون أي كلام جانبي أو مقدمات.")))
        )

        return try {
            val response = apiService.translateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: "عذراً، لم نتمكن من الحصول على رد من خادم الترجمة."
        } catch (e: HttpException) {
            parseHttpError(e, "ترجمة النص")
        } catch (e: Exception) {
            "فشل في الاتصال بخادم الترجمة: ${e.localizedMessage}"
        }
    }

    suspend fun translateVoice(
        phraseText: String,
        sourceLanguage: String,
        targetLanguage: String
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return "ملاحظة: مفتاح API غير مهيأ. يرجى وضعه لتفعيل ترجمة الصوت الفورية بذكاء اصطناعي."
        }

        val prompt = """
            أنت مترجم سياحي صوتي فوري ومحترف. لقد نطق المسافر بهذه العبارة باللغة ($sourceLanguage):
            "$phraseText"
            
            قم بترجمتها إلى لغة ($targetLanguage) صياغةً تناسب النطق الصوتي للمحادثات اليومية (تسهيل التعبيرات الشفهية، سهلة الفهم والسماع لمتحدث لغة الهدف).
            أرسل الترجمة الصوتية مكتوبة مباشرة وواضحة فقط دون مقدمات أو شرح طويل.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.5f)
        )

        return try {
            val response = apiService.translateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: "عذراً، لم نحصل على ترجمة صوتية."
        } catch (e: HttpException) {
            parseHttpError(e, "ترجمة الصوت")
        } catch (e: Exception) {
            "فشل في الاتصال بمترجم الصوت: ${e.localizedMessage}"
        }
    }

    suspend fun translateImage(
        bitmap: Bitmap,
        targetLanguage: String
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return "ملاحظة: مفتاح API غير مهيأ. يرجى تهيئته لترجمة لافتات السفر والصور."
        }

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
        val byteArray = stream.toByteArray()
        val base64Data = Base64.encodeToString(byteArray, Base64.NO_WRAP)

        val prompt = """
            أنت مترجم صور ولافتات سياحية تعمل بالذكاء الاصطناعي والموديلات المتعددة.
            افحص هذه اللقطة المصورة بعناية:
            1. اكتشف أي نصوص أو لافتات أو إرشادات أو شاشات أو قوائم طعام فيها.
            2. ترجم النصوص المستخلصة بدقة عالية إلى اللغة الهدف المحددة: $targetLanguage.
            3. اعرض النتيجة بأسلوب سياحي إرشادي منسق وواضح لمساعدة السائح على حل لغز الصورة وفهم موقعه تماماً.
            مثال للنتيجة:
            🔖 النص المكتوب المكتشف: [النص الأصلي من اللافتة]
            🗺️ معنى الترجمة: [الترجمة والشرح باللغة العربية بوضوح واحترافية]
            💡 نصيحة للسائح: [إرشاد سريع ومفيد يتعلق بمحتوى اللافتة]
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Data))
                    )
                )
            ),
            generationConfig = GenerationConfig(temperature = 0.3f)
        )

        return try {
            val response = apiService.translateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: "لم نستطع ترجمة الصورة تلقائياً."
        } catch (e: HttpException) {
            parseHttpError(e, "ترجمة الصورة")
        } catch (e: Exception) {
            "فشل في معالجة وترجمة الصورة: ${e.localizedMessage}"
        }
    }
}
