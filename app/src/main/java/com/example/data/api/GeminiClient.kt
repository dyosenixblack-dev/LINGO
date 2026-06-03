package com.example.data.api

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun translateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val apiService: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
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
        } catch (e: Exception) {
            "فشل في معالجة وترجمة الصورة: ${e.localizedMessage}"
        }
    }
}
