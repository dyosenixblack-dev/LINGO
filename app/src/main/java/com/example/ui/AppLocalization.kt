package com.example.ui

import com.example.data.SubscriptionType

enum class AppLanguage(val code: String, val displayName: String) {
    AR("ar", "العربية"),
    EN("en", "English"),
    ZH("zh", "中文")
}

object AppLocalization {
    // Current app language code
    var currentLanguageCode = "ar"

    fun get(key: String): String {
        val translations = map[key] ?: return key
        return translations[currentLanguageCode] ?: translations["ar"] ?: key
    }

    private val map = mapOf(
        "app_name" to mapOf(
            "ar" to "Lingo",
            "en" to "Lingo",
            "zh" to "Lingo"
        ),
        "app_subtitle" to mapOf(
            "ar" to "المرافق الذكي في رحلاتك",
            "en" to "Smart travel companion",
            "zh" to "智能旅游助手"
        ),
        "tab_translator" to mapOf(
            "ar" to "المترجم",
            "en" to "Translator",
            "zh" to "翻译官"
        ),
        "tab_history" to mapOf(
            "ar" to "السجل",
            "en" to "History",
            "zh" to "历史记录"
        ),
        "tab_plans" to mapOf(
            "ar" to "الباقات",
            "en" to "Plans",
            "zh" to "订阅方案"
        ),
        "tab_settings" to mapOf(
            "ar" to "الإعدادات",
            "en" to "Settings",
            "zh" to "通用设置"
        ),
        "settings_title" to mapOf(
            "ar" to "الإعدادات العامة لـ Lingo",
            "en" to "General Settings for Lingo",
            "zh" to "Lingo 通用设置"
        ),
        "settings_desc" to mapOf(
            "ar" to "تخصيص مظهر التطبيق وإدارة الاشتراك والبيانات",
            "en" to "Customize design theme, subscriptions & data",
            "zh" to "自定义外观、管理订阅和数据"
        ),
        "active_plan" to mapOf(
            "ar" to "الباقة المفعلة",
            "en" to "Activated Plan",
            "zh" to "当前已启用套餐"
        ),
        "plan_update_auto" to mapOf(
            "ar" to "تاريخ الترقية والتحديث تلقائي",
            "en" to "Upgrade & auto-renewal status",
            "zh" to "升级与自动更新状态"
        ),
        "dark_mode" to mapOf(
            "ar" to "الوضع المظلم",
            "en" to "Dark Mode",
            "zh" to "暗黑模式"
        ),
        "dark_mode_desc" to mapOf(
            "ar" to "تغيير المظهر يدوي لتوفير البطارية",
            "en" to "Manually change appearance to save battery",
            "zh" to "手动切换外观以节省电量"
        ),
        "app_language" to mapOf(
            "ar" to "لغة التطبيق",
            "en" to "App Language",
            "zh" to "应用语言"
        ),
        "app_language_desc" to mapOf(
            "ar" to "تغيير لغة واجهة التطبيق كاملة",
            "en" to "Change the entire application interface language",
            "zh" to "更改整个应用界面的语言"
        ),
        "share_app" to mapOf(
            "ar" to "مشاركة التطبيق مع الأصدقاء",
            "en" to "Share App with friends",
            "zh" to "与朋友分享应用"
        ),
        "share_app_desc" to mapOf(
            "ar" to "شارك Lingo عبر الواتساب، البلوتوث، أو منصات التواصل الأخرى",
            "en" to "Share Lingo via WhatsApp, Bluetooth, or other social platforms",
            "zh" to "通过 WhatsApp、蓝牙或其他社交平台进行分享"
        ),
        "build_with_flash" to mapOf(
            "ar" to "مبني باستعمال فلاش (Gemini 3.5 Flash)",
            "en" to "Powered by Gemini 3.5 Flash",
            "zh" to "基于 Gemini 3.5 Flash 构建"
        ),
        "build_with_flash_desc" to mapOf(
            "ar" to "معالجة عينات الكاميرا والصور والرسائل الصوتية السياحية تلقائياً بدقة ذكاء اصطناعي",
            "en" to "Processes voice and photos using super-fast AI algorithms",
            "zh" to "使用超快人工智能算法处理图像和语音"
        ),
        "zoom_title" to mapOf(
            "ar" to "تكبير وتصغير الكتابة والواجهة",
            "en" to "Text & Interface Zoom Scaling",
            "zh" to "字体与界面缩放"
        ),
        "zoom_desc" to mapOf(
            "ar" to "تحكم في تباعد الكلمات وحجم العرض لتجنب التزاحم أو التداخل",
            "en" to "Control word spacing and layout size to avoid crowding",
            "zh" to "控制字词间距和布局大小以防止拥挤"
        ),
        "zoom_level" to mapOf(
            "ar" to "المستوى",
            "en" to "Level",
            "zh" to "级别"
        ),
        "zoom_live_preview" to mapOf(
            "ar" to "عينة معاينة حية: Lingo يُسهّل تصفح السفر والترجمات بدقة!",
            "en" to "Live preview: Lingo makes dynamic travel and translation effortless!",
            "zh" to "实时预览：Lingo 让动态出行与翻译变得轻而易举！"
        ),
        "guide_title" to mapOf(
            "ar" to "🚨 إرشادات هامة للسفر والأمان:",
            "en" to "🚨 Key Travel & Safety Guidelines:",
            "zh" to "🚨 重要安全与出行指南："
        ),
        "guide_desc" to mapOf(
            "ar" to "• التطبيق يعمل بالاتصال بالانترنت لترجمة الصور الصوتية والنصية.\n• يرجى الاحتفاظ بنسخ محلية من اللوحات الطارئة والاتجاهات الحيوية من دفتري السجل.\n• باقتك المجانية تمكنك من 5 صور و 3 صوتيات باليوم، يمكنك دائماً الترقية لتغطية رحلتك بسهولة مرنة.",
            "en" to "• The app requires active internet connection to translate. \n• Keep offline screenshots of vital signs and emergency directions. \n• Free tier allows 5 images and 3 voices daily; upgrade anytime for unlimited travel coverage.",
            "zh" to "• 应用需网络连接以提供语音、图像与文本翻译。 \n• 建议离线保存应急标识或方向和旅行标记。 \n• 免费套餐每日提供 5 次拍照和 3 次语音翻译；请随时升级以支持无限出行行程。"
        ),
        "rights" to mapOf(
            "ar" to "جميع الحقوق محفوظة\nمنشأ التطبيق: By Younes Hidouri ©",
            "en" to "All rights reserved\nApplication Origin: By Younes Hidouri ©",
            "zh" to "保留所有权利\n应用开发者：By Younes Hidouri ©"
        ),
        "translator_text" to mapOf(
            "ar" to "نص",
            "en" to "Text",
            "zh" to "文本"
        ),
        "translator_voice" to mapOf(
            "ar" to "صوت",
            "en" to "Voice",
            "zh" to "语音"
        ),
        "translator_camera" to mapOf(
            "ar" to "كاميرا",
            "en" to "Camera",
            "zh" to "相机"
        ),
        "enter_source_text" to mapOf(
            "ar" to "ادخل النص السياحي للمسافر...",
            "en" to "Enter traveler's text for travel situation...",
            "zh" to "输入要翻译的旅行文本..."
        ),
        "translate_action" to mapOf(
            "ar" to "ترجم الآن بالـ AI ⚡",
            "en" to "Translate Now via AI ⚡",
            "zh" to "立即人工智能翻译 ⚡"
        ),
        "suggested_translation" to mapOf(
            "ar" to "الترجمة المقترحة لـ",
            "en" to "Suggested translation for",
            "zh" to "翻译结果："
        ),
        "explanation_title" to mapOf(
            "ar" to "تحليل لغوي وسياحي للعبارة 💡",
            "en" to "Linguistic & Cultural Context 💡",
            "zh" to "词汇与文化背景分析 💡"
        ),
        "copy_action" to mapOf(
            "ar" to "نسخ",
            "en" to "Copy",
            "zh" to "复制"
        ),
        "speak_action" to mapOf(
            "ar" to "نطق",
            "en" to "Speak",
            "zh" to "发音"
        ),
        "history_header" to mapOf(
            "ar" to "سجل الترجمات السياحية",
            "en" to "Travel Translation History Log",
            "zh" to "旅行翻译历史记录"
        ),
        "history_subtitle" to mapOf(
            "ar" to "تصفح عباراتك واللافتات التي ترجمتها سابقاً دون الحاجة لإعادة كتابتها",
            "en" to "Browse previously translated phrases and captured signs without retyping",
            "zh" to "浏览以前翻译过的词组和采集标签，无需重新输入"
        ),
        "delete_history_item" to mapOf(
            "ar" to "حذف",
            "en" to "Delete",
            "zh" to "删除"
        ),
        "clear_all" to mapOf(
            "ar" to "مسح الكل",
            "en" to "Clear All",
            "zh" to "清除全部"
        ),
        "no_history" to mapOf(
            "ar" to "لا توجد أي تراجم مسجلة حتى الآن.\nابدأ الترجمة في التبويب الرئيسي لتظهر هنا!",
            "en" to "No history recorded yet.\nStart translating in the main tab to view your records here!",
            "zh" to "目前尚无翻译历史记录。\n在主选项卡开始翻译以在此查看记录！"
        ),
        "plans_header" to mapOf(
            "ar" to "باقات الاشتراك والدفع الفوري الآمن",
            "en" to "Subscription Packages & Secure Payment",
            "zh" to "订阅套餐与安全支付"
        ),
        "plans_subtitle" to mapOf(
            "ar" to "اختر الباقة المناسبة لطبيعة رحلتك وفتراتها للتخلص من كافة القيود",
            "en" to "Choose the best package for your trip and explore without limits",
            "zh" to "选择最适合您旅行的套餐，无限制探索"
        ),
        "active_badge" to mapOf(
            "ar" to "نشط حالياً",
            "en" to "Currently Active",
            "zh" to "当前正在使用"
        ),
        "active_plan_action" to mapOf(
            "ar" to "باقتك النشطة حالياً",
            "en" to "Your current active package",
            "zh" to "您当前启用的套餐"
        ),
        "subscribe_btn" to mapOf(
            "ar" to "اشترك الآن / ترقية الباقة",
            "en" to "Subscribe Now / Upgrade",
            "zh" to "立即订阅/升级套餐"
        ),
        // Additional items for completeness
        "voice_title" to mapOf(
            "ar" to "المترجم الصوتي الذكي في السفر 🎙️",
            "en" to "Smart Voice Travel Translator 🎙️",
            "zh" to "智能语音出行翻译官 🎙️"
        ),
        "voice_desc" to mapOf(
            "ar" to "اضغط للتحدث ثم دع الذكاء الاصطناعي يتعرف ويترجم عباراتك فوراً!",
            "en" to "Tap to speak and let AI instantly recognize and translate your phrases!",
            "zh" to "点击说话，让AI即时识别并翻译您的词组！"
        ),
        "voice_btn_start" to mapOf(
            "ar" to "اضغط على الزر للبدء",
            "en" to "Press button to start",
            "zh" to "按按钮开始"
        ),
        "voice_listening" to mapOf(
            "ar" to "جارٍ الاستماع والترجمة من لغة البدء...",
            "en" to "Listening and translating start language...",
            "zh" to "正在听取并翻译..."
        ),
        "voice_stop_btn" to mapOf(
            "ar" to "توقف وسجّل الترجمة",
            "en" to "Stop & Record translation",
            "zh" to "停止并保存翻译"
        ),
        "voice_sample_phrase" to mapOf(
            "ar" to "عينة عبارة المسافر:",
            "en" to "Sample traveler phrase:",
            "zh" to "实用旅行者样例词组："
        ),
        "camera_title" to mapOf(
            "ar" to "المترجم الذكي للافتات واللوائح والوصفات 📸",
            "en" to "Smart Image OCR & Landmark Sign Translator 📸",
            "zh" to "智能标牌与应急路标图像翻译 📸"
        ),
        "camera_desc" to mapOf(
            "ar" to "التقط صورة للائحة سفر أو اختر صورة ليتم التعرف عليها وترجمتها بالدقة السياحية فوراً!",
            "en" to "Take a picture of a landmark/menu or choose one from library to extract translations!",
            "zh" to "拍摄地标/菜单照片或从相册选择照片以提取翻译！"
        ),
        "camera_choose_btn" to mapOf(
            "ar" to "اختر صورة للافتة",
            "en" to "Choose sign photo",
            "zh" to "选择标牌照片"
        ),
        "camera_capture_btn" to mapOf(
            "ar" to "التقط صورة فورية",
            "en" to "Capture instant photo",
            "zh" to "立即拍摄照片"
        ),
        "camera_processing" to mapOf(
            "ar" to "جارٍ استخلاص وقراءة نصوص وصور اللافتة بالذكاء الاصطناعي...",
            "en" to "Extracting and translating sign text using intelligence...",
            "zh" to "正在使用人工智能提取并翻译路牌/文本..."
        ),
        "copy_success" to mapOf(
            "ar" to "تم النسخ بنجاح",
            "en" to "Copied successfully",
            "zh" to "复制成功"
        ),
        "write_text_alert" to mapOf(
            "ar" to "الرجاء كتابة النص المراد ترجمته أولاً",
            "en" to "Please enter some text in the field first",
            "zh" to "请先在此输入要翻译的内容"
        ),
        "free_limit_voice" to mapOf(
            "ar" to "لقد استهلكت حد ترجمة الرسائل الصوتية اليومي المتاح لباقتك الحالية! يرجى الترقية لزيادة الحد.",
            "en" to "Daily free voice translation limit reached! Please upgrade to continue.",
            "zh" to "已达每日免费语音翻译上限！请升级以继续使用。"
        ),
        "free_limit_image" to mapOf(
            "ar" to "لقد فرغت حدود ترجمة لافتات السفر المتاحة لك اليوم! يرجى الترقية إلى الباقة الممتازة لرفع الحد إلى 10 صور أو اللامحدودة.",
            "en" to "Daily sign translation limit reached! Please upgrade to premium for unlimited uses.",
            "zh" to "路标照片翻译次数本日已用完！请升级为精选包以解除限制。"
        ),
        "simulate_expire_alert" to mapOf(
            "ar" to "تمت محاكاة انتهاء صلاحية باقة %s بنجاح وإرسال تنبيه فوري بالنظام! 🔔",
            "en" to "Simulated package %s expiration & sent a high-priority system alert! 🔔",
            "zh" to "已模拟套餐百分比 %s 到期并发送了高优先级系统通知！🔔"
        )
    )
}

fun SubscriptionType.getTitle(lang: String): String {
    return when (lang) {
        "en" -> when (this) {
            SubscriptionType.FREE -> "Free Tier"
            SubscriptionType.PREMIUM -> "Premium Package"
            SubscriptionType.ULTRA -> "Unlimited Package"
        }
        "zh" -> when (this) {
            SubscriptionType.FREE -> "免费套餐"
            SubscriptionType.PREMIUM -> "精选尊享版套餐"
            SubscriptionType.ULTRA -> "至尊无限制套餐"
        }
        else -> this.titleAr
    }
}

fun SubscriptionType.getDescription(lang: String): String {
    return when (lang) {
        "en" -> when (this) {
            SubscriptionType.FREE -> "Limited daily translation for beginners"
            SubscriptionType.PREMIUM -> "Smart upgrade: double voice and image limits"
            SubscriptionType.ULTRA -> "Infinite travel translation: zero limits"
        }
        "zh" -> when (this) {
            SubscriptionType.FREE -> "新手旅行者有限的日常翻译"
            SubscriptionType.PREMIUM -> "智能升级：双倍语音和图像限制"
            SubscriptionType.ULTRA -> "无限旅行翻译：零限制、更畅意"
        }
        else -> this.descriptionAr
    }
}

fun SubscriptionType.getPrice(lang: String): String {
    return when (lang) {
        "en" -> when (this) {
            SubscriptionType.FREE -> "Free"
            SubscriptionType.PREMIUM -> "$19.99 / m"
            SubscriptionType.ULTRA -> "$39.99 / m"
        }
        "zh" -> when (this) {
            SubscriptionType.FREE -> "免费"
            SubscriptionType.PREMIUM -> "$19.99 / 月"
            SubscriptionType.ULTRA -> "$39.99 / 月"
        }
        else -> this.priceAr
    }
}

fun getLocalizedLanguageName(langNameAr: String): String {
    return when (AppLocalization.currentLanguageCode) {
        "en" -> when (langNameAr) {
            "العربية" -> "Arabic"
            "الإنجليزية" -> "English"
            "الفرنسية" -> "French"
            "الإسبانية" -> "Spanish"
            "الإيطالية" -> "Italian"
            "الألمانية" -> "German"
            "التركية" -> "Turkish"
            "اليابانية" -> "Japanese"
            "الصينية" -> "Chinese"
            "الكورية" -> "Korean"
            "الروسية" -> "Russian"
            else -> langNameAr
        }
        "zh" -> when (langNameAr) {
            "العربية" -> "阿拉伯语"
            "الإنجليزية" -> "英语"
            "الفرنسية" -> "法语"
            "الإسبانية" -> "西班牙语"
            "الإيطالية" -> "意大利语"
            "الألمانية" -> "德语"
            "التركية" -> "土耳其语"
            "اليابانية" -> "日语"
            "الصينية" -> "中文"
            "الكورية" -> "韩语"
            "الروسية" -> "俄语"
            else -> langNameAr
        }
        else -> langNameAr
    }
}

