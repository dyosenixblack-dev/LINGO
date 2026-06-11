package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SubscriptionType
import com.example.data.db.TranslationHistory
import com.example.ui.AppTab
import com.example.ui.TranslatorSubTab
import com.example.ui.TranslationViewModel
import com.example.ui.AppLocalization
import com.example.ui.AppLanguage
import com.example.ui.getTitle
import com.example.ui.getDescription
import com.example.ui.getPrice
import com.example.util.ImageMockGenerator
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Colors list
val NavyLight = Color(0xFFF7F2FA)
val NavyDark = Color(0xFF1C1B1F)
val CardDark = Color(0xFF2B2930)
val AccentTeal = Color(0xFFD0BCFF)
val GoldColor = Color(0xFFE8DEF8)

// Framer Motion spring-style transition specifications
val FramerMotionEnter = fadeIn(
    animationSpec = spring(
        dampingRatio = 0.72f,
        stiffness = 180f
    )
) + slideInVertically(
    initialOffsetY = { 60 },
    animationSpec = spring(
        dampingRatio = 0.72f,
        stiffness = 180f
    )
) + scaleIn(
    initialScale = 0.94f,
    animationSpec = spring(
        dampingRatio = 0.72f,
        stiffness = 180f
    )
)

val FramerMotionExit = fadeOut(
    animationSpec = spring(
        dampingRatio = 0.85f,
        stiffness = 250f
    )
) + slideOutVertically(
    targetOffsetY = { 40 },
    animationSpec = spring(
        dampingRatio = 0.85f,
        stiffness = 250f
    )
) + scaleOut(
    targetScale = 0.95f,
    animationSpec = spring(
        dampingRatio = 0.85f,
        stiffness = 250f
    )
)

val ToastFramerMotionEnter = fadeIn(
    animationSpec = spring(
        dampingRatio = 0.78f,
        stiffness = 220f
    )
) + slideInVertically(
    initialOffsetY = { -it },
    animationSpec = spring(
        dampingRatio = 0.78f,
        stiffness = 220f
    )
) + scaleIn(
    initialScale = 0.90f,
    animationSpec = spring(
        dampingRatio = 0.78f,
        stiffness = 220f
    )
)

val ToastFramerMotionExit = fadeOut(
    animationSpec = spring(
        dampingRatio = 0.90f,
        stiffness = 300f
    )
) + slideOutVertically(
    targetOffsetY = { -it },
    animationSpec = spring(
        dampingRatio = 0.90f,
        stiffness = 300f
    )
) + scaleOut(
    targetScale = 0.92f,
    animationSpec = spring(
        dampingRatio = 0.90f,
        stiffness = 300f
    )
)

val popularLanguages = listOf(
    "العربية", "الإنجليزية", "الفرنسية", "الإسبانية", "الإيطالية", 
    "الألمانية", "التركية", "اليابانية", "الصينية", "الكورية", "الروسية"
)

fun getLocaleForLanguage(languageName: String): java.util.Locale {
    return when (languageName) {
        "العربية" -> java.util.Locale("ar")
        "الإنجليزية" -> java.util.Locale.US
        "الفرنسية" -> java.util.Locale.FRANCE
        "الإسبانية" -> java.util.Locale("es", "ES")
        "الإيطالية" -> java.util.Locale.ITALY
        "الألمانية" -> java.util.Locale.GERMANY
        "التركية" -> java.util.Locale("tr", "TR")
        "اليابانية" -> java.util.Locale.JAPAN
        "الصينية" -> java.util.Locale.CHINA
        "الكورية" -> java.util.Locale.KOREA
        "الروسية" -> java.util.Locale("ru", "RU")
        else -> java.util.Locale.US
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: TranslationViewModel) {
    val context = LocalContext.current
    val systemDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val textScaleMultiplier by viewModel.textScaleMultiplier.collectAsStateWithLifecycle()

    val baseDensity = LocalDensity.current
    val customDensity = remember(baseDensity, textScaleMultiplier) {
        Density(
            density = baseDensity.density,
            fontScale = baseDensity.fontScale * textScaleMultiplier
        )
    }

    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val layoutDirection = if (appLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalDensity provides customDensity,
        LocalLayoutDirection provides layoutDirection
    ) {
        // Clipboard and Toast Handling
        val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
        val scope = rememberCoroutineScope()

    // Active TTS speaker
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        onDispose {
            tts?.shutdown()
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            // Show toast message locally or within a snackbar 
            // In layout we can display a beautiful Toast container
            delay(3500)
            viewModel.clearToast()
        }
    }

    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(3000)
        showSplash = false
    }

    // Dynamic Color Palette Base on Theme Selected
    val backgroundBrush = if (systemDarkMode) {
        Brush.verticalGradient(listOf(Color(0xFF1C1B1F), Color(0xFF141318)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF7F2FA), Color(0xFFE8E1EF)))
    }

    val textColor = if (systemDarkMode) Color(0xFFE6E1E5) else Color(0xFF211F26)
    val cardBgColor = if (systemDarkMode) CardDark else Color(0xFFF3EDF7)
    val secondaryTextColor = if (systemDarkMode) Color(0xFFCAC4D0) else Color(0xFF49454F)

    if (showSplash) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Lingo",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (systemDarkMode) Color(0xFF63C0FF) else Color(0xFF0D47A1),
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "BY YOUNES HIDOURI",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (systemDarkMode) Color(0xFFBBDEFB) else Color(0xFF1565C0),
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(
                    color = if (systemDarkMode) Color(0xFF63C0FF) else Color(0xFF0D47A1),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                TranslationBottomNavigation(
                    currentTab = currentTab,
                    onTabSelect = { viewModel.selectTab(it) },
                    isDarkMode = systemDarkMode
                )
            },
            contentWindowInsets = WindowInsets.navigationBars,
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Header Bar
                    TopHeaderSection(
                        isDarkMode = systemDarkMode,
                        onToggleTheme = { viewModel.isDarkMode.value = !systemDarkMode },
                        onShareClick = {
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(
                                    android.content.Intent.EXTRA_TEXT,
                                    "حمّل تطبيق Lingo المميز لترجمة النصوص والأصوات وصور اللافتات فوراً بدعم الذكاء الاصطناعي! التطبيق مصمم لتسهيل رحلاتك وتواصلك بكافة اللغات. تواصل، ترجم وسافر بكل ثقة!"
                                )
                                type = "text/plain"
                            }
                            val shareIntent = android.content.Intent.createChooser(sendIntent, "مشاركة لـ Lingo عبر:")
                            context.startActivity(shareIntent)
                        }
                    )

                    // Show Content for current tab
                    Box(modifier = Modifier.weight(1f)) {
                        when (currentTab) {
                            AppTab.TRANSLATOR -> TranslatorTabScreen(
                                viewModel = viewModel,
                                cardBgColor = cardBgColor,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor,
                                isDarkMode = systemDarkMode,
                                tts = tts,
                                clipboardManager = clipboardManager
                            )
                            AppTab.HISTORY -> HistoryTabScreen(
                                viewModel = viewModel,
                                cardBgColor = cardBgColor,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor,
                                tts = tts,
                                clipboardManager = clipboardManager
                            )
                            AppTab.PLANS -> PlansTabScreen(
                                viewModel = viewModel,
                                cardBgColor = cardBgColor,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor,
                                isDarkMode = systemDarkMode
                            )
                            AppTab.SETTINGS -> SettingsTabScreen(
                                viewModel = viewModel,
                                cardBgColor = cardBgColor,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor,
                                isDarkMode = systemDarkMode
                            )
                        }
                    }
                }

                // Elegant Custom SnackBar / Toast Alert Notification
                AnimatedVisibility(
                    visible = toastMessage != null,
                    enter = ToastFramerMotionEnter,
                    exit = ToastFramerMotionExit,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp)
                        .padding(horizontal = 24.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (systemDarkMode) Color(0xFF1E283A) else Color(0xFF334155)),
                        elevation = CardDefaults.cardElevation(10.dp),
                        border = BorderStroke(1.dp, AccentTeal.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "تنبيه",
                                tint = AccentTeal,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = toastMessage ?: "",
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun TopHeaderSection(isDarkMode: Boolean, onToggleTheme: () -> Unit, onShareClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) CardDark.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Identity
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AccentTeal.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "مترجم",
                        tint = AccentTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = AppLocalization.get("app_name"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color(0xFF63C0FF) else Color(0xFF0D47A1)
                    )
                    Text(
                        text = AppLocalization.get("app_subtitle"),
                        fontSize = 11.sp,
                        color = AccentTeal
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Share button
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF262E3B) else Color(0xFFE2E8F0))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "مشاركة التطبيق",
                        tint = if (isDarkMode) AccentTeal else Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Dark Mode toggle switch
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF262E3B) else Color(0xFFE2E8F0))
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.NightsStay,
                        contentDescription = "تبديل المظهر",
                        tint = if (isDarkMode) GoldColor else Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TranslationBottomNavigation(
    currentTab: AppTab,
    onTabSelect: (AppTab) -> Unit,
    isDarkMode: Boolean
) {
    NavigationBar(
        containerColor = if (isDarkMode) Color(0xFF0F121C) else Color(0xFFEDF2FA),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentTab == AppTab.TRANSLATOR,
            onClick = { onTabSelect(AppTab.TRANSLATOR) },
            icon = { Icon(Icons.Default.Translate, contentDescription = AppLocalization.get("tab_translator")) },
            label = { Text(AppLocalization.get("tab_translator"), fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentTeal,
                selectedTextColor = AccentTeal,
                indicatorColor = AccentTeal.copy(alpha = 0.15f),
                unselectedIconColor = if (isDarkMode) Color(0xFF8E9AA8) else Color(0xFF64748B),
                unselectedTextColor = if (isDarkMode) Color(0xFF8E9AA8) else Color(0xFF64748B)
            ),
            modifier = Modifier.testTag("nav_translator")
        )
        NavigationBarItem(
            selected = currentTab == AppTab.HISTORY,
            onClick = { onTabSelect(AppTab.HISTORY) },
            icon = { Icon(Icons.Default.History, contentDescription = AppLocalization.get("tab_history")) },
            label = { Text(AppLocalization.get("tab_history"), fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentTeal,
                selectedTextColor = AccentTeal,
                indicatorColor = AccentTeal.copy(alpha = 0.15f),
                unselectedIconColor = if (isDarkMode) Color(0xFF8E9AA8) else Color(0xFF64748B),
                unselectedTextColor = if (isDarkMode) Color(0xFF8E9AA8) else Color(0xFF64748B)
            ),
            modifier = Modifier.testTag("nav_history")
        )
        NavigationBarItem(
            selected = currentTab == AppTab.PLANS,
            onClick = { onTabSelect(AppTab.PLANS) },
            icon = { Icon(Icons.Default.Diamond, contentDescription = AppLocalization.get("tab_plans")) },
            label = { Text(AppLocalization.get("tab_plans"), fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentTeal,
                selectedTextColor = AccentTeal,
                indicatorColor = AccentTeal.copy(alpha = 0.15f),
                unselectedIconColor = if (isDarkMode) Color(0xFF8E9AA8) else Color(0xFF64748B),
                unselectedTextColor = if (isDarkMode) Color(0xFF8E9AA8) else Color(0xFF64748B)
            ),
            modifier = Modifier.testTag("nav_plans")
        )
        NavigationBarItem(
            selected = currentTab == AppTab.SETTINGS,
            onClick = { onTabSelect(AppTab.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = AppLocalization.get("tab_settings")) },
            label = { Text(AppLocalization.get("tab_settings"), fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentTeal,
                selectedTextColor = AccentTeal,
                indicatorColor = AccentTeal.copy(alpha = 0.15f),
                unselectedIconColor = if (isDarkMode) Color(0xFF8E9AA8) else Color(0xFF64748B),
                unselectedTextColor = if (isDarkMode) Color(0xFF8E9AA8) else Color(0xFF64748B)
            ),
            modifier = Modifier.testTag("nav_settings")
        )
    }
}

// -------------------------------------------------------------
// TRANSLATOR TAB SCREEN (TEXT, VOICE, CAMERA SUB-TABS)
// -------------------------------------------------------------
@Composable
fun TranslatorTabScreen(
    viewModel: TranslationViewModel,
    cardBgColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    isDarkMode: Boolean,
    tts: TextToSpeech?,
    clipboardManager: ClipboardManager
) {
    val subTab by viewModel.currentSubTab.collectAsStateWithLifecycle()
    val sourceLang by viewModel.sourceLang.collectAsStateWithLifecycle()
    val targetLang by viewModel.targetLang.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var showSourceDrop by remember { mutableStateOf(false) }
    var showTargetDrop by remember { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (showSourceDrop || showTargetDrop) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Source -> Target Selector Row
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Source Language button
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { showSourceDrop = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal.copy(alpha = 0.1f), contentColor = AccentTeal),
                        modifier = Modifier.fillMaxWidth().testTag("source_lang_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(sourceLang, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = showSourceDrop,
                        onDismissRequest = { showSourceDrop = false }
                    ) {
                        popularLanguages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang) },
                                onClick = {
                                    viewModel.sourceLang.value = lang
                                    showSourceDrop = false
                                }
                            )
                        }
                    }
                }

                // Swap Button
                IconButton(
                    onClick = { viewModel.swapLanguages() },
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(42.dp)
                        .background(AccentTeal.copy(alpha = 0.15f), CircleShape)
                        .testTag("swap_lang_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "تبديل اللغات",
                        tint = AccentTeal,
                        modifier = Modifier.rotate(rotationAngle)
                    )
                }

                // Target Language button
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { showTargetDrop = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal.copy(alpha = 0.1f), contentColor = AccentTeal),
                        modifier = Modifier.fillMaxWidth().testTag("target_lang_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(targetLang, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = showTargetDrop,
                        onDismissRequest = { showTargetDrop = false }
                    ) {
                        popularLanguages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang) },
                                onClick = {
                                    viewModel.targetLang.value = lang
                                    showTargetDrop = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Feature Type Sub-tabs (Text, Voice, Camera)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF161B29) else Color(0xFFEBEFF5)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            val subTabLabels = when (AppLocalization.currentLanguageCode) {
                "en" -> mapOf(
                    TranslatorSubTab.TEXT to "Text",
                    TranslatorSubTab.VOICE to "Voice",
                    TranslatorSubTab.CAMERA to "Camera"
                )
                "zh" -> mapOf(
                    TranslatorSubTab.TEXT to "文本翻译",
                    TranslatorSubTab.VOICE to "语音翻译",
                    TranslatorSubTab.CAMERA to "拍照/图片"
                )
                else -> mapOf(
                    TranslatorSubTab.TEXT to "نص مكتوب",
                    TranslatorSubTab.VOICE to "رسالة صوتية",
                    TranslatorSubTab.CAMERA to "لافتة / صورة"
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    TranslatorSubTab.TEXT to Icons.Default.Keyboard,
                    TranslatorSubTab.VOICE to Icons.Default.Mic,
                    TranslatorSubTab.CAMERA to Icons.Default.PhotoCamera
                ).forEach { (tab, icon) ->
                    val label = subTabLabels[tab] ?: ""
                    val selected = subTab == tab
                    val tabColor by animateColorAsState(
                        targetValue = if (selected) AccentTeal else Color.Transparent
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (selected) Color.White else (if (isDarkMode) Color(0xFF8E9AA8) else Color(0xFF64748B))
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tabColor)
                            .clickable { viewModel.selectSubTab(tab) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(label, color = contentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // SubTab Detail Card
        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AccentTeal)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("جاري الترجمة بذكاء اصطناعي فائق...", color = secondaryTextColor, fontSize = 13.sp)
                    }
                }
            } else {
                when (subTab) {
                    TranslatorSubTab.TEXT -> TextTranslatorView(
                        viewModel = viewModel,
                        cardBgColor = cardBgColor,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        tts = tts,
                        clipboardManager = clipboardManager
                    )
                    TranslatorSubTab.VOICE -> VoiceTranslatorView(
                        viewModel = viewModel,
                        cardBgColor = cardBgColor,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        isDarkMode = isDarkMode,
                        tts = tts,
                        clipboardManager = clipboardManager
                    )
                    TranslatorSubTab.CAMERA -> CameraTranslatorView(
                        viewModel = viewModel,
                        cardBgColor = cardBgColor,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        isDarkMode = isDarkMode,
                        tts = tts,
                        clipboardManager = clipboardManager
                    )
                }
            }
        }
    }
}

// TEXT TRANSLATOR BOX
@Composable
fun TextTranslatorView(
    viewModel: TranslationViewModel,
    cardBgColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    tts: TextToSpeech?,
    clipboardManager: ClipboardManager
) {
    val srcText by viewModel.sourceText.collectAsStateWithLifecycle()
    val translationResult by viewModel.translationResult.collectAsStateWithLifecycle()
    val targetLang by viewModel.targetLang.collectAsStateWithLifecycle()
    val kbd = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = srcText,
                    onValueChange = { viewModel.sourceText.value = it },
                    placeholder = { Text("اكتب النص المراد ترجمته هنا...", fontSize = 14.sp, color = secondaryTextColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("text_input_field"),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        viewModel.performTextTranslation()
                        kbd?.hide()
                    })
                )

                // Input control buttons (Clear text)
                if (srcText.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { viewModel.sourceText.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح", tint = secondaryTextColor)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Big Translate trigger button
        Button(
            onClick = {
                viewModel.performTextTranslation()
                kbd?.hide()
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("translate_btn")
        ) {
            Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("ترجم النص الفوري", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Translation Result Display Cards
        AnimatedVisibility(
            visible = translationResult.isNotEmpty(),
            enter = FramerMotionEnter,
            exit = FramerMotionExit
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AccentTeal.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, AccentTeal.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "الترجمة المقترحة لـ ($targetLang):",
                        fontSize = 12.sp,
                        color = AccentTeal,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = translationResult,
                        fontSize = 16.sp,
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 24.sp,
                        modifier = Modifier.fillMaxWidth().testTag("text_result_view")
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons to copy + tts speak
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                clipboardManager.setPrimaryClip(ClipData.newPlainText("Translation", translationResult))
                                viewModel.showToast("تم نسخ الترجمة بنجاح إلى الحافظة")
                            },
                            modifier = Modifier.testTag("copy_text_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = AccentTeal)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = {
                                val locale = getLocaleForLanguage(targetLang)
                                tts?.language = locale
                                tts?.speak(translationResult, TextToSpeech.QUEUE_FLUSH, null, null)
                            },
                            modifier = Modifier.testTag("speak_text_btn")
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "نطق الترجمة", tint = AccentTeal)
                        }
                    }
                }
            }
        }
    }
}

// VOICE TRANSLATOR VIEW (SPEECH GENERATOR WITH CUSTOM COROUTINE WAVE CANVAS)
@Composable
fun VoiceTranslatorView(
    viewModel: TranslationViewModel,
    cardBgColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    isDarkMode: Boolean,
    tts: TextToSpeech?,
    clipboardManager: ClipboardManager
) {
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val voiceTextPhrase by viewModel.voiceTextPhrase.collectAsStateWithLifecycle()
    val voiceResult by viewModel.voiceTranslationResult.collectAsStateWithLifecycle()
    val targetLang by viewModel.targetLang.collectAsStateWithLifecycle()
    val remainingVoice by viewModel.remainingVoice.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    // Fluctuating waveform generator animation
    val infiniteTransition = rememberInfiniteTransition()
    val waveHeightPercent by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Daily limit indicator
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AccentTeal.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, AccentTeal.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("المتبقي من الرسائل الصوتية اليوم:", fontSize = 12.sp, color = textColor)
                Text(
                    text = if (remainingVoice > 100) "لامحدود" else "$remainingVoice رسائل",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentTeal
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center mic button / active waves
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isRecording) {
                    Text("جاري الاستماع ولقط الصوت...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AccentTeal)
                    Spacer(modifier = Modifier.height(18.dp))

                    // Simulated live recording soundwaves
                    Box(modifier = Modifier.height(60.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.width(160.dp).fillMaxHeight()) {
                            val barWidth = 10f
                            val spacing = 15f
                            val center = size.height / 2
                            val colors = listOf(Color(0xFF0D47A1), AccentTeal, Color(0xFF03A9F4))

                            for (i in 0..8) {
                                val offset = waveHeightPercent * if (i % 2 == 0) 0.5f else 0.9f
                                val barHeight = size.height * offset * (1f - Math.abs(i - 4) * 0.15f)
                                drawRoundRect(
                                    color = colors[i % colors.size],
                                    topLeft = androidx.compose.ui.geometry.Offset((i * spacing), center - barHeight / 2),
                                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(4f, 4f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            // Finish and send a preset spoken text
                            viewModel.stopRecordingAndTranslate("أهلاً، هل يمكنك إرشادي إلى المسار الصحيح للمترو؟")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.height(48.dp).testTag("stop_recording_btn")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إنهاء الإرسال والترجمة", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Pre-activation view
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(AccentTeal.copy(alpha = 0.15f))
                            .clickable { viewModel.startRecordingVoice() }
                            .testTag("mic_trigger_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "بدء التسجيل",
                            tint = AccentTeal,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "اضغط لتسجيل رسالة صوتية وترجمتها",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "أو اختر أحد التعبيرات السياحية المادية مسبقاً:",
                        fontSize = 11.sp,
                        color = secondaryTextColor,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset traveler dialog blocks
                    val quickSpeakPhrases = listOf(
                        "أين أقرب صيدلية مفتوحة الآن؟",
                        "هل يشتمل إيجار الغرفة على وجبة الإفطار؟",
                        "لو سمحت، أريد قائمة طعام باللغة العربية.",
                        "كم تبعد محطة الحافلات عن الفندق؟"
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickSpeakPhrases) { phrase ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.stopRecordingAndTranslate(phrase)
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF262E3B) else Color(0xFFF1F5F9))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(phrase, fontSize = 12.sp, color = textColor)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Result displays
        AnimatedVisibility(
            visible = voiceResult.isNotEmpty() || voiceTextPhrase.isNotEmpty(),
            enter = FramerMotionEnter,
            exit = FramerMotionExit
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AccentTeal.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, AccentTeal.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "العبارة المسموعة باللغة المصدر:",
                        fontSize = 11.sp,
                        color = secondaryTextColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = voiceTextPhrase,
                        fontSize = 14.sp,
                        color = textColor,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Divider(color = AccentTeal.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "الترجمة المقترحة لـ ($targetLang):",
                        fontSize = 11.sp,
                        color = AccentTeal,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = voiceResult,
                        fontSize = 15.sp,
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp).testTag("voice_result_view")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = {
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("Voice Translation", voiceResult))
                            viewModel.showToast("تم النسخ بنجاح")
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = AccentTeal, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(onClick = {
                            val locale = getLocaleForLanguage(targetLang)
                            tts?.language = locale
                            tts?.speak(voiceResult, TextToSpeech.QUEUE_FLUSH, null, null)
                        }) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "نطق", tint = AccentTeal, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// SIGN AND CAMERA TRANSLATOR VIEW (MULTIMODAL Vision OCR)
@Composable
fun CameraTranslatorView(
    viewModel: TranslationViewModel,
    cardBgColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    isDarkMode: Boolean,
    tts: TextToSpeech?,
    clipboardManager: ClipboardManager
) {
    val selectedBitmap by viewModel.selectedBitmap.collectAsStateWithLifecycle()
    val imageResult by viewModel.imageTranslationResult.collectAsStateWithLifecycle()
    val targetLang by viewModel.targetLang.collectAsStateWithLifecycle()
    val remainingImages by viewModel.remainingImages.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Media and gallery launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        // In real app, convert uri to bitmap, but here we can feed into the view model
        uri?.let {
            try {
                val input = context.contentResolver.openInputStream(it)
                val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                if (bitmap != null) {
                    viewModel.setImageAndTranslate(bitmap, "صورة مستوردة من المعرض")
                } else {
                    viewModel.showToast("عذراً فشل قراءة الملف بصيغة صورة صالحة")
                }
            } catch (e: Exception) {
                viewModel.showToast("فشل فتح الصورة: ${e.localizedMessage}")
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            viewModel.setImageAndTranslate(it, "صورة لاقطة بالكاميرا")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Daily Limit Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AccentTeal.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, AccentTeal.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("المتبقي من تراجم لافتات الكاميرا وصورها اليوم:", fontSize = 11.sp, color = textColor)
                Text(
                    text = if (remainingImages > 100) "لامحدود" else "$remainingImages صور",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentTeal
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Center preview screen or preset options
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                item {
                    if (selectedBitmap != null) {
                        Text("تم التقاط لافتة صالحة للترجمة:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                        Spacer(modifier = Modifier.height(10.dp))
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "الملف المختار كعينة",
                            modifier = Modifier
                                .height(130.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(BorderStroke(2.dp, AccentTeal), RoundedCornerShape(16.dp))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        // Empty photo screen display
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("طريقة التقاط لافتات السفر والتحويل المكتوب:", fontSize = 13.sp, color = secondaryTextColor, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                item {
                    // Triggers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { cameraLauncher.launch() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("launch_camera_btn")
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الكاميرا", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDarkMode) Color(0xFF2E3B52) else Color(0xFFE2E8F0), contentColor = textColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("launch_gallery_btn")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("المعرض", fontSize = 12.sp)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = AccentTeal.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "لافتات سياحية نموذجية لاختبار الكاميرا فوراً في المحاكي:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    // Row/Grid of templates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TemplateCard(
                            label = "🚨 لافتة يابانية",
                            subLabel = "مخرج طوارئ",
                            onClick = {
                                val b = ImageMockGenerator.generateJapaneseExitSign()
                                viewModel.setImageAndTranslate(b, "لافتة Exit يابانية")
                            },
                            modifier = Modifier.weight(1f).testTag("sample_jpx_sign")
                        )

                        TemplateCard(
                            label = "🍝 قائمة إيطالية",
                            subLabel = "فاتورة مطعم",
                            onClick = {
                                val b = ImageMockGenerator.generateItalianMenu()
                                viewModel.setImageAndTranslate(b, "فاتورة مطعم إيطالي")
                            },
                            modifier = Modifier.weight(1f).testTag("sample_ita_menu")
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TemplateCard(
                            label = "✈️ مطار باريس",
                            subLabel = "حالة رحلات فرنسا",
                            onClick = {
                                val b = ImageMockGenerator.generateFrenchAirportBoard()
                                viewModel.setImageAndTranslate(b, "جدولة رحلات طيران باريس CDG")
                            },
                            modifier = Modifier.weight(1f)
                        )

                        TemplateCard(
                            label = "🚇 مترو برلين",
                            subLabel = "شباك تذاكر ألمانيا",
                            onClick = {
                                val b = ImageMockGenerator.generateGermanMetroSign()
                                viewModel.setImageAndTranslate(b, "لوحة تذاكر ألمانية U-Bahn")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Output Display
        AnimatedVisibility(
            visible = imageResult.isNotEmpty(),
            enter = FramerMotionEnter,
            exit = FramerMotionExit
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AccentTeal.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, AccentTeal.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ترجمة وتحليل اللافتة لـ ($targetLang):",
                        fontSize = 11.sp,
                        color = AccentTeal,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = imageResult,
                        fontSize = 14.sp,
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp,
                        modifier = Modifier.testTag("ocr_result_view")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = {
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("Image Translation", imageResult))
                            viewModel.showToast("تم النسخ بنجاح")
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = AccentTeal, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(onClick = {
                            val locale = getLocaleForLanguage(targetLang)
                            tts?.language = locale
                            tts?.speak(imageResult, TextToSpeech.QUEUE_FLUSH, null, null)
                        }) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "نطق", tint = AccentTeal, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TemplateCard(
    label: String,
    subLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .themeOutline()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AccentTeal.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentTeal)
            Text(subLabel, fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

// Inline custom Modifier styles
fun Modifier.themeOutline() = this.border(
    width = 1.dp,
    color = AccentTeal.copy(alpha = 0.15f),
    shape = RoundedCornerShape(12.dp)
)

// -------------------------------------------------------------
// HISTORY/RECORDS TAB SCREEN
// -------------------------------------------------------------
@Composable
fun HistoryTabScreen(
    viewModel: TranslationViewModel,
    cardBgColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    tts: TextToSpeech?,
    clipboardManager: ClipboardManager
) {
    val history by viewModel.historyList.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("سجل الدفاتر والترجمات", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
            if (history.isNotEmpty()) {
                Text(
                    text = "مسح الكل",
                    fontSize = 13.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { viewModel.clearAllHistory() }
                        .testTag("clear_all_history_text")
                )
            }
        }

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("السجل فارغ تماماً", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text(
                        text = "ترجم نصوصاً، خطوطاً صوتية أو لافتات ومستندات لتجدها مؤرشفة هنا.",
                        fontSize = 12.sp,
                        color = secondaryTextColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp).padding(top = 6.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history) { record ->
                    HistoryItemCard(
                        record = record,
                        bgColor = cardBgColor,
                        colorsText = textColor,
                        secTextColor = secondaryTextColor,
                        onDelete = { viewModel.deleteHistoryItem(record.id) },
                        tts = tts,
                        clipboardManager = clipboardManager,
                        viewModel = viewModel
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    record: TranslationHistory,
    bgColor: Color,
    colorsText: Color,
    secTextColor: Color,
    onDelete: () -> Unit,
    tts: TextToSpeech?,
    clipboardManager: ClipboardManager,
    viewModel: TranslationViewModel
) {
    val timeFormatted = remember(record.timestamp) {
        val sdf = SimpleDateFormat("HH:mm - MM/dd", Locale.getDefault())
        sdf.format(Date(record.timestamp))
    }

    val typeIcon = when (record.type) {
        "text" -> Icons.Default.Keyboard
        "voice" -> Icons.Default.Mic
        else -> Icons.Default.PhotoCamera
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(typeIcon, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${record.sourceLang} ➔ ${record.targetLang}",
                        fontSize = 11.sp,
                        color = AccentTeal,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(timeFormatted, fontSize = 10.sp, color = secTextColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = record.sourceText,
                fontSize = 13.sp,
                color = colorsText,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))
            Divider(color = colorsText.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = record.translatedText,
                fontSize = 13.sp,
                color = AccentTeal,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action row for Copy + Speak (TTS)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        clipboardManager.setPrimaryClip(ClipData.newPlainText("Translation", record.translatedText))
                        viewModel.showToast("تم النسخ بنجاح")
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = AccentTeal, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val locale = getLocaleForLanguage(record.targetLang)
                        tts?.language = locale
                        tts?.speak(record.translatedText, TextToSpeech.QUEUE_FLUSH, null, null)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "نطق الترجمة", tint = AccentTeal, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PRICING / PLANS / SUB TAB SCREEN
// -------------------------------------------------------------
@Composable
fun PlansTabScreen(
    viewModel: TranslationViewModel,
    cardBgColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    isDarkMode: Boolean
) {
    val activeSubscription by viewModel.subscriptionType.collectAsStateWithLifecycle()
    val expiryDate by viewModel.subscriptionExpiry.collectAsStateWithLifecycle()
    val isExpired by viewModel.isSubscriptionExpired.collectAsStateWithLifecycle()

    var showCheckoutFor by remember { mutableStateOf<SubscriptionType?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "باقات الاشتراك السياحية 🌍",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
        Text(
            text = "احصل على ترقية مرنة لتغطية شاملة لرحلتك دون القلق من انقطاع الترجمة",
            fontSize = 11.sp,
            color = secondaryTextColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // VIP Subscription Status card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = BorderStroke(1.dp, AccentTeal.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "حالة الاشتراك الحالي:",
                            fontSize = 11.sp,
                            color = secondaryTextColor
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Text(
                                text = activeSubscription.titleAr,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentTeal
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (isExpired) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Red.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("منتهي ⚠️", fontSize = 9.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AccentTeal.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("نشط ✅", fontSize = 9.sp, color = AccentTeal, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isExpired) "حالة التجديد الدوري:" else "تاريخ التجديد/الانتهاء:",
                            fontSize = 11.sp,
                            color = secondaryTextColor
                        )
                        Text(
                            text = if (isExpired) "مطلوب تجديد الباقة" else expiryDate,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpired) Color.Red else textColor,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = textColor.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.simulateSubscriptionExpiration() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive, 
                        contentDescription = null, 
                        modifier = Modifier.size(16.dp), 
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "محاكاة انتهاء الصلاحية وتجربة التنبيه 🔔",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SubscriptionPackageCard(
                    packageType = SubscriptionType.FREE,
                    active = activeSubscription == SubscriptionType.FREE && !isExpired,
                    bgColor = cardBgColor,
                    textColor = textColor,
                    secTextColor = secondaryTextColor,
                    features = listOf(
                        "📷 ترجمة كاميرا 5 صور باليوم حد أقصى",
                        "🎙️ ترجمة رسائل صوتية 3 باليوم حد أقصى",
                        "🌐 ترجمة نصوص مفتوحة بكافة اللغات",
                        "💾 حفظ تلقائي مفهرس للسجلات محلياً"
                    ),
                    onActivate = { viewModel.upgradeSubscription(SubscriptionType.FREE) }
                )
            }

            item {
                SubscriptionPackageCard(
                    packageType = SubscriptionType.PREMIUM,
                    active = activeSubscription == SubscriptionType.PREMIUM && !isExpired,
                    bgColor = cardBgColor,
                    textColor = textColor,
                    secTextColor = secondaryTextColor,
                    features = listOf(
                        "📷 تضاعف الحد لـ 10 صور لافتات يومياً",
                        "🎙️ ترقية الترجمة لـ 5 رسائل صوتية يومياً",
                        "⚡ سيرفرات ترجمة بذكاء اصطناعي فائق السرعة",
                        "🔄 تجديد تلقائي شهري ومرونة في الإلغاء"
                    ),
                    onActivate = { showCheckoutFor = SubscriptionType.PREMIUM }
                )
            }

            item {
                SubscriptionPackageCard(
                    packageType = SubscriptionType.ULTRA,
                    active = activeSubscription == SubscriptionType.ULTRA && !isExpired,
                    bgColor = cardBgColor,
                    textColor = textColor,
                    secTextColor = secondaryTextColor,
                    features = listOf(
                        "📷 ترجمة لافتات كاميرا وصور بكافة الطرق (لامحدودة)",
                        "🎙️ ترجمة رسائل صوتية وجمل شفهية (لامحدودة)",
                        "🚀 تصفية وعزل متقدم للضوضاء في التسجيل",
                        "👑 رعاية ودعم سياحي ذكي طوال الـ 24 ساعة"
                    ),
                    onActivate = { showCheckoutFor = SubscriptionType.ULTRA }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Interactive Checkout Overlay Modal
    showCheckoutFor?.let { packageType ->
        PaymentCheckoutDialog(
            packageType = packageType,
            cardBgColor = cardBgColor,
            textColor = textColor,
            secTextColor = secondaryTextColor,
            onDismiss = { showCheckoutFor = null },
            onPaymentSuccess = { paymentMethod ->
                viewModel.purchaseSubscription(
                    type = packageType,
                    paymentMethod = paymentMethod,
                    priceStr = packageType.priceAr
                )
                showCheckoutFor = null
            }
        )
    }
}

@Composable
fun PaymentCheckoutDialog(
    packageType: SubscriptionType,
    cardBgColor: Color,
    textColor: Color,
    secTextColor: Color,
    onDismiss: () -> Unit,
    onPaymentSuccess: (String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("card") } // "card" or "paypal"
    
    // Bank Card form states
    var cardNumber by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    
    // PayPal form states
    var paypalEmail by remember { mutableStateOf("") }
    var paypalPassword by remember { mutableStateOf("") }
    
    // Transaction running state
    var isProcessing by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = BorderStroke(1.dp, AccentTeal.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // SSL Security Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock, 
                        contentDescription = null, 
                        tint = AccentTeal, 
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "بوابة دفع آمنة مشفرة 100% (SSL)",
                        fontSize = 10.sp,
                        color = AccentTeal,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "تأكيد الاشتراك في ${packageType.titleAr}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                
                // Dynamic price determined automatically
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentTeal.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "السعر المستحق تلقائياً: ${packageType.priceAr}",
                        fontSize = 14.sp,
                        color = AccentTeal,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!isProcessing) {
                    // Quick Selector Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(textColor.copy(alpha = 0.05f))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedMethod == "card") AccentTeal else Color.Transparent)
                                .clickable { selectedMethod = "card" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard, 
                                    contentDescription = null, 
                                    tint = if (selectedMethod == "card") Color.White else secTextColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "بطاقة بنكية", 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedMethod == "card") Color.White else secTextColor
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedMethod == "paypal") AccentTeal else Color.Transparent)
                                .clickable { selectedMethod = "paypal" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Payment, 
                                    contentDescription = null, 
                                    tint = if (selectedMethod == "paypal") Color.White else secTextColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "بوابة PayPal", 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedMethod == "paypal") Color.White else secTextColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Gateway Specific Layout Input Fields
                    if (selectedMethod == "card") {
                        // Cardholder Name input
                        OutlinedTextField(
                            value = cardName,
                            onValueChange = { cardName = it },
                            label = { Text("اسم صاحب البطاقة", fontSize = 11.sp) },
                            textStyle = TextStyle(fontSize = 12.sp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentTeal,
                                focusedLabelColor = AccentTeal
                            )
                        )

                        // Card Number input
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { if (it.length <= 16) cardNumber = it.filter { char -> char.isDigit() } },
                            label = { Text("رقم البطاقة (16 رقم مالي)", fontSize = 11.sp) },
                            textStyle = TextStyle(fontSize = 12.sp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentTeal,
                                focusedLabelColor = AccentTeal
                            )
                        )

                        // Date and CVV Row inputs
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = cardExpiry,
                                onValueChange = { cardExpiry = it },
                                label = { Text("الصلاحية MM/YY", fontSize = 10.sp) },
                                textStyle = TextStyle(fontSize = 12.sp),
                                singleLine = true,
                                modifier = Modifier.weight(1.2f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentTeal,
                                    focusedLabelColor = AccentTeal
                                )
                            )
                            OutlinedTextField(
                                value = cardCvv,
                                onValueChange = { if (it.length <= 3) cardCvv = it.filter { char -> char.isDigit() } },
                                label = { Text("CVV", fontSize = 10.sp) },
                                textStyle = TextStyle(fontSize = 12.sp),
                                singleLine = true,
                                modifier = Modifier.weight(0.8f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentTeal,
                                    focusedLabelColor = AccentTeal
                                )
                            )
                        }
                    } else {
                        // PayPal Email input
                        OutlinedTextField(
                            value = paypalEmail,
                            onValueChange = { paypalEmail = it },
                            label = { Text("بريد حساب PayPal", fontSize = 11.sp) },
                            textStyle = TextStyle(fontSize = 12.sp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentTeal,
                                focusedLabelColor = AccentTeal
                            )
                        )

                        // PayPal Password input
                        OutlinedTextField(
                            value = paypalPassword,
                            onValueChange = { paypalPassword = it },
                            label = { Text("كلمة مرور PayPal", fontSize = 11.sp) },
                            textStyle = TextStyle(fontSize = 12.sp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentTeal,
                                focusedLabelColor = AccentTeal
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Dialog Options Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("إلغاء", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                isProcessing = true
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(1600) // Realistic secure banking processing
                                    isProcessing = false
                                    onPaymentSuccess(if (selectedMethod == "card") "البطاقة المصرفية" else "حساب PayPal")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = if (selectedMethod == "card") {
                                cardName.isNotBlank() && cardNumber.length == 16 && cardExpiry.isNotBlank() && cardCvv.length == 3
                            } else {
                                paypalEmail.isNotBlank() && paypalEmail.contains("@") && paypalPassword.isNotBlank()
                            }
                        ) {
                            Text("تأكيد ودفع ${packageType.priceAr}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Processing Progress
                    Spacer(modifier = Modifier.height(20.dp))
                    CircularProgressIndicator(color = AccentTeal, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "جاري معالجة عملية الشراء بأمان...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Text(
                        text = "يرجى عدم غلق التطبيق أو الرجوع أثناء معالجة تشفير SSL.",
                        fontSize = 10.sp,
                        color = secTextColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SubscriptionPackageCard(
    packageType: SubscriptionType,
    active: Boolean,
    bgColor: Color,
    textColor: Color,
    secTextColor: Color,
    features: List<String>,
    onActivate: () -> Unit
) {
    val outlineBorder = if (active) {
        BorderStroke(2.dp, AccentTeal)
    } else {
        BorderStroke(1.dp, AccentTeal.copy(alpha = 0.15f))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = outlineBorder,
        elevation = CardDefaults.cardElevation(if (active) 6.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = packageType.titleAr,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        if (active) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentTeal)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("نشط حالياً", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(packageType.descriptionAr, fontSize = 11.sp, color = secTextColor, modifier = Modifier.padding(top = 2.dp))
                }

                // Price Badge
                Text(
                    text = packageType.priceAr,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentTeal
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = textColor.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(12.dp))

            // Bullet points
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                features.forEach { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(feature, fontSize = 12.sp, color = textColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!active) {
                Button(
                    onClick = onActivate,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                    modifier = Modifier.fillMaxWidth().testTag("activate_${packageType.name}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("اشترك الآن / ترقية الباقة", fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentTeal),
                    border = BorderStroke(1.dp, AccentTeal)
                ) {
                    Text("باقتك النشطة حالياً", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SETTINGS TAB SCREEN
// -------------------------------------------------------------
@Composable
fun SettingsTabScreen(
    viewModel: TranslationViewModel,
    cardBgColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    isDarkMode: Boolean
) {
    val activeSubscription by viewModel.subscriptionType.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val currentLangCode by viewModel.appLanguage.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = AppLocalization.get("settings_title"),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
        Text(
            text = AppLocalization.get("settings_desc"),
            fontSize = 11.sp,
            color = secondaryTextColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Subscription details row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AccentTeal)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(AppLocalization.get("active_plan"), fontSize = 14.sp, color = textColor, fontWeight = FontWeight.Bold)
                            Text(AppLocalization.get("plan_update_auto"), fontSize = 11.sp, color = secondaryTextColor)
                        }
                    }
                    Text(activeSubscription.getTitle(currentLangCode), fontSize = 13.sp, color = AccentTeal, fontWeight = FontWeight.Bold)
                }

                Divider(color = textColor.copy(alpha = 0.08f))

                // Toggle dark mode row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NightsStay, contentDescription = null, tint = AccentTeal)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(AppLocalization.get("dark_mode"), fontSize = 14.sp, color = textColor, fontWeight = FontWeight.Bold)
                            Text(AppLocalization.get("dark_mode_desc"), fontSize = 11.sp, color = secondaryTextColor)
                        }
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.isDarkMode.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentTeal, checkedTrackColor = AccentTeal.copy(alpha = 0.5f))
                    )
                }

                Divider(color = textColor.copy(alpha = 0.08f))

                // App Language Selector Row
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = AccentTeal)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(AppLocalization.get("app_language"), fontSize = 14.sp, color = textColor, fontWeight = FontWeight.Bold)
                            Text(AppLocalization.get("app_language_desc"), fontSize = 11.sp, color = secondaryTextColor)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            AppLanguage.AR to "العربية",
                            AppLanguage.EN to "English",
                            AppLanguage.ZH to "中文"
                        ).forEach { (lang, displayName) ->
                            val isSelected = currentLangCode == lang.code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) AccentTeal else textColor.copy(alpha = 0.05f))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) AccentTeal else textColor.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.setAppLanguage(lang.code)
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("lang_select_${lang.code}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayName,
                                    color = if (isSelected) Color.White else textColor,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Divider(color = textColor.copy(alpha = 0.08f))

                // Share app row
                val context = LocalContext.current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val shareBody = when (currentLangCode) {
                                "en" -> "Download Lingo, the smart AI-powered travel translator for text, voice & captured signs! Designed to support all your traveling steps. Share, translate, and travel with confidence!"
                                "zh" -> "下载 Lingo，一款支持文本、语音和标志的人工智能出行翻译工具！旨在支持您所有的旅行步骤。分享、翻译、自信出行！"
                                else -> "حمّل تطبيق Lingo المميز لترجمة النصوص والأصوات وصور اللافتات فوراً بدعم الذكاء الاصطناعي! التطبيق مصمم لتسهيل رحلاتك وتواصلك بكافة اللغات. تواصل، ترجم وسافر بكل ثقة!"
                            }
                            val shareChooserTitle = when (currentLangCode) {
                                "en" -> "Share Lingo via:"
                                "zh" -> "分享 Lingo 通过:"
                                else -> "مشاركة لـ Lingo عبر:"
                            }
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
                                type = "text/plain"
                            }
                            val shareIntent = android.content.Intent.createChooser(sendIntent, shareChooserTitle)
                            context.startActivity(shareIntent)
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = AccentTeal)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(AppLocalization.get("share_app"), fontSize = 14.sp, color = textColor, fontWeight = FontWeight.Bold)
                            Text(AppLocalization.get("share_app_desc"), fontSize = 11.sp, color = secondaryTextColor)
                        }
                    }
                }

                Divider(color = textColor.copy(alpha = 0.08f))

                // About system
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = AccentTeal)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(AppLocalization.get("build_with_flash"), fontSize = 14.sp, color = textColor, fontWeight = FontWeight.Bold)
                        Text(AppLocalization.get("build_with_flash_desc"), fontSize = 11.sp, color = secondaryTextColor)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(14.dp))

        // Card settings for text & display size scaling in real-time
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val currentScaleIndex by viewModel.textScaleIndex.collectAsStateWithLifecycle()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = null,
                        tint = AccentTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppLocalization.get("zoom_title"),
                            fontSize = 14.sp,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = AppLocalization.get("zoom_desc"),
                            fontSize = 11.sp,
                            color = secondaryTextColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                val scaleLabels = when (currentLangCode) {
                    "en" -> listOf("Tiny", "Default", "Medium", "Large", "Very Large", "Huge")
                    "zh" -> listOf("极小", "默认", "中等", "大号", "超大号", "巨无霸")
                    else -> listOf("صغير جداً", "افتراضي", "متوسط", "كبير", "كبير جداً", "ضخم")
                }
                val multipliers = listOf("85%", "100%", "115%", "130%", "145%", "160%")
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${AppLocalization.get("zoom_level")}: ${scaleLabels.getOrNull(currentScaleIndex) ?: ""} (${multipliers.getOrNull(currentScaleIndex) ?: "100%"})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentTeal
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { 
                                    if (currentScaleIndex > 0) {
                                        viewModel.setTextScaleIndex(currentScaleIndex - 1)
                                    }
                                },
                                enabled = currentScaleIndex > 0,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove, 
                                    contentDescription = null, 
                                    tint = if (currentScaleIndex > 0) AccentTeal else secondaryTextColor.copy(alpha = 0.4f), 
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { 
                                    if (currentScaleIndex < scaleLabels.lastIndex) {
                                        viewModel.setTextScaleIndex(currentScaleIndex + 1)
                                    }
                                },
                                enabled = currentScaleIndex < scaleLabels.lastIndex,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add, 
                                    contentDescription = null, 
                                    tint = if (currentScaleIndex < scaleLabels.lastIndex) AccentTeal else secondaryTextColor.copy(alpha = 0.4f), 
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Slider(
                        value = currentScaleIndex.toFloat(),
                        onValueChange = { 
                            viewModel.setTextScaleIndex(it.toInt())
                        },
                        valueRange = 0f..scaleLabels.lastIndex.toFloat(),
                        steps = scaleLabels.size - 2,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentTeal,
                            activeTrackColor = AccentTeal,
                            inactiveTrackColor = textColor.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Live Preview box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(textColor.copy(alpha = 0.05f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = AppLocalization.get("zoom_live_preview"),
                            fontSize = 11.sp,
                            color = textColor,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Guide / Safety details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AccentTeal.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = AppLocalization.get("guide_title"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentTeal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = AppLocalization.get("guide_desc"),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = textColor
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = AppLocalization.get("rights"),
            fontSize = 11.sp,
            color = secondaryTextColor,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}
