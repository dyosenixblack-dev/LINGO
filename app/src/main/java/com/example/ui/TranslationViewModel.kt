package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.SubscriptionManager
import com.example.data.SubscriptionType
import com.example.data.api.GeminiClient
import com.example.data.db.AppDatabase
import com.example.data.db.TranslationHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppTab {
    TRANSLATOR,
    HISTORY,
    PLANS,
    SETTINGS
}

enum class TranslatorSubTab {
    TEXT,
    VOICE,
    CAMERA
}

class TranslationViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val historyDao = db.historyDao()
    private val subscriptionManager = SubscriptionManager(application)

    // UI Tab states
    private val _currentTab = MutableStateFlow(AppTab.TRANSLATOR)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _currentSubTab = MutableStateFlow(TranslatorSubTab.TEXT)
    val currentSubTab: StateFlow<TranslatorSubTab> = _currentSubTab.asStateFlow()

    // Translation Form Flow
    val sourceText = MutableStateFlow("")
    val sourceLang = MutableStateFlow("العربية")
    val targetLang = MutableStateFlow("الإنجليزية")

    private val _translationResult = MutableStateFlow("")
    val translationResult: StateFlow<String> = _translationResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Voice Translator states
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    val voiceTextPhrase = MutableStateFlow("")
    private val _voiceTranslationResult = MutableStateFlow("")
    val voiceTranslationResult: StateFlow<String> = _voiceTranslationResult.asStateFlow()

    // Camera/Image Translator states
    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap.asStateFlow()

    private val _imageTranslationResult = MutableStateFlow("")
    val imageTranslationResult: StateFlow<String> = _imageTranslationResult.asStateFlow()

    // Limits & Subscription states
    private val _subscriptionType = MutableStateFlow(subscriptionManager.getSubscriptionType())
    val subscriptionType: StateFlow<SubscriptionType> = _subscriptionType.asStateFlow()

    private val _remainingImages = MutableStateFlow(subscriptionManager.getRemainingImages())
    val remainingImages: StateFlow<Int> = _remainingImages.asStateFlow()

    private val _remainingVoice = MutableStateFlow(subscriptionManager.getRemainingVoice())
    val remainingVoice: StateFlow<Int> = _remainingVoice.asStateFlow()

    private val _subscriptionExpiry = MutableStateFlow(subscriptionManager.getSubscriptionExpiry())
    val subscriptionExpiry: StateFlow<String> = _subscriptionExpiry.asStateFlow()

    private val _isSubscriptionExpired = MutableStateFlow(subscriptionManager.isSubscriptionExpired())
    val isSubscriptionExpired: StateFlow<Boolean> = _isSubscriptionExpired.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Text & Display zoom scaling states
    private val _textScaleIndex = MutableStateFlow(subscriptionManager.getTextScaleIndex())
    val textScaleIndex: StateFlow<Int> = _textScaleIndex.asStateFlow()

    private val _textScaleMultiplier = MutableStateFlow(subscriptionManager.getTextScaleMultiplier())
    val textScaleMultiplier: StateFlow<Float> = _textScaleMultiplier.asStateFlow()

    // Dark Mode settings
    val isDarkMode = MutableStateFlow(true) // Start with premium dark mode by default!

    // App Language settings
    private val _appLanguage = MutableStateFlow(subscriptionManager.getAppLanguage())
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    // Database History
    val historyList: StateFlow<List<TranslationHistory>> = historyDao.getAllHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        GeminiClient.initialize(application)
        AppLocalization.currentLanguageCode = subscriptionManager.getAppLanguage()
        updateLimits()
    }

    fun setAppLanguage(lang: String) {
        subscriptionManager.setAppLanguage(lang)
        _appLanguage.value = lang
        AppLocalization.currentLanguageCode = lang
        val feedback = when (lang) {
            "en" -> "App language changed to English! 🌐"
            "zh" -> "应用语言已更改为中文！ 🌐"
            else -> "تم تغيير لغة التطبيق إلى العربية بنجاح! 🌐"
        }
        showToast(feedback)
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun selectSubTab(subTab: TranslatorSubTab) {
        _currentSubTab.value = subTab
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun updateLimits() {
        _subscriptionType.value = subscriptionManager.getSubscriptionType()
        _remainingImages.value = subscriptionManager.getRemainingImages()
        _remainingVoice.value = subscriptionManager.getRemainingVoice()
        _subscriptionExpiry.value = subscriptionManager.getSubscriptionExpiry()
        _isSubscriptionExpired.value = subscriptionManager.isSubscriptionExpired()
        _textScaleIndex.value = subscriptionManager.getTextScaleIndex()
        _textScaleMultiplier.value = subscriptionManager.getTextScaleMultiplier()
    }

    fun setTextScaleIndex(index: Int) {
        subscriptionManager.setTextScaleIndex(index)
        _textScaleIndex.value = index
        _textScaleMultiplier.value = subscriptionManager.getTextScaleMultiplier()
        _toastMessage.value = when (AppLocalization.currentLanguageCode) {
            "en" -> "View scaling changed successfully! 🔬"
            "zh" -> "视图缩放比例更改成功！🔬"
            else -> "تم تغيير تكبير وحجم العرض بنجاح! 🔬"
        }
    }

    // Subscribe/Upgrade action
    fun upgradeSubscription(type: SubscriptionType) {
        subscriptionManager.setSubscriptionType(type)
        updateLimits()
        val title = type.getTitle(AppLocalization.currentLanguageCode)
        _toastMessage.value = when (AppLocalization.currentLanguageCode) {
            "en" -> "Successfully upgraded to $title!"
            "zh" -> "成功升级至 $title！"
            else -> "تمت الترقية بنجاح إلى ${type.titleAr}!"
        }
    }

    // Purchase subscription via specified payment method
    fun purchaseSubscription(type: SubscriptionType, paymentMethod: String, priceStr: String) {
        subscriptionManager.renewSubscription(type)
        updateLimits()
        val title = type.getTitle(AppLocalization.currentLanguageCode)
        _toastMessage.value = when (AppLocalization.currentLanguageCode) {
            "en" -> "Activated $title ($priceStr) via $paymentMethod successfully! Valid for 30 days."
            "zh" -> "已通过 $paymentMethod 成功激活 $title ($priceStr)！30天内有效。"
            else -> "تم تفعيل ${type.titleAr} ($priceStr) عبر $paymentMethod بنجاح! تم تجديد الصلاحية لمدة 30 يوماً."
        }
    }

    // Trigger instant mock subscription expiration and send a real local notification alert
    fun simulateSubscriptionExpiration() {
        val currentType = subscriptionManager.getSubscriptionType()
        if (currentType == SubscriptionType.FREE) {
            _toastMessage.value = when (AppLocalization.currentLanguageCode) {
                "en" -> "You are currently on the Free tier. Please subscribe/upgrade first!"
                "zh" -> "您当前使用的是免费套餐。请先订阅或升级！"
                else -> "أنت تستخدم الباقة المجانية حالياً. يرجى الاشتراك أو الترقية أولاً ثم تجربة محاكاة التنبيه!"
            }
            return
        }

        val packageName = currentType.getTitle(AppLocalization.currentLanguageCode)
        subscriptionManager.setSubscriptionExpiredState(true)
        updateLimits()

        // Trigger native notification
        sendExpiryNotification(getApplication(), currentType)
        _toastMessage.value = when (AppLocalization.currentLanguageCode) {
            "en" -> "Simulated expiration of $packageName & sent notification! 🔔"
            "zh" -> "已模拟套餐 $packageName 到期并发送通知！🔔"
            else -> "تمت محاكاة انتهاء صلاحية باقة $packageName بنجاح وإرسال تنبيه فوري بالنظام! 🔔"
        }
    }

    // Sending native alarm/expiration notification
    private fun sendExpiryNotification(context: android.content.Context, currentType: SubscriptionType) {
        val channelId = "subscription_notifications"
        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        val nameEn = currentType.getTitle("en")
        val nameZh = currentType.getTitle("zh")
        val nameAr = currentType.titleAr

        val title = when (AppLocalization.currentLanguageCode) {
            "en" -> "Lingo subscription expired for $nameEn ⚠️"
            "zh" -> "Lingo 的 $nameZh 套餐已过期 ⚠️"
            else -> "انتهى اشتراك $nameAr لـ Lingo ⚠️"
        }

        val text = when (AppLocalization.currentLanguageCode) {
            "en" -> "Your monthly subscription expired today. Click here to renew and continue your trip!"
            "zh" -> "您的包月套餐今日已到期。点击此处立即续订以继续您的出行！"
            else -> "انتهت صلاحية باقتك الشهرية اليوم. اضغط هنا لتجديد باقتك فوراً ومتابعة السفر!"
        }

        val channelName = when (AppLocalization.currentLanguageCode) {
            "en" -> "Lingo Subscription Alerts"
            "zh" -> "Lingo 订阅警报"
            else -> "إشعارات باقات Lingo"
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                channelName,
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Lingo Subscription Channel"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            notificationManager.notify(1011, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 1. Text Translation Action
    fun performTextTranslation() {
        val text = sourceText.value.trim()
        if (text.isEmpty()) {
            _toastMessage.value = "الرجاء كتابة النص المراد ترجمته أولاً"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val src = sourceLang.value
            val dest = targetLang.value
            val result = withContext(Dispatchers.IO) {
                GeminiClient.translateText(text, src, dest)
            }
            _translationResult.value = result
            _isLoading.value = false

            // Save to history on success
            if (!result.startsWith("ملاحظة:") && !result.startsWith("فشل في الاتصال")) {
                withContext(Dispatchers.IO) {
                    historyDao.insertHistory(
                        TranslationHistory(
                            sourceText = text,
                            translatedText = result,
                            sourceLang = src,
                            targetLang = dest,
                            type = "text"
                        )
                    )
                }
            }
        }
    }

    // 2. Voice Translation Action
    fun startRecordingVoice() {
        _isRecording.value = true
    }

    fun stopRecordingAndTranslate(spokenLabel: String) {
        _isRecording.value = false
        voiceTextPhrase.value = spokenLabel

        if (!subscriptionManager.canTranslateVoice()) {
            _toastMessage.value = "لقد استهلكت حد ترجمة الرسائل الصوتية اليومي المتاح لباقتك الحالية! يرجى الترقية لزيادة الحد."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val src = sourceLang.value
            val dest = targetLang.value
            val result = withContext(Dispatchers.IO) {
                GeminiClient.translateVoice(spokenLabel, src, dest)
            }
            _voiceTranslationResult.value = result
            _isLoading.value = false

            // Decrement plan counter
            subscriptionManager.incrementVoiceCount()
            updateLimits()

            // Save to history
            if (!result.startsWith("ملاحظة:") && !result.startsWith("فشل في الاتصال")) {
                withContext(Dispatchers.IO) {
                    historyDao.insertHistory(
                        TranslationHistory(
                            sourceText = spokenLabel,
                            translatedText = result,
                            sourceLang = src,
                            targetLang = dest,
                            type = "voice"
                        )
                    )
                }
            }
        }
    }

    // 3. Image OCR Translation Action
    fun setImageAndTranslate(bitmap: Bitmap, imageNamePlaceholder: String) {
        _selectedBitmap.value = bitmap

        if (!subscriptionManager.canTranslateImage()) {
            _toastMessage.value = "لقد فرغت حدود ترجمة لافتات السفر المتاحة لك اليوم! يرجى الترقية إلى الباقة الممتازة لرفع الحد إلى 10 صور أو اللامحدودة."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val dest = targetLang.value
            val result = withContext(Dispatchers.IO) {
                GeminiClient.translateImage(bitmap, dest)
            }
            _imageTranslationResult.value = result
            _isLoading.value = false

            // Decrement card counts
            subscriptionManager.incrementImageCount()
            updateLimits()

            // Save to history
            if (!result.startsWith("ملاحظة:") && !result.startsWith("فشل في الاتصال")) {
                withContext(Dispatchers.IO) {
                    historyDao.insertHistory(
                        TranslationHistory(
                            sourceText = "صورة لافتة / $imageNamePlaceholder",
                            translatedText = result,
                            sourceLang = "صورة",
                            targetLang = dest,
                            type = "image"
                        )
                    )
                }
            }
        }
    }

    // Swap Source & Target languages
    fun swapLanguages() {
        val temp = sourceLang.value
        sourceLang.value = targetLang.value
        targetLang.value = temp
    }

    // Delete single history item
    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            historyDao.deleteHistoryById(id)
        }
    }

    // Clear all history item values
    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            historyDao.clearHistory()
        }
    }
}

class TranslationViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TranslationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TranslationViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
