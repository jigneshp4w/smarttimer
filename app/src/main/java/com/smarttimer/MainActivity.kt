package com.smarttimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smarttimer.presentation.navigation.NavGraph
import com.smarttimer.presentation.navigation.Screen
import com.smarttimer.presentation.theme.SmartTimerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartTimerTheme {
                SmartTimerApp()
            }
        }
    }
}

@Composable
fun SmartTimerApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine if we should show bottom navigation
    val showBottomBar = when (currentRoute) {
        Screen.Configuration.route, Screen.Execution.route -> true
        else -> false
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Configuration") },
                        selected = currentRoute == Screen.Configuration.route,
                        onClick = {
                            navController.navigate(Screen.Configuration.route) {
                                popUpTo(Screen.Configuration.route) { inclusive = true }
                            }
                        }
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                        label = { Text("Execution") },
                        selected = currentRoute == Screen.Execution.route,
                        onClick = {
                            navController.navigate(Screen.Execution.route) {
                                popUpTo(Screen.Configuration.route)
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            NavGraph(
                navController = navController,
                startDestination = Screen.Configuration.route
            )
        }
    }
}
