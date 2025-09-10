package com.task_one.notifyme.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.AlarmManager.AlarmClockInfo
import android.content.Context
import android.content.Intent
import com.task_one.notifyme.data.RepeatRule
import com.task_one.notifyme.MainActivity
import com.task_one.notifyme.data.Task

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(task: Task) {
        val pending = buildPendingIntent(task)
        val triggerAt = task.triggerAtEpochMillis
        try {
            val canExact = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else true
            if (canExact) {
                val showIntent = PendingIntent.getActivity(
                    context,
                    (task.id xor 0x7FFFFFFF).toInt(),
                    Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setAlarmClock(AlarmClockInfo(triggerAt, showIntent), pending)
            } else {
                // Still try to be exact while idle when allowed
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            }
        } catch (_: SecurityException) {
            // Fallback to inexact if exact scheduling is not permitted
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    fun cancel(task: Task) {
        val pending = buildPendingIntent(task)
        alarmManager.cancel(pending)
    }

    private fun buildPendingIntent(task: Task): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TASK_ID, task.id)
            putExtra(ReminderReceiver.EXTRA_TITLE, task.title)
            putExtra(ReminderReceiver.EXTRA_DESC, task.description)
        }
        return PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val INTERVAL_DAY = 24L * 60L * 60L * 1000L
        private const val INTERVAL_WEEK = 7L * INTERVAL_DAY
    }
}


