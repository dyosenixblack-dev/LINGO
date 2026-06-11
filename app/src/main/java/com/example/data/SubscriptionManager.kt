package com.example.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SubscriptionType(val titleAr: String, val maxImages: Int, val maxVoice: Int, val priceAr: String, val descriptionAr: String) {
    FREE("الباقة المجانية", 5, 3, "مجاناً", "ترجمة محدودة يومية للمسافر المبتدئ"),
    PREMIUM("الباقة الممتازة", 10, 5, "19.99$ / شهرياً", "ترقية ذكية تضاعف حدود الصور والصوت للرحلات المستمرة"),
    ULTRA("الباقة اللامحدودة", 99999, 99999, "39.99$ / شهرياً", "تنقل بلا قلق، ترجمة لافتات وصوتيات مفتوحة بدون أي قيود")
}

class SubscriptionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("subscription_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SUB_TYPE = "subscription_type"
        private const val KEY_LAST_RESET = "last_reset_date"
        private const val KEY_IMAGE_COUNT = "image_count_today"
        private const val KEY_VOICE_COUNT = "voice_count_today"
        private const val KEY_SUB_EXPIRY = "subscription_expiry"
        private const val KEY_SUB_EXPIRED = "subscription_is_expired"
        private const val KEY_TEXT_SCALE_INDEX = "text_scale_index"
        private const val KEY_APP_LANGUAGE = "app_language"
    }

    fun getAppLanguage(): String {
        return prefs.getString(KEY_APP_LANGUAGE, "ar") ?: "ar"
    }

    fun setAppLanguage(lang: String) {
        prefs.edit().putString(KEY_APP_LANGUAGE, lang).apply()
    }

    fun getTextScaleIndex(): Int {
        return prefs.getInt(KEY_TEXT_SCALE_INDEX, 1) // default standard/medium (index 1)
    }

    fun setTextScaleIndex(index: Int) {
        prefs.edit().putInt(KEY_TEXT_SCALE_INDEX, index).apply()
    }

    fun getTextScaleMultiplier(): Float {
        return when (getTextScaleIndex()) {
            0 -> 0.85f
            1 -> 1.00f
            2 -> 1.15f
            3 -> 1.30f
            4 -> 1.45f
            5 -> 1.60f
            else -> 1.00f
        }
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun getSubscriptionExpiry(): String {
        val expiry = prefs.getString(KEY_SUB_EXPIRY, "") ?: ""
        if (expiry.isEmpty()) {
            // Set default: 30 days from now
            val calendar = java.util.Calendar.getInstance()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 30)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val generated = sdf.format(calendar.time)
            prefs.edit().putString(KEY_SUB_EXPIRY, generated).apply()
            return generated
        }
        return expiry
    }

    fun setSubscriptionExpiry(dateStr: String) {
        prefs.edit().putString(KEY_SUB_EXPIRY, dateStr).apply()
    }

    fun isSubscriptionExpired(): Boolean {
        // If simulated expired, return true
        if (prefs.getBoolean(KEY_SUB_EXPIRED, false)) return true
        
        val expiry = prefs.getString(KEY_SUB_EXPIRY, "") ?: ""
        if (expiry.isEmpty()) return false
        
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val expiryDate = sdf.parse(expiry)
            val today = sdf.parse(getTodayDateString())
            today.after(expiryDate)
        } catch (e: Exception) {
            false
        }
    }

    fun setSubscriptionExpiredState(expired: Boolean) {
        prefs.edit().putBoolean(KEY_SUB_EXPIRED, expired).apply()
        if (expired) {
            // Force type to FREE if expired
            setSubscriptionType(SubscriptionType.FREE)
        }
    }

    fun renewSubscription(type: SubscriptionType) {
        setSubscriptionType(type)
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 30)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateStr = sdf.format(calendar.time)
        prefs.edit()
            .putString(KEY_SUB_EXPIRY, dateStr)
            .putBoolean(KEY_SUB_EXPIRED, false)
            .apply()
    }

    private fun checkAndResetDailyLimits() {
        val today = getTodayDateString()
        val lastReset = prefs.getString(KEY_LAST_RESET, "") ?: ""
        if (today != lastReset) {
            prefs.edit()
                .putString(KEY_LAST_RESET, today)
                .putInt(KEY_IMAGE_COUNT, 0)
                .putInt(KEY_VOICE_COUNT, 0)
                .apply()
        }
    }

    fun getSubscriptionType(): SubscriptionType {
        val subName = prefs.getString(KEY_SUB_TYPE, SubscriptionType.FREE.name) ?: SubscriptionType.FREE.name
        return try {
            SubscriptionType.valueOf(subName)
        } catch (e: Exception) {
            SubscriptionType.FREE
        }
    }

    fun setSubscriptionType(type: SubscriptionType) {
        prefs.edit().putString(KEY_SUB_TYPE, type.name).apply()
    }

    fun getImageCountToday(): Int {
        checkAndResetDailyLimits()
        return prefs.getInt(KEY_IMAGE_COUNT, 0)
    }

    fun incrementImageCount() {
        checkAndResetDailyLimits()
        val current = getImageCountToday()
        prefs.edit().putInt(KEY_IMAGE_COUNT, current + 1).apply()
    }

    fun getVoiceCountToday(): Int {
        checkAndResetDailyLimits()
        return prefs.getInt(KEY_VOICE_COUNT, 0)
    }

    fun incrementVoiceCount() {
        checkAndResetDailyLimits()
        val current = getVoiceCountToday()
        prefs.edit().putInt(KEY_VOICE_COUNT, current + 1).apply()
    }

    fun canTranslateImage(): Boolean {
        val sub = getSubscriptionType()
        return getImageCountToday() < sub.maxImages
    }

    fun canTranslateVoice(): Boolean {
        val sub = getSubscriptionType()
        return getVoiceCountToday() < sub.maxVoice
    }

    fun getRemainingImages(): Int {
        val sub = getSubscriptionType()
        if (sub == SubscriptionType.ULTRA) return 9999
        val remaining = sub.maxImages - getImageCountToday()
        return if (remaining < 0) 0 else remaining
    }

    fun getRemainingVoice(): Int {
        val sub = getSubscriptionType()
        if (sub == SubscriptionType.ULTRA) return 9999
        val remaining = sub.maxVoice - getVoiceCountToday()
        return if (remaining < 0) 0 else remaining
    }
}
