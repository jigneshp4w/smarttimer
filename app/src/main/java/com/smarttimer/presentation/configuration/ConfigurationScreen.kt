package com.smarttimer.presentation.configuration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smarttimer.presentation.components.WorkflowCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(
    viewModel: ConfigurationViewModel = hiltViewModel(),
    onNavigateToWorkflowDetail: (Long) -> Unit
) {
    val workflows by viewModel.allWorkflows.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workflows") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Workflow") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { padding ->
        if (workflows.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No workflows yet.\nTap + to create one!",
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
                items(workflows, key = { it.workflow.id }) { workflowWithTimers ->
                    WorkflowCard(
                        workflow = workflowWithTimers,
                        onClick = { onNavigateToWorkflowDetail(workflowWithTimers.workflow.id) },
                        onDelete = { viewModel.deleteWorkflow(workflowWithTimers.workflow) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddWorkflowDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, description, alertDuration, ttsEnabled, vibrationEnabled ->
                    viewModel.createWorkflow(name, description, alertDuration, ttsEnabled, vibrationEnabled)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddWorkflowDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String?, alertDuration: Int, ttsEnabled: Boolean, vibrationEnabled: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var waitMinutes by remember { mutableStateOf("0") }
    var waitSeconds by remember { mutableStateOf("3") }
    var ttsEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Workflow") },
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
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
