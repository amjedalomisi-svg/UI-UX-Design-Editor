package com.example.ui.canvas

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.*
import kotlin.math.roundToInt

@Composable
fun DesignCanvasViewport(
    project: DesignProject,
    selectedElementId: String?,
    onSelectElement: (String?) -> Unit,
    onUpdateElement: (CanvasElement) -> Unit,
    showGrid: Boolean,
    snapToGrid: Boolean,
    zoomScale: Float,
    panOffset: Offset,
    onZoomChange: (Float) -> Unit,
    onPanChange: (Offset) -> Unit,
    isPreviewMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isPreviewMode) Color(0xFF0F172A) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newZoom = (zoomScale * zoom).coerceIn(0.25f, 4.0f)
                    onZoomChange(newZoom)
                    onPanChange(panOffset + pan)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures {
                    onSelectElement(null)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Pannable and Zoomable Container
        Box(
            modifier = Modifier
                .offset { IntOffset(panOffset.x.roundToInt(), panOffset.y.roundToInt()) }
                .width((project.width * zoomScale).dp)
                .height((project.height * zoomScale).dp)
        ) {
            // Phone Frame Outer Container
            DeviceFrameContainer(
                frameStyle = project.deviceFrame,
                zoomScale = zoomScale,
                canvasWidth = project.width.toFloat(),
                canvasHeight = project.height.toFloat()
            ) {
                // The actual design Canvas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(
                            if (project.deviceFrame != DeviceFrameStyle.NONE)
                                RoundedCornerShape((32 * zoomScale).dp)
                            else RoundedCornerShape(0.dp)
                        )
                ) {
                    // Canvas Background
                    CanvasBackgroundView(
                        background = project.background,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Grid overlay (if enabled and not in preview)
                    if (showGrid && !isPreviewMode) {
                        GridOverlay(
                            zoomScale = zoomScale,
                            canvasWidth = project.width.toFloat(),
                            canvasHeight = project.height.toFloat()
                        )
                    }

                    // Render Canvas Elements
                    val sortedElements = project.elements
                        .filter { it.isVisible }
                        .sortedBy { it.zIndex }

                    for (element in sortedElements) {
                        CanvasElementItem(
                            element = element,
                            isSelected = !isPreviewMode && (element.id == selectedElementId),
                            zoomScale = zoomScale,
                            snapToGrid = snapToGrid,
                            onSelect = { onSelectElement(element.id) },
                            onUpdate = onUpdateElement
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CanvasBackgroundView(
    background: CanvasBackground,
    modifier: Modifier = Modifier
) {
    when (background.type) {
        BackgroundType.SOLID -> {
            Box(
                modifier = modifier.background(Color(background.colorStart))
            )
        }
        BackgroundType.GRADIENT -> {
            Box(
                modifier = modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(Color(background.colorStart), Color(background.colorEnd))
                    )
                )
            )
        }
        BackgroundType.TRANSPARENT -> {
            Canvas(modifier = modifier) {
                val checkSize = 16.dp.toPx()
                val cols = (size.width / checkSize).toInt() + 1
                val rows = (size.height / checkSize).toInt() + 1
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val color = if ((r + c) % 2 == 0) Color(0xFFF1F5F9) else Color(0xFFE2E8F0)
                        drawRect(
                            color = color,
                            topLeft = Offset(c * checkSize, r * checkSize),
                            size = Size(checkSize, checkSize)
                        )
                    }
                }
            }
        }
        BackgroundType.IMAGE -> {
            background.imageUri?.let { uriStr ->
                AsyncImage(
                    model = Uri.parse(uriStr),
                    contentDescription = "Canvas Background",
                    contentScale = ContentScale.Crop,
                    modifier = modifier
                )
            } ?: Box(modifier = modifier.background(Color.White))
        }
    }
}

@Composable
fun DeviceFrameContainer(
    frameStyle: DeviceFrameStyle,
    zoomScale: Float,
    canvasWidth: Float,
    canvasHeight: Float,
    content: @Composable () -> Unit
) {
    when (frameStyle) {
        DeviceFrameStyle.NONE -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow((4 * zoomScale).dp)
            ) {
                content()
            }
        }

        DeviceFrameStyle.ANDROID_MODERN -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A), RoundedCornerShape((36 * zoomScale).dp))
                    .border((3 * zoomScale).dp, Color(0xFF334155), RoundedCornerShape((36 * zoomScale).dp))
                    .padding((8 * zoomScale).dp)
            ) {
                content()

                // Punch hole camera at top center
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = (8 * zoomScale).dp)
                        .size((12 * zoomScale).dp)
                        .background(Color(0xFF020617), CircleShape)
                        .border((1 * zoomScale).dp, Color(0xFF1E293B), CircleShape)
                )

                // Gesture pill at bottom center
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = (6 * zoomScale).dp)
                        .width((80 * zoomScale).dp)
                        .height((4 * zoomScale).dp)
                        .background(Color(0xFF94A3B8).copy(alpha = 0.8f), CircleShape)
                )
            }
        }

        DeviceFrameStyle.MODERN_NOTCH -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF18181B), RoundedCornerShape((40 * zoomScale).dp))
                    .border((3.5f * zoomScale).dp, Color(0xFF3F3F46), RoundedCornerShape((40 * zoomScale).dp))
                    .padding((10 * zoomScale).dp)
            ) {
                content()

                // Dynamic Island pill
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = (6 * zoomScale).dp)
                        .width((90 * zoomScale).dp)
                        .height((22 * zoomScale).dp)
                        .background(Color.Black, RoundedCornerShape((11 * zoomScale).dp))
                )

                // Home bar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = (8 * zoomScale).dp)
                        .width((96 * zoomScale).dp)
                        .height((4 * zoomScale).dp)
                        .background(Color(0xFF27272A), CircleShape)
                )
            }
        }

        DeviceFrameStyle.SIMPLE_BORDER -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border((2 * zoomScale).dp, Color(0xFF94A3B8), RoundedCornerShape((16 * zoomScale).dp))
                    .padding((4 * zoomScale).dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun GridOverlay(
    zoomScale: Float,
    canvasWidth: Float,
    canvasHeight: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val step = 16.dp.toPx() * zoomScale
        val gridColor = Color(0x224F46E5)

        // Vertical lines
        var x = 0f
        while (x <= size.width) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += step
        }

        // Horizontal lines
        var y = 0f
        while (y <= size.height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += step
        }
    }
}

@Composable
fun CanvasElementItem(
    element: CanvasElement,
    isSelected: Boolean,
    zoomScale: Float,
    snapToGrid: Boolean,
    onSelect: () -> Unit,
    onUpdate: (CanvasElement) -> Unit
) {
    var dragStartPos by remember { mutableStateOf(Offset.Zero) }

    val snapInterval = 8f

    fun snap(value: Float): Float {
        return if (snapToGrid) (value / snapInterval).roundToInt() * snapInterval else value
    }

    Box(
        modifier = Modifier
            .offset(
                x = (element.x * zoomScale).dp,
                y = (element.y * zoomScale).dp
            )
            .width((element.width * zoomScale).dp)
            .height((element.height * zoomScale).dp)
            .pointerInput(element.id, isSelected) {
                detectTapGestures(
                    onTap = { onSelect() }
                )
            }
            .pointerInput(element.id, isSelected, snapToGrid) {
                if (!element.isLocked) {
                    detectDragGestures(
                        onDragStart = {
                            dragStartPos = Offset(element.x, element.y)
                            onSelect()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newX = snap(element.x + dragAmount.x / zoomScale)
                            val newY = snap(element.y + dragAmount.y / zoomScale)
                            onUpdate(element.copy(x = newX, y = newY))
                        }
                    )
                }
            }
    ) {
        // Visual component appearance
        ElementVisualComponent(element = element, zoomScale = zoomScale)

        // Selection Box & 8 Resize Handles
        if (isSelected) {
            SelectionBorderAndHandles(
                element = element,
                zoomScale = zoomScale,
                snapToGrid = snapToGrid,
                onUpdate = onUpdate
            )
        }
    }
}

@Composable
fun ElementVisualComponent(
    element: CanvasElement,
    zoomScale: Float
) {
    val shape = when {
        element.type == ElementType.SHAPE_CIRCLE || element.type == ElementType.CONTENT_AVATAR || element.type == ElementType.BUTTON_FAB -> CircleShape
        element.borderRadius > 0 -> RoundedCornerShape((element.borderRadius * zoomScale).dp)
        else -> RoundedCornerShape(0.dp)
    }

    val bgModifier = if (element.backgroundColorSecondary != null) {
        Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(element.backgroundColor), Color(element.backgroundColorSecondary))
            ),
            shape = shape
        )
    } else if (element.backgroundColor != 0L) {
        Modifier.background(Color(element.backgroundColor), shape = shape)
    } else {
        Modifier
    }

    val borderModifier = if (element.borderWidth > 0) {
        Modifier.border(
            (element.borderWidth * zoomScale).dp,
            Color(element.borderColor),
            shape = shape
        )
    } else Modifier

    val elevationModifier = if (element.elevation > 0) {
        Modifier.shadow(
            elevation = (element.elevation * zoomScale).dp,
            shape = shape,
            ambientColor = Color(element.shadowColor),
            spotColor = Color(element.shadowColor)
        )
    } else Modifier

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(elevationModifier)
            .then(bgModifier)
            .then(borderModifier)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        when (element.type) {
            ElementType.TEXT_HEADING, ElementType.TEXT_SUBTITLE, ElementType.TEXT_BODY, ElementType.TEXT_CAPTION -> {
                Text(
                    text = element.text,
                    fontSize = (element.fontSize * zoomScale).sp,
                    fontWeight = if (element.fontWeight == "Bold") FontWeight.Bold else if (element.fontWeight == "Medium") FontWeight.Medium else FontWeight.Normal,
                    color = Color(element.textColor),
                    textAlign = when (element.textAlign) {
                        "Left" -> TextAlign.Start
                        "Right" -> TextAlign.End
                        else -> TextAlign.Center
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = (4 * zoomScale).dp)
                )
            }

            ElementType.BUTTON_PRIMARY, ElementType.BUTTON_OUTLINED, ElementType.BUTTON_TEXT -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = (8 * zoomScale).dp)
                ) {
                    if (element.iconName == "add") {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(element.textColor),
                            modifier = Modifier.size((18 * zoomScale).dp)
                        )
                        Spacer(modifier = Modifier.width((4 * zoomScale).dp))
                    }
                    Text(
                        text = element.text,
                        fontSize = (element.fontSize * zoomScale).sp,
                        fontWeight = if (element.fontWeight == "Bold") FontWeight.Bold else FontWeight.Medium,
                        color = Color(element.textColor)
                    )
                }
            }

            ElementType.BUTTON_ICON -> {
                Icon(
                    when (element.iconName) {
                        "search" -> Icons.Default.Search
                        "home" -> Icons.Default.Home
                        "user" -> Icons.Default.Person
                        "settings" -> Icons.Default.Settings
                        "star" -> Icons.Default.Star
                        else -> Icons.Default.Favorite
                    },
                    contentDescription = null,
                    tint = Color(element.textColor),
                    modifier = Modifier.size((22 * zoomScale).dp)
                )
            }

            ElementType.BUTTON_FAB -> {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(element.textColor),
                    modifier = Modifier.size((26 * zoomScale).dp)
                )
            }

            ElementType.INPUT_TEXT, ElementType.INPUT_PASSWORD -> {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = (12 * zoomScale).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (element.iconName == "lock") {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size((16 * zoomScale).dp)
                        )
                        Spacer(modifier = Modifier.width((6 * zoomScale).dp))
                    }
                    Text(
                        text = if (element.text.isNotEmpty()) element.text else element.placeholder,
                        color = if (element.text.isNotEmpty()) Color(element.textColor) else Color(0xFF94A3B8),
                        fontSize = (14 * zoomScale).sp
                    )
                }
            }

            ElementType.INPUT_SEARCH -> {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = (14 * zoomScale).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size((18 * zoomScale).dp)
                    )
                    Spacer(modifier = Modifier.width((8 * zoomScale).dp))
                    Text(
                        text = if (element.text.isNotEmpty()) element.text else element.placeholder,
                        color = Color(0xFF94A3B8),
                        fontSize = (14 * zoomScale).sp
                    )
                }
            }

            ElementType.INPUT_CHECKBOX -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size((20 * zoomScale).dp)
                            .background(
                                if (element.isChecked) Color(0xFF4F46E5) else Color.Transparent,
                                RoundedCornerShape((4 * zoomScale).dp)
                            )
                            .border(
                                (1.5f * zoomScale).dp,
                                if (element.isChecked) Color(0xFF4F46E5) else Color(0xFFCBD5E1),
                                RoundedCornerShape((4 * zoomScale).dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (element.isChecked) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size((14 * zoomScale).dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width((8 * zoomScale).dp))
                    Text(
                        text = element.text,
                        fontSize = (13 * zoomScale).sp,
                        color = Color(element.textColor)
                    )
                }
            }

            ElementType.INPUT_RADIO -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size((20 * zoomScale).dp)
                            .border((2 * zoomScale).dp, if (element.isChecked) Color(0xFF4F46E5) else Color(0xFFCBD5E1), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (element.isChecked) {
                            Box(
                                modifier = Modifier
                                    .size((10 * zoomScale).dp)
                                    .background(Color(0xFF4F46E5), CircleShape)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width((8 * zoomScale).dp))
                    Text(
                        text = element.text,
                        fontSize = (13 * zoomScale).sp,
                        color = Color(element.textColor)
                    )
                }
            }

            ElementType.INPUT_SWITCH -> {
                Box(
                    modifier = Modifier
                        .width((48 * zoomScale).dp)
                        .height((26 * zoomScale).dp)
                        .background(
                            if (element.isChecked) Color(0xFF4F46E5) else Color(0xFFCBD5E1),
                            RoundedCornerShape((13 * zoomScale).dp)
                        )
                        .padding((2 * zoomScale).dp),
                    contentAlignment = if (element.isChecked) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .size((22 * zoomScale).dp)
                            .background(Color.White, CircleShape)
                            .shadow((2 * zoomScale).dp, CircleShape)
                    )
                }
            }

            ElementType.INPUT_SLIDER -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = (6 * zoomScale).dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((6 * zoomScale).dp)
                            .background(Color(0xFFE2E8F0), CircleShape)
                    )
                    // Active Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(element.progress.coerceIn(0f, 1f))
                            .height((6 * zoomScale).dp)
                            .background(Color(0xFF4F46E5), CircleShape)
                    )
                    // Thumb
                    Box(
                        modifier = Modifier
                            .offset(x = ((element.width * element.progress.coerceIn(0f, 1f) - 10) * zoomScale).dp)
                            .size((20 * zoomScale).dp)
                            .background(Color(0xFF4F46E5), CircleShape)
                            .border((2 * zoomScale).dp, Color.White, CircleShape)
                    )
                }
            }

            ElementType.CONTENT_IMAGE -> {
                if (element.imageUri != null) {
                    AsyncImage(
                        model = Uri.parse(element.imageUri),
                        contentDescription = "Image element",
                        contentScale = if (element.scaleType == "Fit") ContentScale.Fit else ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size((32 * zoomScale).dp)
                        )
                        Spacer(modifier = Modifier.height((4 * zoomScale).dp))
                        Text(
                            text = "صورة",
                            color = Color(0xFF64748B),
                            fontSize = (12 * zoomScale).sp
                        )
                    }
                }
            }

            ElementType.CONTENT_AVATAR -> {
                if (element.imageUri != null) {
                    AsyncImage(
                        model = Uri.parse(element.imageUri),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = element.text.take(2),
                        color = Color(element.textColor),
                        fontSize = (element.fontSize * zoomScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            ElementType.CONTENT_CARD -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding((12 * zoomScale).dp)
                ) {
                    Text(
                        text = element.text,
                        fontSize = (15 * zoomScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height((4 * zoomScale).dp))
                    Text(
                        text = element.placeholder,
                        fontSize = (12 * zoomScale).sp,
                        color = Color(0xFF64748B),
                        maxLines = 2
                    )
                }
            }

            ElementType.CONTENT_CHIP -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = (10 * zoomScale).dp)
                ) {
                    if (element.iconName == "check") {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(element.textColor),
                            modifier = Modifier.size((14 * zoomScale).dp)
                        )
                        Spacer(modifier = Modifier.width((4 * zoomScale).dp))
                    }
                    Text(
                        text = element.text,
                        fontSize = (element.fontSize * zoomScale).sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(element.textColor)
                    )
                }
            }

            ElementType.CONTENT_BADGE -> {
                Text(
                    text = element.badgeCount,
                    fontSize = (element.fontSize * zoomScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(element.textColor)
                )
            }

            ElementType.CONTENT_PROGRESS -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(element.progress.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(Color(element.textColor), CircleShape)
                    )
                }
            }

            ElementType.CONTENT_LOADING -> {
                CircularProgressIndicator(
                    color = Color(element.textColor),
                    strokeWidth = (3 * zoomScale).dp,
                    modifier = Modifier.size((28 * zoomScale).dp)
                )
            }

            ElementType.NAV_TOP_BAR -> {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = (12 * zoomScale).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(element.textColor),
                        modifier = Modifier.size((20 * zoomScale).dp)
                    )
                    Text(
                        text = element.text,
                        fontSize = (16 * zoomScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(element.textColor)
                    )
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color(element.textColor),
                        modifier = Modifier.size((20 * zoomScale).dp)
                    )
                }
            }

            ElementType.NAV_BOTTOM -> {
                val items = if (element.items.isNotEmpty()) element.items else listOf("الرئيسية", "البحث", "المفضلة", "حسابي")
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, title ->
                        val isSelected = index == 0
                        val color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF94A3B8)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                when (index) {
                                    0 -> Icons.Default.Home
                                    1 -> Icons.Default.Search
                                    2 -> Icons.Default.Favorite
                                    else -> Icons.Default.Person
                                },
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size((20 * zoomScale).dp)
                            )
                            Spacer(modifier = Modifier.height((2 * zoomScale).dp))
                            Text(
                                text = title,
                                fontSize = (9 * zoomScale).sp,
                                color = color,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            ElementType.NAV_TAB_BAR -> {
                val tabs = if (element.items.isNotEmpty()) element.items else listOf("الرئيسية", "الأحدث", "المميز")
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isSelected = index == 0
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = tab,
                                fontSize = (12 * zoomScale).sp,
                                color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF64748B),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.height((4 * zoomScale).dp))
                                Box(
                                    modifier = Modifier
                                        .width((24 * zoomScale).dp)
                                        .height((2.5f * zoomScale).dp)
                                        .background(Color(0xFF4F46E5), CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            else -> {
                // Shape container / empty
            }
        }
    }
}

@Composable
fun SelectionBorderAndHandles(
    element: CanvasElement,
    zoomScale: Float,
    snapToGrid: Boolean,
    onUpdate: (CanvasElement) -> Unit
) {
    val snapInterval = 8f

    fun snap(value: Float): Float {
        return if (snapToGrid) (value / snapInterval).roundToInt() * snapInterval else value
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .border((1.5f * zoomScale).dp, MaterialTheme.colorScheme.primary)
    ) {
        val handleSize = (14 * zoomScale).coerceAtLeast(10f).dp

        // 4 Corner Handles (Top-Left, Top-Right, Bottom-Left, Bottom-Right)
        ResizeHandle(
            alignment = Alignment.TopStart,
            size = handleSize,
            onDrag = { dx, dy ->
                val newW = (element.width - dx / zoomScale).coerceAtLeast(20f)
                val newH = (element.height - dy / zoomScale).coerceAtLeast(20f)
                val newX = snap(element.x + (element.width - newW))
                val newY = snap(element.y + (element.height - newH))
                onUpdate(element.copy(x = newX, y = newY, width = snap(newW), height = snap(newH)))
            }
        )

        ResizeHandle(
            alignment = Alignment.TopEnd,
            size = handleSize,
            onDrag = { dx, dy ->
                val newW = (element.width + dx / zoomScale).coerceAtLeast(20f)
                val newH = (element.height - dy / zoomScale).coerceAtLeast(20f)
                val newY = snap(element.y + (element.height - newH))
                onUpdate(element.copy(y = newY, width = snap(newW), height = snap(newH)))
            }
        )

        ResizeHandle(
            alignment = Alignment.BottomStart,
            size = handleSize,
            onDrag = { dx, dy ->
                val newW = (element.width - dx / zoomScale).coerceAtLeast(20f)
                val newH = (element.height + dy / zoomScale).coerceAtLeast(20f)
                val newX = snap(element.x + (element.width - newW))
                onUpdate(element.copy(x = newX, width = snap(newW), height = snap(newH)))
            }
        )

        ResizeHandle(
            alignment = Alignment.BottomEnd,
            size = handleSize,
            onDrag = { dx, dy ->
                val newW = (element.width + dx / zoomScale).coerceAtLeast(20f)
                val newH = (element.height + dy / zoomScale).coerceAtLeast(20f)
                onUpdate(element.copy(width = snap(newW), height = snap(newH)))
            }
        )
    }
}

@Composable
fun BoxScope.ResizeHandle(
    alignment: Alignment,
    size: androidx.compose.ui.unit.Dp,
    onDrag: (Float, Float) -> Unit
) {
    Box(
        modifier = Modifier
            .align(alignment)
            .offset(
                x = if (alignment == Alignment.TopStart || alignment == Alignment.BottomStart) (-size / 2) else (size / 2),
                y = if (alignment == Alignment.TopStart || alignment == Alignment.TopEnd) (-size / 2) else (size / 2)
            )
            .size(size)
            .background(Color.White, CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
    )
}
