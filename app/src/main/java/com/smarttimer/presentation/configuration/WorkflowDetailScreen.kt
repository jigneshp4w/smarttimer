package com.smarttimer.presentation.configuration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smarttimer.data.local.entity.TimerEntity
import com.smarttimer.data.local.entity.WorkflowEntity
import com.smarttimer.data.local.entity.WorkflowWithTimers
import com.smarttimer.util.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowDetailScreen(
    workflowId: Long,
    viewModel: ConfigurationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val allWorkflows by viewModel.allWorkflows.collectAsState()
    val workflow = remember(allWorkflows, workflowId) {
        allWorkflows.find { it.workflow.id == workflowId }
    }
    var showAddTimerDialog by remember { mutableStateOf(false) }
    var showEditWorkflowDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workflow?.workflow?.name ?: "Workflow") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditWorkflowDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Edit workflow settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddTimerDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Timer") }
            )
        }
    ) { padding ->
        workflow?.let { wf ->
            if (wf.timers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No timers yet.\nTap + to add one!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(wf.timers, key = { _, timer -> timer.id }) { index, timer ->
                        TimerCard(
                            timer = timer,
                            position = index + 1,
                            onDelete = { viewModel.deleteTimer(timer) }
                        )
                    }
                }
            }

            if (showAddTimerDialog) {
                AddTimerDialog(
                    onDismiss = { showAddTimerDialog = false },
                    onConfirm = { label, minutes, seconds ->
                        val totalSeconds = (minutes * 60) + seconds
                        viewModel.addTimerToWorkflow(
                            workflowId = workflowId,
                            label = label,
                            durationSeconds = totalSeconds,
                            position = wf.timers.size
                        )
                        showAddTimerDialog = false
                    }
                )
            }

            if (showEditWorkflowDialog) {
                EditWorkflowDialog(
                    workflow = wf.workflow,
                    onDismiss = { showEditWorkflowDialog = false },
                    onConfirm = { name, description, alertDuration, ttsEnabled, vibrationEnabled ->
                        viewModel.updateWorkflow(
                            wf.workflow.copy(
                                name = name,
                                description = description,
                                alertDurationSeconds = alertDuration,
                                ttsEnabled = ttsEnabled,
                                vibrationEnabled = vibrationEnabled
                            )
                        )
                        showEditWorkflowDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun TimerCard(
    timer: TimerEntity,
    position: Int,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$position",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Text(
                        text = timer.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = TimeFormatter.formatTime(timer.durationSeconds),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete timer",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddTimerDialog(
    onDismiss: () -> Unit,
    onConfirm: (label: String, minutes: Int, seconds: Int) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var seconds by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Timer") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Timer Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { if (it.all { char -> char.isDigit() }) minutes = it },
                        label = { Text("Minutes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = seconds,
                        onValueChange = { if (it.all { char -> char.isDigit() } && (it.toIntOrNull() ?: 0) < 60) seconds = it },
                        label = { Text("Seconds") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (label.isNotBlank() && (minutes.isNotEmpty() || seconds.isNotEmpty())) {
                        onConfirm(
                            label.trim(),
                            minutes.toIntOrNull() ?: 0,
                            seconds.toIntOrNull() ?: 0
                        )
                    }
                },
                enabled = label.isNotBlank() && (minutes.isNotEmpty() || seconds.isNotEmpty())
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditWorkflowDialog(
    workflow: WorkflowEntity,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String?, alertDuration: Int, ttsEnabled: Boolean, vibrationEnabled: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(workflow.name) }
    var description by remember { mutableStateOf(workflow.description ?: "") }
    var waitMinutes by remember { mutableStateOf((workflow.alertDurationSeconds / 60).toString()) }
    var waitSeconds by remember { mutableStateOf((workflow.alertDurationSeconds % 60).toString()) }
    var ttsEnabled by remember { mutableStateOf(workflow.ttsEnabled) }
    var vibrationEnabled by remember { mutableStateOf(workflow.vibrationEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Workflow") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Workflow Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text(
                        text = "Wait Period Between Tasks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = waitMinutes,
                            onValueChange = {
                                if (it.isEmpty() || (it.all { char -> char.isDigit() } && (it.toIntOrNull() ?: 0) <= 5)) {
                                    waitMinutes = it
                                }
                            },
                            label = { Text("Minutes") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = waitSeconds,
                            onValueChange = {
                                if (it.isEmpty() || (it.all { char -> char.isDigit() } && (it.toIntOrNull() ?: 0) < 60)) {
                                    waitSeconds = it
                                }
                            },
                            label = { Text("Seconds") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = "Maximum: 5 minutes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Voice Announcements",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = ttsEnabled,
                        onCheckedChange = { ttsEnabled = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vibration",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val totalSeconds = ((waitMinutes.toIntOrNull() ?: 0) * 60) +
                                           (waitSeconds.toIntOrNull() ?: 0)
                        val finalDuration = totalSeconds.coerceIn(1, 300)
                        onConfirm(
                            name.trim(),
                            description.trim().takeIf { it.isNotEmpty() },
                            finalDuration,
                            ttsEnabled,
                            vibrationEnabled
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
