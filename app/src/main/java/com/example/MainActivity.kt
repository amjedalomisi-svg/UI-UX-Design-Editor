package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.data.ProjectRepository
import com.example.model.DesignProject
import com.example.ui.EditorScreen
import com.example.ui.HomeScreen
import com.example.ui.theme.ScreenCraftTheme
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    data class Editor(val project: DesignProject) : Screen()
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: ProjectRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = ProjectRepository(applicationContext)

        setContent {
            val systemDark = isSystemInDarkTheme()
            var userDarkTheme by remember { mutableStateOf<Boolean?>(null) }
            val isDark = userDarkTheme ?: systemDark

            var currentScreen by remember { mutableStateOf<Screen?>(null) }

            // Restore last open session or default to Home
            LaunchedEffect(Unit) {
                val lastSessionId = repository.getLastSessionId()
                if (lastSessionId != null) {
                    val lastProject = repository.getProject(lastSessionId)
                    if (lastProject != null) {
                        currentScreen = Screen.Editor(lastProject)
                        return@LaunchedEffect
                    }
                }
                currentScreen = Screen.Home
            }

            ScreenCraftTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (val screen = currentScreen) {
                        null -> {
                            // Loading state
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }

                        is Screen.Home -> {
                            HomeScreen(
                                repository = repository,
                                onOpenProject = { project ->
                                    currentScreen = Screen.Editor(project)
                                },
                                isDarkTheme = isDark,
                                onToggleDarkTheme = {
                                    userDarkTheme = !isDark
                                }
                            )
                        }

                        is Screen.Editor -> {
                            EditorScreen(
                                initialProject = screen.project,
                                repository = repository,
                                onBackToHome = {
                                    currentScreen = Screen.Home
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
