package com.task_one.notifyme.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String?,
    val triggerAtEpochMillis: Long,
    val repeatRule: RepeatRule = RepeatRule.NONE,
    val repeatDaysMask: Int = 0,
    val isEnabled: Boolean = true
)

enum class RepeatRule {
    NONE,
    DAILY,
    WEEKLY
}


