package com.example.model

import java.util.UUID

enum class ElementType(val displayName: String, val category: ElementCategory) {
    // Text
    TEXT_HEADING("عنوان رئيسي", ElementCategory.TEXT),
    TEXT_SUBTITLE("عنوان فرعي", ElementCategory.TEXT),
    TEXT_BODY("نص عادي", ElementCategory.TEXT),
    TEXT_CAPTION("نص وصفي صغير", ElementCategory.TEXT),

    // Buttons
    BUTTON_PRIMARY("زر أساسي", ElementCategory.BUTTONS),
    BUTTON_OUTLINED("زر بإطار", ElementCategory.BUTTONS),
    BUTTON_TEXT("زر نصي", ElementCategory.BUTTONS),
    BUTTON_ICON("زر أيقونة", ElementCategory.BUTTONS),
    BUTTON_FAB("زر إجراء عائم (FAB)", ElementCategory.BUTTONS),

    // Inputs
    INPUT_TEXT("حقل إدخال نص", ElementCategory.INPUTS),
    INPUT_SEARCH("شريط بحث", ElementCategory.INPUTS),
    INPUT_PASSWORD("حقل كلمة المرور", ElementCategory.INPUTS),
    INPUT_CHECKBOX("مربع اختيار (Checkbox)", ElementCategory.INPUTS),
    INPUT_RADIO("زر راديو (Radio)", ElementCategory.INPUTS),
    INPUT_SWITCH("مفتاح تبديل (Switch)", ElementCategory.INPUTS),
    INPUT_SLIDER("شريط تمرير (Slider)", ElementCategory.INPUTS),

    // Navigation
    NAV_TOP_BAR("شريط علوي (Top Bar)", ElementCategory.NAVIGATION),
    NAV_BOTTOM("شريط سفلي (Bottom Nav)", ElementCategory.NAVIGATION),
    NAV_TAB_BAR("شريط تبويبات (Tab Bar)", ElementCategory.NAVIGATION),

    // Content
    CONTENT_CARD("بطاقة محتوى (Card)", ElementCategory.CONTENT),
    CONTENT_IMAGE("صورة", ElementCategory.CONTENT),
    CONTENT_AVATAR("صورة شخصية (Avatar)", ElementCategory.CONTENT),
    CONTENT_DIVIDER("فاصل أفقي", ElementCategory.CONTENT),
    CONTENT_SPACER("مسافة فارغة", ElementCategory.CONTENT),
    CONTENT_BADGE("شارة إشعار (Badge)", ElementCategory.CONTENT),
    CONTENT_CHIP("شريحة (Chip)", ElementCategory.CONTENT),
    CONTENT_PROGRESS("شريط تقدم (Progress)", ElementCategory.CONTENT),
    CONTENT_LOADING("مؤشر تحميل (Spinner)", ElementCategory.CONTENT),

    // Shapes
    SHAPE_CONTAINER("حاوية (Container)", ElementCategory.SHAPES),
    SHAPE_RECTANGLE("مستطيل", ElementCategory.SHAPES),
    SHAPE_ROUNDED_RECT("مستطيل مستدير", ElementCategory.SHAPES),
    SHAPE_CIRCLE("دائرة", ElementCategory.SHAPES)
}

enum class ElementCategory(val title: String) {
    TEXT("النصوص"),
    BUTTONS("الأزرار"),
    INPUTS("الإدخال"),
    NAVIGATION("التنقل"),
    CONTENT("المحتوى"),
    SHAPES("الأشكال والحاويات")
}

enum class DeviceFrameStyle(val title: String) {
    NONE("بدون إطار"),
    ANDROID_MODERN("هاتف أندرويد حديث"),
    MODERN_NOTCH("هاتف بإطار دائري"),
    SIMPLE_BORDER("إطار بسيط")
}

enum class BackgroundType {
    SOLID,
    GRADIENT,
    TRANSPARENT,
    IMAGE
}

data class CanvasBackground(
    val type: BackgroundType = BackgroundType.SOLID,
    val colorStart: Long = 0xFFFFFFFF,
    val colorEnd: Long = 0xFFF1F5F9,
    val gradientAngle: Float = 0f,
    val imageUri: String? = null
)

data class PhonePreset(
    val name: String,
    val width: Int,
    val height: Int,
    val description: String
) {
    companion object {
        val PRESETS = listOf(
            PhonePreset("Android Compact", 360, 800, "360 × 800 px"),
            PhonePreset("Android Standard", 360, 780, "360 × 780 px"),
            PhonePreset("iPhone X / 11 Pro", 375, 812, "375 × 812 px"),
            PhonePreset("iPhone 12 / 13 / 14", 390, 844, "390 × 844 px"),
            PhonePreset("iPhone 14 / 15 Pro", 393, 852, "393 × 852 px"),
            PhonePreset("Pixel 7 / 8 / 9 Pro", 412, 915, "412 × 915 px"),
            PhonePreset("iPhone Plus / Max", 414, 896, "414 × 896 px"),
            PhonePreset("Full HD Screen", 1080, 1920, "1080 × 1920 px")
        )
    }
}

data class CanvasElement(
    val id: String = UUID.randomUUID().toString(),
    val type: ElementType,
    val name: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val zIndex: Int = 0,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val variant: String = "Filled", // Filled, Outlined, Rounded, Pill, Gradient, Square, WithIcon, IconOnly

    // Styling
    val backgroundColor: Long = 0xFF4F46E5,
    val backgroundColorSecondary: Long? = null,
    val textColor: Long = 0xFFFFFFFF,
    val borderColor: Long = 0xFFE2E8F0,
    val borderWidth: Float = 0f,
    val borderRadius: Float = 8f,
    val opacity: Float = 1.0f,
    val elevation: Float = 0f,
    val shadowColor: Long = 0x40000000,

    // Typography
    val text: String = "",
    val fontSize: Float = 16f,
    val fontWeight: String = "Normal", // Normal, Medium, Bold
    val textAlign: String = "Center", // Left, Center, Right
    val letterSpacing: Float = 0f,

    // Media & Icons
    val imageUri: String? = null,
    val iconName: String? = null,
    val scaleType: String = "Crop", // Crop, Fit

    // State & Content
    val isChecked: Boolean = false,
    val progress: Float = 0.5f,
    val placeholder: String = "اكتب هنا...",
    val badgeCount: String = "3",
    val items: List<String> = emptyList()
)

data class DesignProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val width: Int = 390,
    val height: Int = 844,
    val presetName: String = "iPhone 12 / 13 / 14",
    val deviceFrame: DeviceFrameStyle = DeviceFrameStyle.ANDROID_MODERN,
    val background: CanvasBackground = CanvasBackground(),
    val elements: List<CanvasElement> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val previewThumbnailPath: String? = null
)
