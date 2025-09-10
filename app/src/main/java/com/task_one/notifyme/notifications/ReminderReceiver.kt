package com.task_one.notifyme.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.task_one.notifyme.NotifyMeApp
import com.task_one.notifyme.R
import com.task_one.notifyme.data.RepeatRule
import com.task_one.notifyme.data.NotifyMeDatabase
import com.task_one.notifyme.data.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: context.getString(R.string.app_name)
        val description = intent.getStringExtra(EXTRA_DESC)

        val openIntent = Intent(context, com.task_one.notifyme.MainActivity::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotifyMeApp.CHANNEL_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(description ?: context.getString(R.string.app_name))
            .setContentIntent(pendingOpen)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val canNotify = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
        if (canNotify) {
            try {
                with(NotificationManagerCompat.from(context)) {
                    notify(taskId.toInt(), notification)
                }
            } catch (_: SecurityException) { }
        }

        // Reschedule for repeating tasks using setAlarmClock for reliability
        val dao = NotifyMeDatabase.get(context).taskDao()
        GlobalScope.launch(Dispatchers.IO) {
            val task: Task? = dao.getById(taskId)
            task?.let { current ->
                when (current.repeatRule) {
                    RepeatRule.DAILY -> {
                        val nextMask = if (current.repeatDaysMask == 0) 0b1111111 else current.repeatDaysMask
                        val nextMillis = nextSelectedWeekdayMillis(current.triggerAtEpochMillis, nextMask)
                        val next = current.copy(triggerAtEpochMillis = nextMillis, repeatDaysMask = nextMask)
                        dao.upsert(next)
                        AlarmScheduler(context).schedule(next)
                    }
                    RepeatRule.WEEKLY -> {
                        // If days mask provided, schedule next selected weekday, else +7 days
                        val nextMillis = if (current.repeatDaysMask != 0) {
                            nextSelectedWeekdayMillis(current.triggerAtEpochMillis, current.repeatDaysMask)
                        } else current.triggerAtEpochMillis + 7L * 24L * 60L * 60L * 1000L
                        val next = current.copy(triggerAtEpochMillis = nextMillis)
                        dao.upsert(next)
                        AlarmScheduler(context).schedule(next)
                    }
                    else -> Unit
                }
            }
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DESC = "extra_desc"
        private fun nextSelectedWeekdayMillis(fromMillis: Long, mask: Int): Long {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = fromMillis
            for (i in 1..7) {
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                val dow = cal.get(java.util.Calendar.DAY_OF_WEEK) // 1=Sunday..7=Saturday
                val idx = when (dow) {
                    java.util.Calendar.MONDAY -> 0
                    java.util.Calendar.TUESDAY -> 1
                    java.util.Calendar.WEDNESDAY -> 2
                    java.util.Calendar.THURSDAY -> 3
                    java.util.Calendar.FRIDAY -> 4
                    java.util.Calendar.SATURDAY -> 5
                    java.util.Calendar.SUNDAY -> 6
                    else -> 0
                }
                if ((mask and (1 shl idx)) != 0) {
                    return cal.timeInMillis
                }
            }
            return fromMillis + 7L * 24L * 60L * 60L * 1000L
        }
    }
}


