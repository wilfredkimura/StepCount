package com.example.stepcount.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.stepcount.MainActivity
import com.example.stepcount.R
import com.example.stepcount.core.util.Constants
import com.example.stepcount.domain.model.MotivationalQuote
import com.example.stepcount.domain.model.Resource
import com.example.stepcount.domain.repository.MotivationRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages posting system notifications for daily step goal achievements and custom milestones.
 *
 * This class handles:
 * 1. Creating the high-priority Android Notification Channel.
 * 2. Checking if the user has reached custom milestone percentages (like 50% or 65%) or 100% of their goal.
 * 3. Including inspiring motivational quotes from Quotable API in expandable notification cards.
 * 4. Preventing duplicate notifications on the same calendar day.
 */
class StepNotificationHelper(
    private val context: Context,
    private val prefs: SharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE),
    private val motivationRepository: MotivationRepository? = null
) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    /**
     * Creates the Android Notification Channel required on Android 8.0 (API 26) and above.
     * We give it high importance so that milestone and goal alerts pop up on the screen with sound.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_GOALS,
                Constants.NOTIFICATION_CHANNEL_GOALS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies you when you reach custom milestones and complete your daily walking goals."
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Checks current steps against the daily goal and fires milestone or 100% completion notifications.
     *
     * @param todaySteps The current number of steps walked today.
     * @param goal The user's active daily step goal.
     * @param todayDate The current calendar date in YYYY-MM-DD format.
     */
    suspend fun checkAndNotify(
        todaySteps: Long,
        goal: Int,
        todayDate: String = getTodayDateString()
    ) {
        // Step 1: Verify if notifications are turned on in user settings
        val isEnabled = prefs.getBoolean(Constants.KEY_NOTIFICATIONS_ENABLED, true)
        if (!isEnabled || goal <= 0) {
            return
        }

        // Step 2: Read custom milestone percentage (e.g. 50% or 65%)
        val milestonePercentage = prefs.getInt(
            Constants.KEY_MILESTONE_PERCENTAGE,
            Constants.DEFAULT_MILESTONE_PERCENTAGE
        )

        val lastNotifiedMilestoneDate = prefs.getString(Constants.KEY_LAST_NOTIFIED_DATE_MILESTONE, null)
        val lastNotifiedGoalDate = prefs.getString(Constants.KEY_LAST_NOTIFIED_DATE_GOAL, null)

        // Calculate the step threshold for the milestone
        val milestoneStepTarget = (goal.toLong() * milestonePercentage) / 100L

        // Step 3: Check Custom Milestone Alert (e.g. 50% or 65%)
        if (todaySteps >= milestoneStepTarget &&
            todaySteps < goal &&
            lastNotifiedMilestoneDate != todayDate
        ) {
            val quote = fetchQuoteSafe()
            val title = "🏃 $milestonePercentage% Daily Goal Reached!"
            val shortText = "You've walked %,d / %,d steps ($milestonePercentage%). Keep going!".format(todaySteps, goal)
            val expandedText = "You've walked %,d of %,d steps today ($milestonePercentage% of your daily goal)!\n\n\"%s\"\n— %s"
                .format(todaySteps, goal, quote.quote, quote.author)

            postNotification(
                notificationId = NOTIFICATION_ID_MILESTONE,
                title = title,
                shortText = shortText,
                expandedText = expandedText
            )

            // Remember that we already alerted the user for this milestone today
            prefs.edit().putString(Constants.KEY_LAST_NOTIFIED_DATE_MILESTONE, todayDate).apply()
        }

        // Step 4: Check 100% Goal Completion Celebration Alert
        if (todaySteps >= goal && lastNotifiedGoalDate != todayDate) {
            val quote = fetchQuoteSafe()
            val title = "🎉 Daily Goal Achieved!"
            val shortText = "Awesome job! You hit your %,d step goal today! 🔥".format(goal)
            val expandedText = "Congratulations! You completed your daily goal of %,d steps today! Your walking streak is active 🔥\n\n\"%s\"\n— %s"
                .format(goal, quote.quote, quote.author)

            postNotification(
                notificationId = NOTIFICATION_ID_GOAL,
                title = title,
                shortText = shortText,
                expandedText = expandedText
            )

            // Remember that we already alerted the user for completing their goal today
            prefs.edit().putString(Constants.KEY_LAST_NOTIFIED_DATE_GOAL, todayDate).apply()
        }
    }

    /**
     * Sends a test notification immediately so the user can test their notification settings.
     */
    suspend fun sendTestNotification() {
        val quote = fetchQuoteSafe()
        val milestonePercentage = prefs.getInt(Constants.KEY_MILESTONE_PERCENTAGE, Constants.DEFAULT_MILESTONE_PERCENTAGE)
        val title = "🎯 StepCount Notification Test"
        val shortText = "Milestone alerts are set to $milestonePercentage%. Everything is working!"
        val expandedText = "Goal notifications are enabled with your custom $milestonePercentage% milestone threshold!\n\n\"%s\"\n— %s"
            .format(quote.quote, quote.author)

        postNotification(
            notificationId = NOTIFICATION_ID_TEST,
            title = title,
            shortText = shortText,
            expandedText = expandedText
        )
    }

    /**
     * Safely builds and displays the notification using Android NotificationManagerCompat.
     */
    private fun postNotification(
        notificationId: Int,
        title: String,
        shortText: String,
        expandedText: String
    ) {
        // Android 13+ permission check
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return
        }

        // Intent to open MainActivity when the notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_GOALS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(shortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: Throwable) {
            android.util.Log.e("StepNotificationHelper", "Failed to post notification $notificationId: ${e.message}", e)
        }
    }

    /**
     * Fetches a motivational quote from the Quotable API repository with a reliable fallback.
     */
    private suspend fun fetchQuoteSafe(): MotivationalQuote {
        if (motivationRepository != null) {
            try {
                val result = motivationRepository.getMotivationalQuote()
                if (result is Resource.Success && result.data != null) {
                    return result.data
                }
            } catch (e: Throwable) {
                android.util.Log.w("StepNotificationHelper", "Failed to fetch remote quote, using fallback", e)
            }
        }
        return MotivationalQuote(
            quote = "The secret of getting ahead is getting started.",
            author = "Mark Twain"
        )
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    companion object {
        const val NOTIFICATION_ID_MILESTONE = 1001
        const val NOTIFICATION_ID_GOAL = 1002
        const val NOTIFICATION_ID_TEST = 1003
    }
}
