package com.example.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProjectRepository
import com.example.model.*
import com.example.ui.canvas.DesignCanvasViewport
import com.example.ui.components.*
import com.example.ui.dialogs.ExportDialog
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    initialProject: DesignProject,
    repository: ProjectRepository,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var currentProject by remember { mutableStateOf(initialProject) }
    var selectedElementId by remember { mutableStateOf<String?>(null) }
    var showGrid by remember { mutableStateOf(false) }
    var snapToGrid by remember { mutableStateOf(false) }
    var isPreviewMode by remember { mutableStateOf(false) }

    // Canvas zoom & pan state
    var zoomScale by remember { mutableFloatStateOf(0.75f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Bottom sheet & dialog states
    var showAddElementSheet by remember { mutableStateOf(false) }
    var showPropertiesSheet by remember { mutableStateOf(false) }
    var showLayersSheet by remember { mutableStateOf(false) }
    var showBackgroundSheet by remember { mutableStateOf(false) }
    var showFrameSheet by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Undo / Redo Manager
    val undoRedoManager = remember { UndoRedoManager() }

    fun captureSnapshot(): ProjectSnapshot {
        return ProjectSnapshot(
            elements = currentProject.elements,
            background = currentProject.background,
            deviceFrame = currentProject.deviceFrame
        )
    }

    fun mutateProject(action: (DesignProject) -> DesignProject) {
        undoRedoManager.pushState(captureSnapshot())
        val updated = action(currentProject)
        currentProject = updated
        scope.launch {
            repository.saveProject(updated)
        }
    }

    // Auto-save on back press
    BackHandler {
        if (isPreviewMode) {
            isPreviewMode = false
        } else {
            scope.launch {
                repository.saveProject(currentProject)
                onBackToHome()
            }
        }
    }

    // Photo picker for adding image element
    val imageElementPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            mutateProject { proj ->
                val newEl = CanvasElement(
                    id = UUID.randomUUID().toString(),
                    type = ElementType.CONTENT_IMAGE,
                    name = "صورة مضافة",
                    x = (proj.width / 2f) - 100f,
                    y = (proj.height / 2f) - 100f,
                    width = 200f,
                    height = 200f,
                    borderRadius = 16f,
                    imageUri = it.toString(),
                    zIndex = (proj.elements.maxOfOrNull { it.zIndex } ?: 0) + 1
                )
                proj.copy(elements = proj.elements + newEl)
            }
        }
    }

    // Photo picker for background image
    val backgroundImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            mutateProject { proj ->
                proj.copy(
                    background = proj.background.copy(
                        type = BackgroundType.IMAGE,
                        imageUri = it.toString()
                    )
                )
            }
        }
    }

    Scaffold(
        topBar = {
            if (!isPreviewMode) {
                TopAppBar(
                    title = {
                        Text(
                            text = currentProject.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                repository.saveProject(currentProject)
                                onBackToHome()
                            }
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    actions = {
                        // Undo
                        IconButton(
                            onClick = {
                                val prev = undoRedoManager.undo(captureSnapshot())
                                if (prev != null) {
                                    currentProject = currentProject.copy(
                                        elements = prev.elements,
                                        background = prev.background,
                                        deviceFrame = prev.deviceFrame
                                    )
                                    scope.launch { repository.saveProject(currentProject) }
                                }
                            },
                            enabled = undoRedoManager.canUndo
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = "تراجع")
                        }

                        // Redo
                        IconButton(
                            onClick = {
                                val next = undoRedoManager.redo(captureSnapshot())
                                if (next != null) {
                                    currentProject = currentProject.copy(
                                        elements = next.elements,
                                        background = next.background,
                                        deviceFrame = next.deviceFrame
                                    )
                                    scope.launch { repository.saveProject(currentProject) }
                                }
                            },
                            enabled = undoRedoManager.canRedo
                        ) {
                            Icon(Icons.Default.Redo, contentDescription = "إعادة")
                        }

                        // Grid Toggle
                        IconButton(onClick = { showGrid = !showGrid }) {
                            Icon(
                                Icons.Default.GridOn,
                                contentDescription = "الشبكة",
                                tint = if (showGrid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Preview Mode
                        IconButton(onClick = {
                            selectedElementId = null
                            isPreviewMode = true
                        }) {
                            Icon(Icons.Default.Visibility, contentDescription = "معاينة")
                        }

                        // Export Button
                        FilledTonalIconButton(onClick = { showExportDialog = true }) {
                            Icon(Icons.Default.Download, contentDescription = "تصدير")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            if (!isPreviewMode) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        // Add Element
                        NavigationBarItem(
                            selected = false,
                            onClick = { showAddElementSheet = true },
                            icon = { Icon(Icons.Default.AddBox, contentDescription = null) },
                            label = { Text("عنصر") }
                        )

                        // Add Photo from phone
                        NavigationBarItem(
                            selected = false,
                            onClick = {
                                imageElementPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            icon = { Icon(Icons.Default.AddPhotoAlternate, contentDescription = null) },
                            label = { Text("صورة") }
                        )

                        // Layers
                        NavigationBarItem(
                            selected = false,
                            onClick = { showLayersSheet = true },
                            icon = { Icon(Icons.Default.Layers, contentDescription = null) },
                            label = { Text("الطبقات") }
                        )

                        // Background
                        NavigationBarItem(
                            selected = false,
                            onClick = { showBackgroundSheet = true },
                            icon = { Icon(Icons.Default.Palette, contentDescription = null) },
                            label = { Text("الخلفية") }
                        )

                        // Frame
                        NavigationBarItem(
                            selected = false,
                            onClick = { showFrameSheet = true },
                            icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                            label = { Text("الإطار") }
                        )

                        // Properties (highlighted when element is selected)
                        NavigationBarItem(
                            selected = selectedElementId != null,
                            onClick = {
                                if (selectedElementId != null) {
                                    showPropertiesSheet = true
                                }
                            },
                            enabled = selectedElementId != null,
                            icon = { Icon(Icons.Default.Tune, contentDescription = null) },
                            label = { Text("الخصائص") }
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Interactive Viewport with Phone Canvas
            DesignCanvasViewport(
                project = currentProject,
                selectedElementId = selectedElementId,
                onSelectElement = { selectedElementId = it },
                onUpdateElement = { updatedEl ->
                    mutateProject { proj ->
                        proj.copy(
                            elements = proj.elements.map { if (it.id == updatedEl.id) updatedEl else it }
                        )
                    }
                },
                showGrid = showGrid,
                snapToGrid = snapToGrid,
                zoomScale = zoomScale,
                panOffset = panOffset,
                onZoomChange = { zoomScale = it },
                onPanChange = { panOffset = it },
                isPreviewMode = isPreviewMode,
                modifier = Modifier.fillMaxSize()
            )

            // Floating Zoom and Alignment Controls
            if (!isPreviewMode) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Zoom Out
                        IconButton(
                            onClick = { zoomScale = (zoomScale - 0.15f).coerceAtLeast(0.3f) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "تصغير", modifier = Modifier.size(18.dp))
                        }

                        // Zoom Label / Reset
                        TextButton(
                            onClick = {
                                zoomScale = 0.75f
                                panOffset = Offset.Zero
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Text(
                                text = "${(zoomScale * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Zoom In
                        IconButton(
                            onClick = { zoomScale = (zoomScale + 0.15f).coerceAtMost(3.0f) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "تكبير", modifier = Modifier.size(18.dp))
                        }

                        // Snap Toggle
                        IconButton(
                            onClick = { snapToGrid = !snapToGrid },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Grain,
                                contentDescription = "المحاذاة التلقائية",
                                tint = if (snapToGrid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Quick Floating Toolbar when an element is selected
            if (selectedElementId != null && !isPreviewMode) {
                val selectedEl = currentProject.elements.find { it.id == selectedElementId }
                if (selectedEl != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Edit Properties
                            Button(
                                onClick = { showPropertiesSheet = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تعديل الخصائص", fontSize = 13.sp)
                            }

                            // Duplicate
                            IconButton(onClick = {
                                mutateProject { proj ->
                                    val copy = selectedEl.copy(
                                        id = UUID.randomUUID().toString(),
                                        x = selectedEl.x + 16f,
                                        y = selectedEl.y + 16f,
                                        zIndex = (proj.elements.maxOfOrNull { it.zIndex } ?: 0) + 1
                                    )
                                    proj.copy(elements = proj.elements + copy)
                                }
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = MaterialTheme.colorScheme.primary)
                            }

                            // Delete
                            IconButton(onClick = {
                                mutateProject { proj ->
                                    proj.copy(elements = proj.elements.filter { it.id != selectedEl.id })
                                }
                                selectedElementId = null
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Floating Exit Preview Button
            AnimatedVisibility(
                visible = isPreviewMode,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
            ) {
                Button(
                    onClick = { isPreviewMode = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("الخروج من وضع المعاينة", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Add Element Sheet
    if (showAddElementSheet) {
        AddElementBottomSheet(
            onDismiss = { showAddElementSheet = false },
            onAddElement = { type, variant ->
                mutateProject { proj ->
                    val centerX = proj.width / 2f
                    val centerY = proj.height / 2f
                    val maxZ = (proj.elements.maxOfOrNull { it.zIndex } ?: 0) + 1
                    val newElement = ElementDefaults.createElement(type, centerX, centerY, variant).copy(zIndex = maxZ)
                    proj.copy(elements = proj.elements + newElement)
                }
            }
        )
    }

    // Properties Sheet
    if (showPropertiesSheet && selectedElementId != null) {
        val selectedEl = currentProject.elements.find { it.id == selectedElementId }
        if (selectedEl != null) {
            PropertiesBottomSheet(
                element = selectedEl,
                canvasWidth = currentProject.width.toFloat(),
                canvasHeight = currentProject.height.toFloat(),
                onUpdateElement = { updated ->
                    mutateProject { proj ->
                        proj.copy(elements = proj.elements.map { if (it.id == updated.id) updated else it })
                    }
                },
                onDeleteElement = { id ->
                    mutateProject { proj ->
                        proj.copy(elements = proj.elements.filter { it.id != id })
                    }
                    selectedElementId = null
                },
                onDuplicateElement = { el ->
                    mutateProject { proj ->
                        val copy = el.copy(
                            id = UUID.randomUUID().toString(),
                            x = el.x + 16f,
                            y = el.y + 16f,
                            zIndex = (proj.elements.maxOfOrNull { it.zIndex } ?: 0) + 1
                        )
                        proj.copy(elements = proj.elements + copy)
                    }
                },
                onDismiss = { showPropertiesSheet = false }
            )
        }
    }

    // Layers Sheet
    if (showLayersSheet) {
        LayersBottomSheet(
            elements = currentProject.elements,
            selectedElementId = selectedElementId,
            onSelectElement = { selectedElementId = it },
            onToggleVisibility = { id ->
                mutateProject { proj ->
                    proj.copy(elements = proj.elements.map { if (it.id == id) it.copy(isVisible = !it.isVisible) else it })
                }
            },
            onToggleLock = { id ->
                mutateProject { proj ->
                    proj.copy(elements = proj.elements.map { if (it.id == id) it.copy(isLocked = !it.isLocked) else it })
                }
            },
            onMoveUp = { id ->
                mutateProject { proj ->
                    val list = proj.elements.sortedBy { it.zIndex }.toMutableList()
                    val idx = list.indexOfFirst { it.id == id }
                    if (idx < list.size - 1) {
                        val currentZ = list[idx].zIndex
                        val nextZ = list[idx + 1].zIndex
                        list[idx] = list[idx].copy(zIndex = nextZ)
                        list[idx + 1] = list[idx + 1].copy(zIndex = currentZ)
                    }
                    proj.copy(elements = list)
                }
            },
            onMoveDown = { id ->
                mutateProject { proj ->
                    val list = proj.elements.sortedBy { it.zIndex }.toMutableList()
                    val idx = list.indexOfFirst { it.id == id }
                    if (idx > 0) {
                        val currentZ = list[idx].zIndex
                        val prevZ = list[idx - 1].zIndex
                        list[idx] = list[idx].copy(zIndex = prevZ)
                        list[idx - 1] = list[idx - 1].copy(zIndex = currentZ)
                    }
                    proj.copy(elements = list)
                }
            },
            onBringToFront = { id ->
                mutateProject { proj ->
                    val maxZ = (proj.elements.maxOfOrNull { it.zIndex } ?: 0) + 1
                    proj.copy(elements = proj.elements.map { if (it.id == id) it.copy(zIndex = maxZ) else it })
                }
            },
            onSendToBack = { id ->
                mutateProject { proj ->
                    val minZ = (proj.elements.minOfOrNull { it.zIndex } ?: 0) - 1
                    proj.copy(elements = proj.elements.map { if (it.id == id) it.copy(zIndex = minZ) else it })
                }
            },
            onDeleteElement = { id ->
                mutateProject { proj ->
                    proj.copy(elements = proj.elements.filter { it.id != id })
                }
                if (selectedElementId == id) selectedElementId = null
            },
            onDismiss = { showLayersSheet = false }
        )
    }

    // Background Sheet
    if (showBackgroundSheet) {
        BackgroundBottomSheet(
            background = currentProject.background,
            onUpdateBackground = { newBg ->
                mutateProject { it.copy(background = newBg) }
            },
            onPickImage = {
                backgroundImagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onDismiss = { showBackgroundSheet = false }
        )
    }

    // Device Frame Sheet
    if (showFrameSheet) {
        DeviceFrameBottomSheet(
            currentFrame = currentProject.deviceFrame,
            onSelectFrame = { newFrame ->
                mutateProject { it.copy(deviceFrame = newFrame) }
            },
            onDismiss = { showFrameSheet = false }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        ExportDialog(
            project = currentProject,
            onDismiss = { showExportDialog = false },
            onSuccessMessage = { msg ->
                scope.launch {
                    snackbarHostState.showSnackbar(msg)
                }
            }
        )
    }
}
