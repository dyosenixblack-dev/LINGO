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

    // Dark Mode settings
    val isDarkMode = MutableStateFlow(true) // Start with premium dark mode by default!

    // Database History
    val historyList: StateFlow<List<TranslationHistory>> = historyDao.getAllHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        updateLimits()
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
    }

    // Subscribe/Upgrade action
    fun upgradeSubscription(type: SubscriptionType) {
        subscriptionManager.setSubscriptionType(type)
        updateLimits()
        _toastMessage.value = "تمت الترقية بنجاح إلى ${type.titleAr}!"
    }

    // Purchase subscription via specified payment method
    fun purchaseSubscription(type: SubscriptionType, paymentMethod: String, priceStr: String) {
        subscriptionManager.renewSubscription(type)
        updateLimits()
        _toastMessage.value = "تم تفعيل ${type.titleAr} ($priceStr) عبر $paymentMethod بنجاح! تم تجديد الصلاحية لمدة 30 يوماً."
    }

    // Trigger instant mock subscription expiration and send a real local notification alert
    fun simulateSubscriptionExpiration() {
        val currentType = subscriptionManager.getSubscriptionType()
        if (currentType == SubscriptionType.FREE) {
            _toastMessage.value = "أنت تستخدم الباقة المجانية حالياً. يرجى الاشتراك أو الترقية أولاً ثم تجربة محاكاة التنبيه!"
            return
        }

        val packageName = currentType.titleAr
        subscriptionManager.setSubscriptionExpiredState(true)
        updateLimits()

        // Trigger native notification
        sendExpiryNotification(getApplication(), packageName)
        _toastMessage.value = "تمت محاكاة انتهاء صلاحية باقة $packageName بنجاح وإرسال تنبيه فوري بالنظام! 🔔"
    }

    // Sending native alarm/expiration notification
    private fun sendExpiryNotification(context: android.content.Context, packageName: String) {
        val channelId = "subscription_notifications"
        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "إشعارات باقات TradiDour",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قنوات تنبيه لتجديد واشتراك باقات تطبيق TradiDour"
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
            .setContentTitle("انتهى اشتراك $packageName لـ TradiDour ⚠️")
            .setContentText("انتهت صلاحية باقتك الشهرية اليوم. اضغط هنا لتجديد باقتك فوراً ومتابعة السفر!")
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
