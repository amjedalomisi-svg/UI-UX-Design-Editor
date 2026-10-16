package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CanvasElement
import com.example.model.ElementCategory
import com.example.model.ElementType
import com.example.ui.dialogs.ColorPickerDialog
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiesBottomSheet(
    element: CanvasElement,
    canvasWidth: Float,
    canvasHeight: Float,
    onUpdateElement: (CanvasElement) -> Unit,
    onDeleteElement: (String) -> Unit,
    onDuplicateElement: (CanvasElement) -> Unit,
    onDismiss: () -> Unit
) {
    var activeColorPickerTarget by remember { mutableStateOf<String?>(null) } // "bg", "text", "border", "gradient"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Name & Quick Actions (Duplicate & Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = element.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = element.type.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { onDuplicateElement(element) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        onDeleteElement(element.id)
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Alignment Tools Row (Requirement 20)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "محاذاة العنصر على الشاشة", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        IconButton(onClick = { onUpdateElement(element.copy(x = 0f)) }) {
                            Icon(Icons.Default.AlignHorizontalLeft, contentDescription = "يسار")
                        }
                        IconButton(onClick = { onUpdateElement(element.copy(x = (canvasWidth - element.width) / 2f)) }) {
                            Icon(Icons.Default.AlignHorizontalCenter, contentDescription = "وسط أفقي")
                        }
                        IconButton(onClick = { onUpdateElement(element.copy(x = canvasWidth - element.width)) }) {
                            Icon(Icons.Default.AlignHorizontalRight, contentDescription = "يمين")
                        }
                        IconButton(onClick = { onUpdateElement(element.copy(y = 0f)) }) {
                            Icon(Icons.Default.AlignVerticalTop, contentDescription = "أعلى")
                        }
                        IconButton(onClick = { onUpdateElement(element.copy(y = (canvasHeight - element.height) / 2f)) }) {
                            Icon(Icons.Default.AlignVerticalCenter, contentDescription = "وسط عمودي")
                        }
                        IconButton(onClick = { onUpdateElement(element.copy(y = canvasHeight - element.height)) }) {
                            Icon(Icons.Default.AlignVerticalBottom, contentDescription = "أسفل")
                        }
                    }
                }
            }

            // Dimensions: Width & Height
            Text(text = "الأبعاد والموقع", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = element.width.roundToInt().toString(),
                    onValueChange = {
                        val v = it.toFloatOrNull() ?: element.width
                        onUpdateElement(element.copy(width = v.coerceAtLeast(10f)))
                    },
                    label = { Text("العرض (W)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = element.height.roundToInt().toString(),
                    onValueChange = {
                        val v = it.toFloatOrNull() ?: element.height
                        onUpdateElement(element.copy(height = v.coerceAtLeast(10f)))
                    },
                    label = { Text("الارتفاع (H)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            // Position: X & Y
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = element.x.roundToInt().toString(),
                    onValueChange = {
                        val v = it.toFloatOrNull() ?: element.x
                        onUpdateElement(element.copy(x = v))
                    },
                    label = { Text("الموقع الأفقي (X)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = element.y.roundToInt().toString(),
                    onValueChange = {
                        val v = it.toFloatOrNull() ?: element.y
                        onUpdateElement(element.copy(y = v))
                    },
                    label = { Text("الموقع الرأسي (Y)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            // Colors: Background, Text, Border
            Text(text = "الألوان والحدود", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Background Color
                ColorCircleButton(
                    label = "الخلفية",
                    color = Color(element.backgroundColor),
                    onClick = { activeColorPickerTarget = "bg" }
                )

                // Text Color
                if (element.textColor != 0L) {
                    ColorCircleButton(
                        label = "النص",
                        color = Color(element.textColor),
                        onClick = { activeColorPickerTarget = "text" }
                    )
                }

                // Border Color
                ColorCircleButton(
                    label = "الإطار",
                    color = Color(element.borderColor),
                    onClick = { activeColorPickerTarget = "border" }
                )

                // Secondary Gradient Color (if used)
                ColorCircleButton(
                    label = "تدرج 2",
                    color = element.backgroundColorSecondary?.let { Color(it) } ?: Color.Transparent,
                    onClick = { activeColorPickerTarget = "gradient" }
                )
            }

            // Border Width & Radius Sliders
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "استدارة الزوايا (Radius)")
                    Text(text = "${element.borderRadius.roundToInt()} dp", fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = element.borderRadius,
                    onValueChange = { onUpdateElement(element.copy(borderRadius = it)) },
                    valueRange = 0f..50f
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "سمك الإطار (Border Width)")
                    Text(text = "${element.borderWidth.roundToInt()} dp", fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = element.borderWidth,
                    onValueChange = { onUpdateElement(element.copy(borderWidth = it)) },
                    valueRange = 0f..10f
                )
            }

            // Opacity & Shadow Sliders
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "الشفافية (Opacity)")
                    Text(text = "${(element.opacity * 100).roundToInt()}%", fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = element.opacity,
                    onValueChange = { onUpdateElement(element.copy(opacity = it)) },
                    valueRange = 0.05f..1.0f
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "الظل والبروز (Shadow)")
                    Text(text = "${element.elevation.roundToInt()} dp", fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = element.elevation,
                    onValueChange = { onUpdateElement(element.copy(elevation = it)) },
                    valueRange = 0f..20f
                )
            }

            // Text Properties (if element has text)
            if (element.text.isNotEmpty() || element.type.category == ElementCategory.TEXT) {
                Divider()
                Text(text = "تعديل النص", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                OutlinedTextField(
                    value = element.text,
                    onValueChange = { onUpdateElement(element.copy(text = it)) },
                    label = { Text("محتوى النص") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Font Size Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "حجم الخط (Font Size)")
                        Text(text = "${element.fontSize.roundToInt()} sp", fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = element.fontSize,
                        onValueChange = { onUpdateElement(element.copy(fontSize = it)) },
                        valueRange = 10f..48f
                    )
                }

                // Font Weight & Alignment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Weight
                    listOf("Normal", "Medium", "Bold").forEach { weight ->
                        FilterChip(
                            selected = element.fontWeight == weight,
                            onClick = { onUpdateElement(element.copy(fontWeight = weight)) },
                            label = { Text(if (weight == "Bold") "عريض" else if (weight == "Medium") "متوسط" else "عادي") }
                        )
                    }
                }
            }
        }
    }

    // Color Picker Dialog
    activeColorPickerTarget?.let { target ->
        val currentColor = when (target) {
            "bg" -> element.backgroundColor
            "text" -> element.textColor
            "border" -> element.borderColor
            "gradient" -> element.backgroundColorSecondary ?: 0xFF6366F1
            else -> 0xFF4F46E5
        }

        ColorPickerDialog(
            initialColor = currentColor,
            title = when (target) {
                "bg" -> "اختر لون الخلفية"
                "text" -> "اختر لون النص"
                "border" -> "اختر لون الإطار"
                "gradient" -> "اختر لون التدرج الثاني"
                else -> "اختر اللون"
            },
            onDismiss = { activeColorPickerTarget = null },
            onColorSelected = { selectedCol ->
                when (target) {
                    "bg" -> onUpdateElement(element.copy(backgroundColor = selectedCol))
                    "text" -> onUpdateElement(element.copy(textColor = selectedCol))
                    "border" -> onUpdateElement(element.copy(borderColor = selectedCol))
                    "gradient" -> onUpdateElement(element.copy(backgroundColorSecondary = selectedCol))
                }
            }
        )
    }
}

@Composable
fun ColorCircleButton(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color)
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                .clickable { onClick() }
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
