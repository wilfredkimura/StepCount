package com.example.stepcount.sensor

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.stepcount.MainActivity
import com.example.stepcount.R
import com.example.stepcount.core.di.AppContainer
import com.example.stepcount.core.util.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull

/**
 * Persistent 24/7 Foreground Service that keeps device step sensors actively listening in the background.
 *
 * Why this is essential for reliable tracking:
 * 1. Android OS puts apps into deep sleep and suspends background processes within minutes.
 * 2. By running as a Foreground Service with a low-priority, silent notification, Android
 *    guarantees that our process will not be killed.
 * 3. The low-power hardware step counter sensor (SoC sensor hub) batches steps and delivers
 *    them to this service 24/7 without draining battery.
 * 4. Ensures that even if the user does not open the app for days or weeks, every single step
 *    is accurately recorded in the local Room database.
 */
class StepForegroundService : Service(), SensorEventListener {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var accelerometerDetector: AccelerometerStepDetector? = null

    private lateinit var stepDeltaTracker: StepDeltaTracker
    private lateinit var prefs: SharedPreferences
    private lateinit var appContainer: AppContainer

    private var currentTodaySteps: Long = 0L
    private var currentDailyGoal: Int = Constants.DEFAULT_DAILY_GOAL

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(applicationContext)
        stepDeltaTracker = appContainer.stepDeltaTracker
        prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        createForegroundNotificationChannel()
        startForegroundWithNotification()

        registerStepSensors()
        loadInitialStepCounts()
        MidnightStepRolloverReceiver.scheduleMidnightAlarm(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        // Keep service running and restart automatically if killed by extreme memory pressure
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Registers the low-power hardware step counter or accelerometer fallback.
     */
    private fun registerStepSensors() {
        if (stepCounterSensor != null) {
            // SENSOR_DELAY_UI with max batch reporting latency (10 seconds) for maximum battery preservation
            sensorManager.registerListener(
                this,
                stepCounterSensor,
                SensorManager.SENSOR_DELAY_UI,
                10_000_000 // 10 seconds max batch latency
            )
            android.util.Log.i("StepForegroundService", "24/7 Hardware step counter registered successfully.")
        } else if (accelerometerSensor != null) {
            accelerometerDetector = AccelerometerStepDetector {
                serviceScope.launch {
                    val updated = currentTodaySteps + 1
                    currentTodaySteps = updated
                    appContainer.stepRepository.saveDailySteps(stepDeltaTracker.getTodayDateString(), updated)
                    updateOngoingNotification(updated, currentDailyGoal)
                }
            }
            sensorManager.registerListener(
                accelerometerDetector,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
            android.util.Log.i("StepForegroundService", "24/7 Accelerometer fallback detector registered.")
        }
    }

    private fun loadInitialStepCounts() {
        serviceScope.launch {
            val record = appContainer.stepRepository.getTodaySteps().firstOrNull()
            currentTodaySteps = record?.steps ?: 0L
            currentDailyGoal = record?.goal ?: Constants.DEFAULT_DAILY_GOAL
            updateOngoingNotification(currentTodaySteps, currentDailyGoal)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_STEP_COUNTER) return

        val rawHardwareSteps = event.values[0].toLong()
        serviceScope.launch {
            val updatedSteps = stepDeltaTracker.processHardwareReading(rawHardwareSteps)
            currentTodaySteps = updatedSteps
            updateOngoingNotification(updatedSteps, currentDailyGoal)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action needed
    }

    /**
     * Creates a low-priority notification channel so the ongoing tracking notification
     * is completely silent and does not produce annoying sounds or vibrations.
     */
    private fun createForegroundNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_FOREGROUND_TRACKING,
                Constants.NOTIFICATION_CHANNEL_FOREGROUND_TRACKING_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows continuous live step counts while StepCount is active in the background."
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Starts foreground service with the ongoing live step counter notification.
     */
    private fun startForegroundWithNotification() {
        val notification = buildOngoingNotification(currentTodaySteps, currentDailyGoal)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            } else {
                0
            }
            startForeground(NOTIFICATION_ID_FOREGROUND, notification, foregroundServiceType)
        } else {
            startForeground(NOTIFICATION_ID_FOREGROUND, notification)
        }
    }

    private fun updateOngoingNotification(steps: Long, goal: Int) {
        val notification = buildOngoingNotification(steps, goal)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID_FOREGROUND, notification)
    }

    private fun buildOngoingNotification(steps: Long, goal: Int): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val percentage = if (goal > 0) ((steps.toFloat() / goal.toFloat()) * 100).toInt() else 0
        val formattedSteps = "%,d".format(steps)
        val contentText = if (steps > 0) {
            "$formattedSteps steps today • $percentage% of daily goal"
        } else {
            "Tracking your daily walking steps in background"
        }

        return androidx.core.app.NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_FOREGROUND_TRACKING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("StepCount Active")
            .setContentText(contentText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        accelerometerDetector?.let {
            sensorManager.unregisterListener(it)
        }
        serviceScope.cancel()
        android.util.Log.i("StepForegroundService", "StepForegroundService stopped.")
    }

    companion object {
        const val NOTIFICATION_ID_FOREGROUND = 2001
        const val ACTION_START_SERVICE = "ACTION_START_STEP_TRACKING"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_STEP_TRACKING"

        /**
         * Starts the 24/7 background tracking service safely.
         */
        fun startService(context: Context) {
            val isEnabled = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(Constants.KEY_PERSISTENT_TRACKING_ENABLED, true)

            if (!isEnabled) return

            val intent = Intent(context, StepForegroundService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                android.util.Log.e("StepForegroundService", "Error starting foreground service", e)
            }
        }

        /**
         * Stops the background tracking service.
         */
        fun stopService(context: Context) {
            val intent = Intent(context, StepForegroundService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                android.util.Log.e("StepForegroundService", "Error stopping foreground service", e)
            }
        }
    }
}
