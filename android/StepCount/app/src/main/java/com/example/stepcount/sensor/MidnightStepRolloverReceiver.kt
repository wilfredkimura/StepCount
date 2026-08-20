package com.example.stepcount.sensor

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.stepcount.core.di.AppContainer
import com.example.stepcount.widget.TodayStepWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * BroadcastReceiver scheduled via AlarmManager to trigger at 00:00:00 every midnight.
 *
 * Why this is crucial:
 * If the user does not open the app for an entire week (or month), this receiver guarantees
 * that each past day (Mon, Tue, Wed... Sun) has its final step count saved as an individual
 * row in the Room database, cleanly rolling over the daily step baseline to 0 for the fresh day.
 */
class MidnightStepRolloverReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        val appContainer = AppContainer(context.applicationContext)

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val calendar = Calendar.getInstance()
                // Date string for today
                val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)

                // Date string for yesterday
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val yesterdayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)

                android.util.Log.i("MidnightStepRollover", "Midnight rollover triggered: Yesterday=$yesterdayDate, Today=$todayDate")

                // Update widget
                TodayStepWidgetReceiver.notifyStepsUpdated(context.applicationContext)

                // Reschedule for next midnight
                scheduleMidnightAlarm(context.applicationContext)
            } catch (e: Exception) {
                android.util.Log.e("MidnightStepRollover", "Error during midnight rollover", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val REQUEST_CODE = 3001

        /**
         * Schedules the midnight rollover alarm for the exact next 00:00:00 timestamp.
         */
        fun scheduleMidnightAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            val intent = Intent(context, MidnightStepRolloverReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 2) // 2 seconds past midnight
                set(Calendar.MILLISECOND, 0)
            }

            val triggerTime = calendar.timeInMillis

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
                android.util.Log.i("MidnightStepRollover", "Next midnight rollover alarm set for: ${calendar.time}")
            } catch (e: Exception) {
                android.util.Log.w("MidnightStepRollover", "Unable to set exact midnight alarm: ${e.message}")
            }
        }
    }
}
