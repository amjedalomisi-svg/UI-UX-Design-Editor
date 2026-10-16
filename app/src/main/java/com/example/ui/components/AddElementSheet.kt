package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ElementCategory
import com.example.model.ElementDefaults
import com.example.model.ElementType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddElementBottomSheet(
    onDismiss: () -> Unit,
    onAddElement: (ElementType, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(ElementCategory.TEXT) }
    var selectedTypeForVariant by remember { mutableStateOf<ElementType?>(null) }

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
        ) {
            // Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (selectedTypeForVariant != null) "اختر تصميم (${selectedTypeForVariant?.displayName})" else "مكتبة عناصر الواجهة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                if (selectedTypeForVariant != null) {
                    TextButton(onClick = { selectedTypeForVariant = null }) {
                        Text("رجوع للعناصر")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTypeForVariant == null) {
                // Category Tabs
                ScrollableTabRow(
                    selectedTabIndex = ElementCategory.values().indexOf(selectedCategory),
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    ElementCategory.values().forEach { category ->
                        Tab(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            text = { Text(category.title, fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Elements Grid for selected Category
                val categoryElements = ElementType.values().filter { it.category == selectedCategory }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categoryElements) { elementType ->
                        ElementCardItem(
                            elementType = elementType,
                            onClick = {
                                val variants = ElementDefaults.getVariantsFor(elementType)
                                if (variants.size > 1) {
                                    selectedTypeForVariant = elementType
                                } else {
                                    onAddElement(elementType, "Default")
                                    onDismiss()
                                }
                            }
                        )
                    }
                }
            } else {
                // Variant Selection Grid
                val variants = ElementDefaults.getVariantsFor(selectedTypeForVariant!!)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(variants) { variantName ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onAddElement(selectedTypeForVariant!!, variantName)
                                    onDismiss()
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = translateVariant(variantName),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
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
fun ElementCardItem(
    elementType: ElementType,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(width = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = getIconForType(elementType),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = elementType.displayName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

fun getIconForType(type: ElementType): ImageVector {
    return when (type) {
        ElementType.TEXT_HEADING -> Icons.Default.Title
        ElementType.TEXT_SUBTITLE -> Icons.Default.Subtitles
        ElementType.TEXT_BODY -> Icons.Default.TextFields
        ElementType.TEXT_CAPTION -> Icons.Default.FormatSize
        ElementType.BUTTON_PRIMARY -> Icons.Default.SmartButton
        ElementType.BUTTON_OUTLINED -> Icons.Default.CropPortrait
        ElementType.BUTTON_TEXT -> Icons.Default.TextFormat
        ElementType.BUTTON_ICON -> Icons.Default.Favorite
        ElementType.BUTTON_FAB -> Icons.Default.AddCircle
        ElementType.INPUT_TEXT -> Icons.Default.EditNote
        ElementType.INPUT_SEARCH -> Icons.Default.Search
        ElementType.INPUT_PASSWORD -> Icons.Default.Lock
        ElementType.INPUT_CHECKBOX -> Icons.Default.CheckBox
        ElementType.INPUT_RADIO -> Icons.Default.RadioButtonChecked
        ElementType.INPUT_SWITCH -> Icons.Default.ToggleOn
        ElementType.INPUT_SLIDER -> Icons.Default.LinearScale
        ElementType.NAV_TOP_BAR -> Icons.Default.WebAsset
        ElementType.NAV_BOTTOM -> Icons.Default.CallToAction
        ElementType.NAV_TAB_BAR -> Icons.Default.Tab
        ElementType.CONTENT_CARD -> Icons.Default.Dashboard
        ElementType.CONTENT_IMAGE -> Icons.Default.Image
        ElementType.CONTENT_AVATAR -> Icons.Default.AccountCircle
        ElementType.CONTENT_DIVIDER -> Icons.Default.HorizontalRule
        ElementType.CONTENT_SPACER -> Icons.Default.SpaceBar
        ElementType.CONTENT_BADGE -> Icons.Default.Notifications
        ElementType.CONTENT_CHIP -> Icons.Default.Label
        ElementType.CONTENT_PROGRESS -> Icons.Default.TrendingFlat
        ElementType.CONTENT_LOADING -> Icons.Default.Refresh
        ElementType.SHAPE_CONTAINER -> Icons.Default.Layers
        ElementType.SHAPE_RECTANGLE -> Icons.Default.Rectangle
        ElementType.SHAPE_ROUNDED_RECT -> Icons.Default.CropLandscape
        ElementType.SHAPE_CIRCLE -> Icons.Default.Circle
    }
}

fun translateVariant(variant: String): String {
    return when (variant) {
        "Filled" -> "معبأ (Filled)"
        "Outlined" -> "بإطار (Outlined)"
        "Rounded" -> "مستدير (Rounded)"
        "Pill" -> "دائري الأطراف (Pill)"
        "Gradient" -> "متدرج (Gradient)"
        "Square" -> "حاد الزوايا (Square)"
        "WithIcon" -> "مع أيقونة (With Icon)"
        "Underline" -> "خط سفلي (Underline)"
        "Simple" -> "بسيط (Simple)"
        "Elevated" -> "بارز بظل (Elevated)"
        "Standard" -> "قياسي (Standard)"
        "Floating" -> "عائم (Floating)"
        else -> variant
    }
}
