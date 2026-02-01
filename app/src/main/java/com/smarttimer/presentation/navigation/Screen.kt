package com.smarttimer.presentation.navigation

sealed class Screen(val route: String) {
    object Configuration : Screen("configuration")
    object Execution : Screen("execution")
    object WorkflowDetail : Screen("workflow_detail/{workflowId}") {
        fun createRoute(workflowId: Long) = "workflow_detail/$workflowId"
    }
}
