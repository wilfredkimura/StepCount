package com.example.stepcount.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.stepcount.MainActivity
import com.example.stepcount.R
import com.example.stepcount.core.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AppWidgetProvider for the Today's Steps Home Screen Widget.
 * Reads today's step count and goal from the local Room database,
 * updates the RemoteViews layout, and handles tap interactions to open the app.
 */
class TodayStepWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Refresh all instances of the widget currently placed on the user's home screen
        updateAllWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TodayStepWidgetReceiver::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)
            if (ids.isNotEmpty()) {
                updateAllWidgets(context, appWidgetManager, ids)
            }
        }
    }

    private fun updateAllWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val appContainer = AppContainer(context.applicationContext)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val formattedDate = SimpleDateFormat("EEE, MMM d", Locale.US).format(Date())

        CoroutineScope(Dispatchers.IO).launch {
            val userId = appContainer.authService.getCurrentUserId() ?: "guest_default"
            val todayRecord = appContainer.database.dailyStepsDao().getStepsForDateOnce(userId, today)
            val userProfile = appContainer.database.userProfileDao().getUserProfileOnce(userId)

            val steps = todayRecord?.steps ?: 0L
            val goal = userProfile?.dailyGoal ?: todayRecord?.goal ?: 10000

            for (widgetId in appWidgetIds) {
                renderWidget(
                    context = context,
                    appWidgetManager = appWidgetManager,
                    appWidgetId = widgetId,
                    steps = steps,
                    goal = goal,
                    dateString = formattedDate
                )
            }
        }
    }

    companion object {

        /**
         * Renders step data, goal progress, and click actions into the RemoteViews layout.
         */
        fun renderWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            steps: Long,
            goal: Int,
            dateString: String
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_today_steps)

            // Calculate progress percentage and calories
            val percentage = if (goal > 0) {
                ((steps.toFloat() / goal) * 100).toInt().coerceAtMost(100)
            } else {
                0
            }
            val calories = (steps * 0.045).toInt()

            // Update text views with formatted values
            views.setTextViewText(R.id.tv_widget_date, dateString)
            views.setTextViewText(R.id.tv_widget_steps, "%,d".format(Locale.US, steps))
            views.setTextViewText(R.id.tv_widget_goal, "Goal: %,d (%d%%)".format(Locale.US, goal, percentage))
            views.setTextViewText(R.id.tv_widget_calories, "🔥 %d kcal".format(Locale.US, calories))
            views.setProgressBar(R.id.pb_widget_progress, 100, percentage, false)

            // Configure tap intent to launch MainActivity (Dashboard screen)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            // Push updated views to the widget manager
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * Static helper function to trigger an immediate update on all active home screen widgets.
         * Call this whenever steps are saved to Room or daily goal is modified.
         */
        fun notifyStepsUpdated(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, TodayStepWidgetReceiver::class.java)
                val ids = appWidgetManager.getAppWidgetIds(componentName)
                if (ids != null && ids.isNotEmpty()) {
                    val intent = Intent(context, TodayStepWidgetReceiver::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                    context.sendBroadcast(intent)
                }
            } catch (e: Exception) {
                // Ignore non-fatal widget update errors
            }
        }
    }
}
