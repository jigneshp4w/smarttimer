package com.smarttimer.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.smarttimer.presentation.configuration.ConfigurationScreen
import com.smarttimer.presentation.configuration.WorkflowDetailScreen
import com.smarttimer.presentation.execution.ExecutionScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Configuration.route) {
            ConfigurationScreen(
                onNavigateToWorkflowDetail = { workflowId ->
                    navController.navigate(Screen.WorkflowDetail.createRoute(workflowId))
                }
            )
        }

        composable(Screen.Execution.route) {
            ExecutionScreen()
        }

        composable(
            route = Screen.WorkflowDetail.route,
            arguments = listOf(
                navArgument("workflowId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val workflowId = backStackEntry.arguments?.getLong("workflowId") ?: return@composable
            WorkflowDetailScreen(
                workflowId = workflowId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
