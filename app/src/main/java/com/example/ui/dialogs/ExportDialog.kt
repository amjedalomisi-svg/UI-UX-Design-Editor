package com.example.ui.dialogs

import android.graphics.Bitmap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.export.CanvasBitmapRenderer
import com.example.model.DesignProject
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ExportDialog(
    project: DesignProject,
    onDismiss: () -> Unit,
    onSuccessMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var format by remember { mutableStateOf(Bitmap.CompressFormat.PNG) }
    var scale by remember { mutableFloatStateOf(1.0f) }
    var withFrame by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }

    val outputWidth = ((project.width + (if (withFrame) 64 else 0)) * scale).toInt()
    val outputHeight = ((project.height + (if (withFrame) 96 else 0)) * scale).toInt()

    AlertDialog(
        onDismissRequest = { if (!isExporting) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تصدير التصميم كصورة", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Resolution info card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "أبعاد الصورة المصدرة:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$outputWidth × $outputHeight بكسل",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Format: PNG vs JPG
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "صيغة الصورة:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = format == Bitmap.CompressFormat.PNG,
                            onClick = { format = Bitmap.CompressFormat.PNG },
                            label = { Text("PNG (شفافية وجودة كاملة)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = format == Bitmap.CompressFormat.JPEG,
                            onClick = { format = Bitmap.CompressFormat.JPEG },
                            label = { Text("JPG (حجم ملف أصغر)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Resolution: 1x vs 2x
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "الدقة والجودة:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = scale == 1.0f,
                            onClick = { scale = 1.0f },
                            label = { Text("1x أبعاد أصلية") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = scale == 2.0f,
                            onClick = { scale = 2.0f },
                            label = { Text("2x عالية الدقة (HD)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Frame Option: UI Only vs With Phone Frame
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "إطار الهاتف:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = !withFrame,
                            onClick = { withFrame = false },
                            label = { Text("واجهة فقط (UI Only)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = withFrame,
                            onClick = { withFrame = true },
                            label = { Text("مع إطار الهاتف (Frame)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (isExporting) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("جاري تحويل التصميم إلى صورة...", fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Save to Gallery Button
                Button(
                    onClick = {
                        isExporting = true
                        scope.launch {
                            val bitmap = CanvasBitmapRenderer.renderProjectToBitmap(
                                context = context,
                                project = project,
                                scale = scale,
                                withDeviceFrame = withFrame
                            )
                            val cleanName = project.name.replace(" ", "_") + "_" + System.currentTimeMillis()
                            val result = CanvasBitmapRenderer.saveBitmapToFileAndGallery(
                                context = context,
                                bitmap = bitmap,
                                format = format,
                                fileName = cleanName
                            )
                            isExporting = false
                            onSuccessMessage("تم حفظ التصميم بنجاح في معرض الصور (Pictures/ScreenCraft)!")
                            onDismiss()
                        }
                    },
                    enabled = !isExporting
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ بالمعرض")
                }

                // Share to Apps Button
                OutlinedButton(
                    onClick = {
                        isExporting = true
                        scope.launch {
                            val bitmap = CanvasBitmapRenderer.renderProjectToBitmap(
                                context = context,
                                project = project,
                                scale = scale,
                                withDeviceFrame = withFrame
                            )
                            val cleanName = project.name.replace(" ", "_") + "_" + System.currentTimeMillis()
                            val (_, cacheFile) = CanvasBitmapRenderer.saveBitmapToFileAndGallery(
                                context = context,
                                bitmap = bitmap,
                                format = format,
                                fileName = cleanName
                            )
                            isExporting = false
                            if (cacheFile != null) {
                                val mime = if (format == Bitmap.CompressFormat.PNG) "image/png" else "image/jpeg"
                                CanvasBitmapRenderer.shareExportedImage(context, cacheFile, mime)
                            }
                            onDismiss()
                        }
                    },
                    enabled = !isExporting
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isExporting
            ) {
                Text("إلغاء")
            }
        }
    )
}
