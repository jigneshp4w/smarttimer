package com.smarttimer.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class WorkflowWithTimers(
    @Embedded val workflow: WorkflowEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "workflow_id"
    )
    val timers: List<TimerEntity>
)
