package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BackgroundType
import com.example.model.CanvasBackground
import com.example.ui.dialogs.ColorPickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundBottomSheet(
    background: CanvasBackground,
    onUpdateBackground: (CanvasBackground) -> Unit,
    onPickImage: () -> Unit,
    onDismiss: () -> Unit
) {
    var activeColorPickerTarget by remember { mutableStateOf<String?>(null) } // "solid", "gradStart", "gradEnd"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "خلفية الشاشة (Canvas Background)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            // Background Type Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = background.type == BackgroundType.SOLID,
                    onClick = { onUpdateBackground(background.copy(type = BackgroundType.SOLID)) },
                    label = { Text("لون ثابت") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = background.type == BackgroundType.GRADIENT,
                    onClick = { onUpdateBackground(background.copy(type = BackgroundType.GRADIENT)) },
                    label = { Text("تدرج") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = background.type == BackgroundType.TRANSPARENT,
                    onClick = { onUpdateBackground(background.copy(type = BackgroundType.TRANSPARENT)) },
                    label = { Text("شفافة") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Controls depending on Type
            when (background.type) {
                BackgroundType.SOLID -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "اختر لون الخلفية الثابت:", fontWeight = FontWeight.Medium)
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(background.colorStart))
                                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                .clickable { activeColorPickerTarget = "solid" }
                        )
                    }
                }

                BackgroundType.GRADIENT -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "ألوان التدرج اللوني:", fontWeight = FontWeight.Medium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(background.colorStart))
                                        .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                        .clickable { activeColorPickerTarget = "gradStart" }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("اللون الأول", style = MaterialTheme.typography.bodySmall)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(background.colorEnd))
                                        .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                        .clickable { activeColorPickerTarget = "gradEnd" }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("اللون الثاني", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                BackgroundType.TRANSPARENT -> {
                    Text(
                        text = "الخلفية ستكون شفافة بدون أي لون (مثالية لتصدير العناصر كـ PNG شفاف).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                BackgroundType.IMAGE -> {
                    Text(
                        text = "تم تعيين صورة كخلفية للشاشة.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Divider()

            // Pick background image from phone
            OutlinedButton(
                onClick = {
                    onPickImage()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("اختيار صورة كخلفية من المعرض")
            }
        }
    }

    // Color Picker Dialog
    activeColorPickerTarget?.let { target ->
        val col = when (target) {
            "solid", "gradStart" -> background.colorStart
            "gradEnd" -> background.colorEnd
            else -> 0xFFFFFFFF
        }

        ColorPickerDialog(
            initialColor = col,
            title = when (target) {
                "solid" -> "لون الخلفية"
                "gradStart" -> "لون بداية التدرج"
                else -> "لون نهاية التدرج"
            },
            onDismiss = { activeColorPickerTarget = null },
            onColorSelected = { selected ->
                when (target) {
                    "solid" -> onUpdateBackground(background.copy(colorStart = selected))
                    "gradStart" -> onUpdateBackground(background.copy(colorStart = selected))
                    "gradEnd" -> onUpdateBackground(background.copy(colorEnd = selected))
                }
            }
        )
    }
}
