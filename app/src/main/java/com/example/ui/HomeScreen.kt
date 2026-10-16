package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProjectRepository
import com.example.model.*
import com.example.ui.dialogs.NewProjectDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: ProjectRepository,
    onOpenProject: (DesignProject) -> Unit,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var projects by remember { mutableStateOf<List<DesignProject>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var projectToRename by remember { mutableStateOf<DesignProject?>(null) }
    var newNameText by remember { mutableStateOf("") }
    var projectToDelete by remember { mutableStateOf<DesignProject?>(null) }

    fun refreshProjects() {
        scope.launch {
            projects = repository.getAllProjects()
        }
    }

    LaunchedEffect(Unit) {
        refreshProjects()
    }

    val filteredProjects = if (searchQuery.isBlank()) {
        projects
    } else {
        projects.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFFA855F7))),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PhoneIphone,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ScreenCraft",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "محرر واجهات UI/UX",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Dark / Light Mode Toggle
                    IconButton(onClick = onToggleDarkTheme) {
                        Icon(
                            if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "تبديل المظهر"
                        )
                    }

                    // About / Help Dialog
                    IconButton(onClick = { showAboutDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "معلومات التطبيق")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewProjectDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("مشروع جديد", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث في المشاريع المحفوظة...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hero banner if no projects yet
            if (projects.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.DesignServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "صمّم واجهات هاتفك بصرياً",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "أنشئ واجهات تطبيقات احترافية بسهولة، عدّل الألوان والعناصر، ثم صدّرها كصور جاهزة للمشاركة دون الحاجة لأي كود.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { showNewProjectDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إنشاء أول مشروع")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Projects List Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "مشاريعي (${filteredProjects.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Projects List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredProjects, key = { it.id }) { project ->
                    ProjectCardItem(
                        project = project,
                        onClick = { onOpenProject(project) },
                        onRename = {
                            projectToRename = project
                            newNameText = project.name
                        },
                        onDuplicate = {
                            scope.launch {
                                repository.duplicateProject(project.id)
                                refreshProjects()
                            }
                        },
                        onDelete = {
                            projectToDelete = project
                        }
                    )
                }
            }
        }
    }

    // New Project Dialog
    if (showNewProjectDialog) {
        NewProjectDialog(
            onDismiss = { showNewProjectDialog = false },
            onCreateProject = { name, width, height, presetName ->
                scope.launch {
                    val newProject = DesignProject(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        width = width,
                        height = height,
                        presetName = presetName,
                        elements = listOf(
                            CanvasElement(
                                id = UUID.randomUUID().toString(),
                                type = ElementType.NAV_TOP_BAR,
                                name = "شريط علوي",
                                x = 0f,
                                y = 0f,
                                width = width.toFloat(),
                                height = 56f,
                                text = name,
                                fontSize = 17f,
                                fontWeight = "Bold",
                                backgroundColor = 0xFFFFFFFF,
                                textColor = 0xFF0F172A,
                                zIndex = 0
                            ),
                            CanvasElement(
                                id = UUID.randomUUID().toString(),
                                type = ElementType.TEXT_HEADING,
                                name = "عنوان ترحيبي",
                                x = (width / 2f) - 120f,
                                y = 140f,
                                width = 240f,
                                height = 48f,
                                text = "مرحبًا بك في التطبيق",
                                fontSize = 22f,
                                fontWeight = "Bold",
                                textColor = 0xFF0F172A,
                                zIndex = 1
                            ),
                            CanvasElement(
                                id = UUID.randomUUID().toString(),
                                type = ElementType.BUTTON_PRIMARY,
                                name = "زر رئيسي",
                                x = (width / 2f) - 100f,
                                y = (height - 140f),
                                width = 200f,
                                height = 48f,
                                text = "ابدأ الاستخدام",
                                borderRadius = 12f,
                                backgroundColor = 0xFF4F46E5,
                                textColor = 0xFFFFFFFF,
                                zIndex = 2
                            )
                        )
                    )
                    repository.saveProject(newProject)
                    refreshProjects()
                    onOpenProject(newProject)
                }
            }
        )
    }

    // Rename Dialog
    projectToRename?.let { proj ->
        AlertDialog(
            onDismissRequest = { projectToRename = null },
            title = { Text("إعادة تسمية المشروع") },
            text = {
                OutlinedTextField(
                    value = newNameText,
                    onValueChange = { newNameText = it },
                    singleLine = true,
                    label = { Text("الاسم الجديد") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newNameText.isNotBlank()) {
                        scope.launch {
                            repository.renameProject(proj.id, newNameText.trim())
                            refreshProjects()
                            projectToRename = null
                        }
                    }
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToRename = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    projectToDelete?.let { proj ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("حذف المشروع؟") },
            text = { Text("هل أنت متأكد من حذف مشروع '${proj.name}'؟ لا يمكن التراجع عن هذه الخطوة.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.deleteProject(proj.id)
                            refreshProjects()
                            projectToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // About & Privacy Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("عن تطبيق ScreenCraft", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "محرر بصري تفاعلي لتصميم واجهات تطبيقات الهاتف وتصديرها كصور بدون كتابة أي كود.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(text = "• يعمل محلياً 100% بدون إنترنت وبدون حسابات أو خوادم خارجية.", fontSize = 13.sp)
                    Text(text = "• يستخدم منتقي الصور الحديث (Photo Picker) بدون طلب صلاحيات تخزين واسعة.", fontSize = 13.sp)
                    Text(text = "• يدعم التراجع/الإعادة، الطبقات، والمقاسات القياسية للهواتف.", fontSize = 13.sp)
                    Text(text = "• تصدير بصيغتي PNG و JPG بالأبعاد الأصلية مع أو بدون إطار الهاتف.", fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("فهمت ذلك")
                }
            }
        )
    }
}

@Composable
fun ProjectCardItem(
    project: DesignProject,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault()) }
    val dateStr = remember(project.updatedAt) { dateFormat.format(Date(project.updatedAt)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(width = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon thumbnail with aspect ratio
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF3B82F6))),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Smartphone,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${project.presetName} (${project.width} × ${project.height})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${project.elements.size} عنصر • $dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "خيارات")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("فتح") },
                        leadingIcon = { Icon(Icons.Default.OpenInNew, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("إعادة تسمية") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("إنشاء نسخة") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onDuplicate()
                        }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = { Text("حذف", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
