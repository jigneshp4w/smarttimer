package com.smarttimer.presentation.execution

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smarttimer.data.local.entity.WorkflowWithTimers
import com.smarttimer.presentation.components.CircularTimerProgress
import com.smarttimer.service.TimerState
import com.smarttimer.util.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutionScreen(
    viewModel: ExecutionViewModel = hiltViewModel()
) {
    val selectedWorkflow by viewModel.selectedWorkflow.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Execute Workflow") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            when (val state = timerState) {
                is TimerState.Idle -> {
                    WorkflowSelectionContent(
                        viewModel = viewModel,
                        selectedWorkflow = selectedWorkflow,
                        onStartWorkflow = { viewModel.startWorkflow(context) }
                    )
                }
                is TimerState.Running -> {
                    TimerRunningContent(
                        state = state,
                        onPause = { viewModel.pauseTimer() },
                        onStop = { viewModel.stopTimer(context) }
                    )
                }
                is TimerState.Paused -> {
                    TimerPausedContent(
                        onResume = { viewModel.resumeTimer() },
                        onStop = { viewModel.stopTimer(context) }
                    )
                }
                is TimerState.AlertPlaying -> {
                    AlertPlayingContent(state = state)
                }
                is TimerState.Stopped -> {
                    WorkflowCompletedContent(
                        onReset = { viewModel.stopTimer(context) }
                    )
                }
            }
        }
    }
}

@Composable
fun WorkflowSelectionContent(
    viewModel: ExecutionViewModel,
    selectedWorkflow: WorkflowWithTimers?,
    onStartWorkflow: () -> Unit
) {
    val allWorkflows by viewModel.allWorkflows.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Select a Workflow",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (allWorkflows.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No workflows available.\nCreate one in the Configuration tab!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allWorkflows, key = { it.workflow.id }) { workflow ->
                    WorkflowSelectionCard(
                        workflow = workflow,
                        isSelected = selectedWorkflow?.workflow?.id == workflow.workflow.id,
                        onClick = { viewModel.selectWorkflow(workflow.workflow.id) }
                    )
                }
            }

            Button(
                onClick = onStartWorkflow,
                enabled = selectedWorkflow != null && selectedWorkflow.timers.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Workflow")
            }
        }
    }
}

@Composable
fun WorkflowSelectionCard(
    workflow: WorkflowWithTimers,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = workflow.workflow.name,
                style = MaterialTheme.typography.titleLarge,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${workflow.timers.size} timers • Alert: ${workflow.workflow.alertDurationSeconds}s",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TimerRunningContent(
    state: TimerState.Running,
    onPause: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Timer ${state.currentIndex + 1} of ${state.totalTimers}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = state.currentTimer.label,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        CircularTimerProgress(
            progress = state.remainingSeconds.toFloat() / state.totalSeconds.toFloat(),
            remainingSeconds = state.remainingSeconds
        )

        state.nextTimer?.let { next ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Next Up",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = next.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = TimeFormatter.formatTime(next.durationSeconds),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onPause,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(Icons.Default.Pause, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pause")
            }

            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop")
            }
        }
    }
}

@Composable
fun TimerPausedContent(
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Paused",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Resume")
            }

            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop")
            }
        }
    }
}

@Composable
fun AlertPlayingContent(
    state: TimerState.AlertPlaying
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Timer Complete!",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = state.completedTimer.label,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        CircularProgressIndicator()
    }
}

@Composable
fun WorkflowCompletedContent(
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Workflow Complete!",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Button(onClick = onReset) {
            Text("Start Another")
        }
    }
}
