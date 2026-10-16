package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun ColorPickerDialog(
    initialColor: Long,
    title: String = "اختيار اللون",
    onDismiss: () -> Unit,
    onColorSelected: (Long) -> Unit
) {
    val initialInt = initialColor.toInt()
    var red by remember { mutableStateOf(((initialInt shr 16) and 0xFF) / 255f) }
    var green by remember { mutableStateOf(((initialInt shr 8) and 0xFF) / 255f) }
    var blue by remember { mutableStateOf((initialInt and 0xFF) / 255f) }
    var alpha by remember { mutableStateOf(((initialInt shr 24) and 0xFF) / 255f) }

    val currentColor = Color(red, green, blue, alpha)

    val presetColors = listOf(
        0xFF4F46E5, // Indigo
        0xFF6366F1, // Light Indigo
        0xFF0284C7, // Sky Blue
        0xFF06B6D4, // Cyan
        0xFF10B981, // Emerald Green
        0xFF84CC16, // Lime
        0xFFF59E0B, // Amber
        0xFFEF4444, // Red
        0xFFEC4899, // Pink
        0xFF8B5CF6, // Purple
        0xFFA855F7, // Violet
        0xFF0F172A, // Slate Dark
        0xFF334155, // Slate Medium
        0xFF64748B, // Slate Light
        0xFFCBD5E1, // Slate Border
        0xFFF1F5F9, // Slate Ultra Light
        0xFFFFFFFF, // White
        0xFF000000  // Black
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Color Preview Box with Hex code
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(currentColor, RoundedCornerShape(12.dp))
                            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    )

                    val hex = String.format("#%08X", currentColor.toArgb())
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "كود اللون", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = hex, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                // Preset Palette Grid
                Text(text = "ألوان سريعة", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presetColors) { colLong ->
                        val col = Color(colLong)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(1.5.dp, if (currentColor.toArgb() == colLong.toInt()) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                .clickable {
                                    val colInt = colLong.toInt()
                                    red = ((colInt shr 16) and 0xFF) / 255f
                                    green = ((colInt shr 8) and 0xFF) / 255f
                                    blue = (colInt and 0xFF) / 255f
                                    alpha = ((colInt shr 24) and 0xFF) / 255f
                                }
                        )
                    }
                }

                Divider()

                // RGB and Alpha Sliders
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "R", fontWeight = FontWeight.Bold, color = Color.Red, modifier = Modifier.width(20.dp))
                        Slider(
                            value = red,
                            onValueChange = { red = it },
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = "${(red * 255).roundToInt()}", modifier = Modifier.width(36.dp), fontSize = 12.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "G", fontWeight = FontWeight.Bold, color = Color.Green, modifier = Modifier.width(20.dp))
                        Slider(
                            value = green,
                            onValueChange = { green = it },
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = "${(green * 255).roundToInt()}", modifier = Modifier.width(36.dp), fontSize = 12.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "B", fontWeight = FontWeight.Bold, color = Color.Blue, modifier = Modifier.width(20.dp))
                        Slider(
                            value = blue,
                            onValueChange = { blue = it },
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = "${(blue * 255).roundToInt()}", modifier = Modifier.width(36.dp), fontSize = 12.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "A", fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                        Slider(
                            value = alpha,
                            onValueChange = { alpha = it },
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = "${(alpha * 100).roundToInt()}%", modifier = Modifier.width(36.dp), fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val colorLong = (currentColor.toArgb().toLong()) and 0xFFFFFFFFL
                    onColorSelected(colorLong)
                    onDismiss()
                }
            ) {
                Text("تطبيق اللون")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
