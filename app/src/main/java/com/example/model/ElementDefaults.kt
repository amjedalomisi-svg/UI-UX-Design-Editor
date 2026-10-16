package com.example.model

import java.util.UUID

object ElementDefaults {

    fun createElement(
        type: ElementType,
        centerX: Float,
        centerY: Float,
        variant: String = "Filled"
    ): CanvasElement {
        val id = UUID.randomUUID().toString()

        return when (type) {
            ElementType.TEXT_HEADING -> CanvasElement(
                id = id,
                type = type,
                name = "عنوان رئيسي",
                x = centerX - 120,
                y = centerY - 25,
                width = 240f,
                height = 50f,
                text = "عنوان رئيسي",
                fontSize = 24f,
                fontWeight = "Bold",
                textAlign = "Center",
                textColor = 0xFF0F172A,
                backgroundColor = 0x00000000
            )

            ElementType.TEXT_SUBTITLE -> CanvasElement(
                id = id,
                type = type,
                name = "عنوان فرعي",
                x = centerX - 100,
                y = centerY - 18,
                width = 200f,
                height = 36f,
                text = "عنوان فرعي للتطبيق",
                fontSize = 18f,
                fontWeight = "Medium",
                textAlign = "Center",
                textColor = 0xFF475569,
                backgroundColor = 0x00000000
            )

            ElementType.TEXT_BODY -> CanvasElement(
                id = id,
                type = type,
                name = "نص عادي",
                x = centerX - 110,
                y = centerY - 15,
                width = 220f,
                height = 30f,
                text = "هذا نص توضيحي داخل الواجهة",
                fontSize = 14f,
                fontWeight = "Normal",
                textAlign = "Center",
                textColor = 0xFF334155,
                backgroundColor = 0x00000000
            )

            ElementType.TEXT_CAPTION -> CanvasElement(
                id = id,
                type = type,
                name = "نص وصفي",
                x = centerX - 80,
                y = centerY - 12,
                width = 160f,
                height = 24f,
                text = "نص وصفي صغير",
                fontSize = 11f,
                fontWeight = "Normal",
                textAlign = "Center",
                textColor = 0xFF94A3B8,
                backgroundColor = 0x00000000
            )

            ElementType.BUTTON_PRIMARY -> {
                when (variant) {
                    "Pill" -> CanvasElement(
                        id = id,
                        type = type,
                        name = "زر دائري الأطراف",
                        x = centerX - 90,
                        y = centerY - 24,
                        width = 180f,
                        height = 48f,
                        variant = variant,
                        text = "متابعة",
                        fontSize = 15f,
                        fontWeight = "Medium",
                        borderRadius = 24f,
                        backgroundColor = 0xFF4F46E5,
                        textColor = 0xFFFFFFFF
                    )
                    "Outlined" -> CanvasElement(
                        id = id,
                        type = type,
                        name = "زر بإطار",
                        x = centerX - 90,
                        y = centerY - 24,
                        width = 180f,
                        height = 48f,
                        variant = variant,
                        text = "إلغاء",
                        fontSize = 15f,
                        fontWeight = "Medium",
                        borderRadius = 10f,
                        borderWidth = 1.5f,
                        borderColor = 0xFF4F46E5,
                        backgroundColor = 0x00000000,
                        textColor = 0xFF4F46E5
                    )
                    "Gradient" -> CanvasElement(
                        id = id,
                        type = type,
                        name = "زر متدرج",
                        x = centerX - 90,
                        y = centerY - 24,
                        width = 180f,
                        height = 48f,
                        variant = variant,
                        text = "ابدأ الآن",
                        fontSize = 15f,
                        fontWeight = "Bold",
                        borderRadius = 12f,
                        backgroundColor = 0xFF6366F1,
                        backgroundColorSecondary = 0xFFA855F7,
                        textColor = 0xFFFFFFFF,
                        elevation = 4f
                    )
                    "Square" -> CanvasElement(
                        id = id,
                        type = type,
                        name = "زر حاد الزوايا",
                        x = centerX - 90,
                        y = centerY - 24,
                        width = 180f,
                        height = 48f,
                        variant = variant,
                        text = "تأكيد",
                        fontSize = 15f,
                        fontWeight = "Bold",
                        borderRadius = 0f,
                        backgroundColor = 0xFF0F172A,
                        textColor = 0xFFFFFFFF
                    )
                    "WithIcon" -> CanvasElement(
                        id = id,
                        type = type,
                        name = "زر مع أيقونة",
                        x = centerX - 100,
                        y = centerY - 24,
                        width = 200f,
                        height = 48f,
                        variant = variant,
                        text = "إضافة جديد",
                        iconName = "add",
                        fontSize = 15f,
                        fontWeight = "Medium",
                        borderRadius = 10f,
                        backgroundColor = 0xFF0284C7,
                        textColor = 0xFFFFFFFF
                    )
                    else -> CanvasElement(
                        id = id,
                        type = type,
                        name = "زر أساسي",
                        x = centerX - 90,
                        y = centerY - 24,
                        width = 180f,
                        height = 48f,
                        variant = "Filled",
                        text = "اضغط هنا",
                        fontSize = 15f,
                        fontWeight = "Medium",
                        borderRadius = 10f,
                        backgroundColor = 0xFF4F46E5,
                        textColor = 0xFFFFFFFF,
                        elevation = 2f
                    )
                }
            }

            ElementType.BUTTON_OUTLINED -> CanvasElement(
                id = id,
                type = type,
                name = "زر محدد",
                x = centerX - 80,
                y = centerY - 22,
                width = 160f,
                height = 44f,
                variant = "Outlined",
                text = "تراجع",
                fontSize = 14f,
                fontWeight = "Medium",
                borderRadius = 8f,
                borderWidth = 1.5f,
                borderColor = 0xFF64748B,
                backgroundColor = 0x00000000,
                textColor = 0xFF334155
            )

            ElementType.BUTTON_TEXT -> CanvasElement(
                id = id,
                type = type,
                name = "زر نصي",
                x = centerX - 60,
                y = centerY - 18,
                width = 120f,
                height = 36f,
                variant = "Text",
                text = "تخطي الخطوة",
                fontSize = 14f,
                fontWeight = "Normal",
                backgroundColor = 0x00000000,
                textColor = 0xFF4F46E5
            )

            ElementType.BUTTON_ICON -> CanvasElement(
                id = id,
                type = type,
                name = "زر أيقونة",
                x = centerX - 24,
                y = centerY - 24,
                width = 48f,
                height = 48f,
                borderRadius = 24f,
                iconName = "heart",
                backgroundColor = 0xFFF1F5F9,
                textColor = 0xFFEF4444
            )

            ElementType.BUTTON_FAB -> CanvasElement(
                id = id,
                type = type,
                name = "زر إجراء عائم (FAB)",
                x = centerX - 28,
                y = centerY - 28,
                width = 56f,
                height = 56f,
                borderRadius = 28f,
                iconName = "add",
                backgroundColor = 0xFF4F46E5,
                textColor = 0xFFFFFFFF,
                elevation = 6f
            )

            ElementType.INPUT_TEXT -> {
                when (variant) {
                    "Underline" -> CanvasElement(
                        id = id,
                        type = type,
                        name = "حقل خط سفلي",
                        x = centerX - 120,
                        y = centerY - 25,
                        width = 240f,
                        height = 50f,
                        variant = variant,
                        placeholder = "اسم المستخدم",
                        text = "",
                        backgroundColor = 0x00000000,
                        borderWidth = 1.5f,
                        borderColor = 0xFF4F46E5,
                        textColor = 0xFF0F172A
                    )
                    "Rounded" -> CanvasElement(
                        id = id,
                        type = type,
                        name = "حقل دائري",
                        x = centerX - 120,
                        y = centerY - 25,
                        width = 240f,
                        height = 50f,
                        variant = variant,
                        placeholder = "البريد الإلكتروني",
                        text = "",
                        borderRadius = 25f,
                        backgroundColor = 0xFFF8FAFC,
                        borderWidth = 1f,
                        borderColor = 0xFFE2E8F0,
                        textColor = 0xFF0F172A
                    )
                    "Filled" -> CanvasElement(
                        id = id,
                        type = type,
                        name = "حقل معبأ",
                        x = centerX - 120,
                        y = centerY - 28,
                        width = 240f,
                        height = 56f,
                        variant = variant,
                        placeholder = "الاسم بالكامل",
                        text = "",
                        borderRadius = 8f,
                        backgroundColor = 0xFFF1F5F9,
                        textColor = 0xFF0F172A
                    )
                    else -> CanvasElement(
                        id = id,
                        type = type,
                        name = "حقل بإطار",
                        x = centerX - 120,
                        y = centerY - 26,
                        width = 240f,
                        height = 52f,
                        variant = "Outlined",
                        placeholder = "أدخل النص هنا...",
                        text = "",
                        borderRadius = 8f,
                        borderWidth = 1f,
                        borderColor = 0xFFCBD5E1,
                        backgroundColor = 0xFFFFFFFF,
                        textColor = 0xFF0F172A
                    )
                }
            }

            ElementType.INPUT_SEARCH -> CanvasElement(
                id = id,
                type = type,
                name = "شريط بحث",
                x = centerX - 130,
                y = centerY - 24,
                width = 260f,
                height = 48f,
                borderRadius = 24f,
                backgroundColor = 0xFFF1F5F9,
                placeholder = "ابحث عن أي شيء...",
                iconName = "search",
                textColor = 0xFF64748B
            )

            ElementType.INPUT_PASSWORD -> CanvasElement(
                id = id,
                type = type,
                name = "حقل كلمة المرور",
                x = centerX - 120,
                y = centerY - 25,
                width = 240f,
                height = 50f,
                placeholder = "••••••••",
                iconName = "lock",
                borderRadius = 8f,
                borderWidth = 1f,
                borderColor = 0xFFCBD5E1,
                backgroundColor = 0xFFFFFFFF,
                textColor = 0xFF0F172A
            )

            ElementType.INPUT_CHECKBOX -> CanvasElement(
                id = id,
                type = type,
                name = "مربع اختيار",
                x = centerX - 80,
                y = centerY - 16,
                width = 160f,
                height = 32f,
                text = "الموافقة على الشروط",
                isChecked = true,
                backgroundColor = 0x00000000,
                textColor = 0xFF1E293B
            )

            ElementType.INPUT_RADIO -> CanvasElement(
                id = id,
                type = type,
                name = "زر راديو",
                x = centerX - 70,
                y = centerY - 16,
                width = 140f,
                height = 32f,
                text = "الخيار الأول",
                isChecked = true,
                backgroundColor = 0x00000000,
                textColor = 0xFF1E293B
            )

            ElementType.INPUT_SWITCH -> CanvasElement(
                id = id,
                type = type,
                name = "مفتاح تبديل",
                x = centerX - 30,
                y = centerY - 16,
                width = 60f,
                height = 32f,
                isChecked = true,
                backgroundColor = 0xFF4F46E5,
                textColor = 0xFFFFFFFF
            )

            ElementType.INPUT_SLIDER -> CanvasElement(
                id = id,
                type = type,
                name = "شريط تمرير",
                x = centerX - 100,
                y = centerY - 15,
                width = 200f,
                height = 30f,
                progress = 0.65f,
                backgroundColor = 0xFFE2E8F0,
                textColor = 0xFF4F46E5
            )

            ElementType.NAV_TOP_BAR -> CanvasElement(
                id = id,
                type = type,
                name = "شريط علوي",
                x = 0f,
                y = 0f,
                width = 390f,
                height = 64f,
                text = "الرئيسية",
                fontSize = 18f,
                fontWeight = "Bold",
                iconName = "arrow_back",
                backgroundColor = 0xFFFFFFFF,
                textColor = 0xFF0F172A,
                elevation = 2f
            )

            ElementType.NAV_BOTTOM -> {
                when (variant) {
                    "Floating" -> CanvasElement(
                        id = id,
                        type = type,
                        name = "شريط سفلي عائم",
                        x = 20f,
                        y = 750f,
                        width = 350f,
                        height = 64f,
                        variant = variant,
                        borderRadius = 32f,
                        backgroundColor = 0xFF0F172A,
                        textColor = 0xFFFFFFFF,
                        elevation = 8f,
                        items = listOf("الرئيسية", "البحث", "المفضلة", "حسابي")
                    )
                    "Rounded" -> CanvasElement(
                        id = id,
                        type = type,
                        name = "شريط سفلي مستدير",
                        x = 0f,
                        y = 760f,
                        width = 390f,
                        height = 70f,
                        variant = variant,
                        borderRadius = 20f,
                        backgroundColor = 0xFFFFFFFF,
                        textColor = 0xFF4F46E5,
                        elevation = 4f,
                        items = listOf("الرئيسية", "الطلبات", "الإشعارات", "الإعدادات")
                    )
                    else -> CanvasElement(
                        id = id,
                        type = type,
                        name = "شريط سفلي قياسي",
                        x = 0f,
                        y = 770f,
                        width = 390f,
                        height = 64f,
                        variant = "Standard",
                        backgroundColor = 0xFFFFFFFF,
                        textColor = 0xFF4F46E5,
                        elevation = 3f,
                        borderWidth = 1f,
                        borderColor = 0xFFF1F5F9,
                        items = listOf("الرئيسية", "البحث", "المفضلة", "الملف")
                    )
                }
            }

            ElementType.NAV_TAB_BAR -> CanvasElement(
                id = id,
                type = type,
                name = "شريط تبويبات",
                x = centerX - 140,
                y = centerY - 24,
                width = 280f,
                height = 48f,
                borderRadius = 8f,
                backgroundColor = 0xFFF1F5F9,
                textColor = 0xFF4F46E5,
                items = listOf("الشائع", "الأحدث", "المميز")
            )

            ElementType.CONTENT_CARD -> {
                when (variant) {
                    "Elevated" -> CanvasElement(
                        id = id,
                        type = type,
                        name = "بطاقة بظل بارز",
                        x = centerX - 120,
                        y = centerY - 70,
                        width = 240f,
                        height = 140f,
                        variant = variant,
                        borderRadius = 16f,
                        backgroundColor = 0xFFFFFFFF,
                        elevation = 6f,
                        text = "عنوان البطاقة",
                        placeholder = "تفاصيل توضيحية للبطاقة ومحتواها التفاعلي الجذاب."
                    )
                    "Outlined" -> CanvasElement(
                        id = id,
                        type = type,
                        name = "بطاقة بإطار",
                        x = centerX - 120,
                        y = centerY - 65,
                        width = 240f,
                        height = 130f,
                        variant = variant,
                        borderRadius = 12f,
                        borderWidth = 1.5f,
                        borderColor = 0xFFCBD5E1,
                        backgroundColor = 0xFFF8FAFC,
                        text = "بطاقة بإطار أنيق",
                        placeholder = "محتوى البطاقة المنظم بتصميم مريح."
                    )
                    else -> CanvasElement(
                        id = id,
                        type = type,
                        name = "بطاقة محتوى",
                        x = centerX - 120,
                        y = centerY - 65,
                        width = 240f,
                        height = 130f,
                        variant = "Simple",
                        borderRadius = 12f,
                        backgroundColor = 0xFFFFFFFF,
                        elevation = 2f,
                        text = "بطاقة منتج أو خدمة",
                        placeholder = "وصف سريع للعنصر المعروض."
                    )
                }
            }

            ElementType.CONTENT_IMAGE -> CanvasElement(
                id = id,
                type = type,
                name = "صورة",
                x = centerX - 100,
                y = centerY - 75,
                width = 200f,
                height = 150f,
                borderRadius = 12f,
                backgroundColor = 0xFFE2E8F0,
                textColor = 0xFF94A3B8
            )

            ElementType.CONTENT_AVATAR -> CanvasElement(
                id = id,
                type = type,
                name = "صورة شخصية (Avatar)",
                x = centerX - 32,
                y = centerY - 32,
                width = 64f,
                height = 64f,
                borderRadius = 32f,
                backgroundColor = 0xFF6366F1,
                textColor = 0xFFFFFFFF,
                text = "أمجد",
                fontSize = 16f,
                fontWeight = "Bold"
            )

            ElementType.CONTENT_DIVIDER -> CanvasElement(
                id = id,
                type = type,
                name = "فاصل أفقي",
                x = centerX - 120,
                y = centerY - 1,
                width = 240f,
                height = 2f,
                backgroundColor = 0xFFE2E8F0
            )

            ElementType.CONTENT_SPACER -> CanvasElement(
                id = id,
                type = type,
                name = "مسافة فارغة",
                x = centerX - 50,
                y = centerY - 15,
                width = 100f,
                height = 30f,
                backgroundColor = 0x1A64748B
            )

            ElementType.CONTENT_BADGE -> CanvasElement(
                id = id,
                type = type,
                name = "شارة إشعار",
                x = centerX - 16,
                y = centerY - 16,
                width = 32f,
                height = 32f,
                borderRadius = 16f,
                backgroundColor = 0xFFEF4444,
                textColor = 0xFFFFFFFF,
                text = "5",
                fontSize = 12f,
                fontWeight = "Bold"
            )

            ElementType.CONTENT_CHIP -> CanvasElement(
                id = id,
                type = type,
                name = "شريحة (Chip)",
                x = centerX - 55,
                y = centerY - 18,
                width = 110f,
                height = 36f,
                borderRadius = 18f,
                backgroundColor = 0xFFEEF2FF,
                textColor = 0xFF4F46E5,
                text = "تصميم UI",
                fontSize = 13f,
                fontWeight = "Medium",
                iconName = "check"
            )

            ElementType.CONTENT_PROGRESS -> CanvasElement(
                id = id,
                type = type,
                name = "شريط تقدم",
                x = centerX - 100,
                y = centerY - 6,
                width = 200f,
                height = 12f,
                borderRadius = 6f,
                progress = 0.70f,
                backgroundColor = 0xFFE2E8F0,
                textColor = 0xFF10B981
            )

            ElementType.CONTENT_LOADING -> CanvasElement(
                id = id,
                type = type,
                name = "مؤشر تحميل",
                x = centerX - 24,
                y = centerY - 24,
                width = 48f,
                height = 48f,
                textColor = 0xFF4F46E5,
                backgroundColor = 0x00000000
            )

            ElementType.SHAPE_CONTAINER -> CanvasElement(
                id = id,
                type = type,
                name = "حاوية مخصصة",
                x = centerX - 100,
                y = centerY - 60,
                width = 200f,
                height = 120f,
                borderRadius = 16f,
                backgroundColor = 0xFFF8FAFC,
                borderWidth = 1f,
                borderColor = 0xFFE2E8F0
            )

            ElementType.SHAPE_RECTANGLE -> CanvasElement(
                id = id,
                type = type,
                name = "مستطيل",
                x = centerX - 80,
                y = centerY - 50,
                width = 160f,
                height = 100f,
                borderRadius = 0f,
                backgroundColor = 0xFF6366F1
            )

            ElementType.SHAPE_ROUNDED_RECT -> CanvasElement(
                id = id,
                type = type,
                name = "مستطيل مستدير",
                x = centerX - 80,
                y = centerY - 50,
                width = 160f,
                height = 100f,
                borderRadius = 20f,
                backgroundColor = 0xFF8B5CF6
            )

            ElementType.SHAPE_CIRCLE -> CanvasElement(
                id = id,
                type = type,
                name = "دائرة",
                x = centerX - 45,
                y = centerY - 45,
                width = 90f,
                height = 90f,
                borderRadius = 45f,
                backgroundColor = 0xFFEC4899
            )
        }
    }

    fun getVariantsFor(type: ElementType): List<String> {
        return when (type) {
            ElementType.BUTTON_PRIMARY -> listOf("Filled", "Outlined", "Rounded", "Pill", "Gradient", "Square", "WithIcon")
            ElementType.INPUT_TEXT -> listOf("Outlined", "Filled", "Underline", "Rounded")
            ElementType.CONTENT_CARD -> listOf("Simple", "Elevated", "Outlined")
            ElementType.NAV_BOTTOM -> listOf("Standard", "Floating", "Rounded")
            else -> listOf("Default")
        }
    }
}
