package com.example.stepcount.sensor

import android.content.SharedPreferences
import com.example.stepcount.core.util.Constants
import com.example.stepcount.domain.model.DailyStepRecord
import com.example.stepcount.domain.model.Resource
import com.example.stepcount.domain.repository.StepRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying StepDeltaTracker calculation accuracy across
 * standard walk increments, midnight day transitions, and device reboots.
 */
class StepDeltaTrackerTest {

    private lateinit var fakePrefs: FakeSharedPreferences
    private lateinit var fakeRepository: FakeStepRepository
    private lateinit var tracker: StepDeltaTracker

    @Before
    fun setup() {
        fakePrefs = FakeSharedPreferences()
        fakeRepository = FakeStepRepository()
        tracker = StepDeltaTracker(
            prefs = fakePrefs,
            stepRepository = fakeRepository,
            context = null
        )
    }

    @Test
    fun `first time reading initializes hardware counter and returns existing record steps`() = runBlocking {
        fakeRepository.setTodayRecord(DailyStepRecord(id = 1, userId = "u1", date = "2026-08-15", steps = 100L, goal = 8000, isSynced = true))

        val result = tracker.processHardwareReading(currentHardwareSteps = 5000L, todayDate = "2026-08-15")

        assertEquals(100L, result)
        assertEquals(5000L, fakePrefs.getLong(Constants.KEY_LAST_HARDWARE_COUNTER, -1L))
        assertEquals("2026-08-15", fakePrefs.getString(Constants.KEY_LAST_RECORDED_DATE, null))
    }

    @Test
    fun `subsequent reading on same day computes delta and adds to today steps`() = runBlocking {
        // Initial reading establishes baseline
        tracker.processHardwareReading(currentHardwareSteps = 5000L, todayDate = "2026-08-15")

        // User walks 500 steps
        val updatedSteps = tracker.processHardwareReading(currentHardwareSteps = 5500L, todayDate = "2026-08-15")

        assertEquals(500L, updatedSteps)
        assertEquals(5500L, fakePrefs.getLong(Constants.KEY_LAST_HARDWARE_COUNTER, -1L))
        assertEquals(500L, fakeRepository.lastSavedSteps)
    }

    @Test
    fun `midnight crossover allocates delta to new day record without losing morning steps`() = runBlocking {
        // Day 1 at 11:00 PM (timestamp: 1,000,000): reading is at 10,000 steps
        tracker.processHardwareReading(currentHardwareSteps = 10000L, todayDate = "2026-08-14", currentTimeMillis = 1000000L)

        // Day 2 morning at 7:00 AM (8 hours / 28,800,000ms later): user walked 1,200 steps before opening app (counter is at 11,200)
        // Switch fake repository to report no steps for new day 2026-08-15
        fakeRepository.setTodayRecord(null)

        val day2Steps = tracker.processHardwareReading(
            currentHardwareSteps = 11200L,
            todayDate = "2026-08-15",
            currentTimeMillis = 29800000L
        )

        // Delta of 1,200 is preserved and assigned to today
        assertEquals(1200L, day2Steps)
        assertEquals("2026-08-15", fakePrefs.getString(Constants.KEY_LAST_RECORDED_DATE, null))
        assertEquals(11200L, fakePrefs.getLong(Constants.KEY_LAST_HARDWARE_COUNTER, -1L))
        assertEquals(1200L, fakeRepository.lastSavedSteps)
    }

    @Test
    fun `device reboot resets hardware counter and continues tracking from zero`() = runBlocking {
        // Establish baseline before reboot at 8,000
        tracker.processHardwareReading(currentHardwareSteps = 8000L, todayDate = "2026-08-15", currentTimeMillis = 1000000L)
        fakeRepository.setTodayRecord(DailyStepRecord(id = 1, userId = "u1", date = "2026-08-15", steps = 2000L, goal = 8000, isSynced = true))

        // Phone reboots! Hardware counter starts at 0, user walks 150 steps (300 seconds later)
        val stepsAfterReboot = tracker.processHardwareReading(currentHardwareSteps = 150L, todayDate = "2026-08-15", currentTimeMillis = 1300000L)

        // Existing 2000 steps + 150 new steps = 2150 total steps
        assertEquals(2150L, stepsAfterReboot)
        assertEquals(150L, fakePrefs.getLong(Constants.KEY_LAST_HARDWARE_COUNTER, -1L))
        assertEquals(2150L, fakeRepository.lastSavedSteps)
    }

    @Test
    fun `zero delta keeps current steps intact`() = runBlocking {
        tracker.processHardwareReading(currentHardwareSteps = 6000L, todayDate = "2026-08-15")
        fakeRepository.setTodayRecord(DailyStepRecord(id = 1, userId = "u1", date = "2026-08-15", steps = 500L, goal = 8000, isSynced = true))

        val result = tracker.processHardwareReading(currentHardwareSteps = 6000L, todayDate = "2026-08-15")

        assertEquals(500L, result)
    }

    @Test
    fun `app update preserves baseline and correctly adds small walking delta during update downtime without phantom steps`() = runBlocking {
        // App was tracking before update at 85,000 raw steps with 3,500 steps today
        fakePrefs.edit()
            .putLong(Constants.KEY_LAST_HARDWARE_COUNTER, 85000L)
            .putLong(Constants.KEY_LAST_HARDWARE_TIMESTAMP, 1000000L)
            .putString(Constants.KEY_LAST_RECORDED_DATE, "2026-08-15")
            .apply()
        fakeRepository.setTodayRecord(DailyStepRecord(id = 1, userId = "u1", date = "2026-08-15", steps = 3500L, goal = 8000, isSynced = true))

        // New APK version installed: user walked 30 steps during update downtime (counter at 85,030)
        // 60 seconds elapsed
        val updatedSteps = tracker.processHardwareReading(
            currentHardwareSteps = 85030L,
            todayDate = "2026-08-15",
            currentTimeMillis = 1060000L
        )

        // Today's steps should be 3,500 + 30 = 3,530 (NOT 85,030 phantom steps!)
        assertEquals(3530L, updatedSteps)
        assertEquals(85030L, fakePrefs.getLong(Constants.KEY_LAST_HARDWARE_COUNTER, -1L))
        assertEquals(3530L, fakeRepository.lastSavedSteps)
    }

    @Test
    fun `anomaly detection discards impossibly huge step jumps within short window and re-baselines safely`() = runBlocking {
        // Baseline established at 1,000 steps with 500 today steps
        fakePrefs.edit()
            .putLong(Constants.KEY_LAST_HARDWARE_COUNTER, 1000L)
            .putLong(Constants.KEY_LAST_HARDWARE_TIMESTAMP, 1000000L)
            .putString(Constants.KEY_LAST_RECORDED_DATE, "2026-08-15")
            .apply()
        fakeRepository.setTodayRecord(DailyStepRecord(id = 1, userId = "u1", date = "2026-08-15", steps = 500L, goal = 8000, isSynced = true))

        // 10 seconds later, hardware sensor glitches or returns a massive corrupted reading (+50,000 steps)
        val result = tracker.processHardwareReading(
            currentHardwareSteps = 51000L,
            todayDate = "2026-08-15",
            currentTimeMillis = 1010000L
        )

        // Corrupted spike should be discarded, keeping today's steps at 500
        assertEquals(500L, result)
        // Baseline safely updated to 51,000 so subsequent real steps increment from there
        assertEquals(51000L, fakePrefs.getLong(Constants.KEY_LAST_HARDWARE_COUNTER, -1L))
    }

    @Test
    fun `concurrent calls to processHardwareReading produce exact step count without double-counting`() = runBlocking {
        // Initial reading at 2,000
        fakePrefs.edit()
            .putLong(Constants.KEY_LAST_HARDWARE_COUNTER, 2000L)
            .putLong(Constants.KEY_LAST_HARDWARE_TIMESTAMP, 1000000L)
            .putString(Constants.KEY_LAST_RECORDED_DATE, "2026-08-15")
            .apply()
        fakeRepository.setTodayRecord(DailyStepRecord(id = 1, userId = "u1", date = "2026-08-15", steps = 100L, goal = 8000, isSynced = true))

        // First call processes reading at 2,050 (+50 steps)
        val firstResult = tracker.processHardwareReading(
            currentHardwareSteps = 2050L,
            todayDate = "2026-08-15",
            currentTimeMillis = 1020000L
        )
        // Second call (e.g. from UI listener or service) processes the same 2,050 reading
        val secondResult = tracker.processHardwareReading(
            currentHardwareSteps = 2050L,
            todayDate = "2026-08-15",
            currentTimeMillis = 1020005L
        )

        assertEquals(150L, firstResult)
        assertEquals(150L, secondResult)
        assertEquals(150L, fakeRepository.lastSavedSteps)
    }

    @Test
    fun `oem transient zero reading from driver flush is ignored when baseline exists`() = runBlocking {
        // Established baseline on Redmi/Oppo at 4,172,000 with 200 today steps
        fakePrefs.edit()
            .putLong(Constants.KEY_LAST_HARDWARE_COUNTER, 4172000L)
            .putLong(Constants.KEY_LAST_HARDWARE_TIMESTAMP, 1000000L)
            .putString(Constants.KEY_LAST_RECORDED_DATE, "2026-08-15")
            .apply()
        fakeRepository.setTodayRecord(DailyStepRecord(id = 1, userId = "u1", date = "2026-08-15", steps = 200L, goal = 8000, isSynced = true))

        // Sensor hub deep sleep emits a transient 0 reading
        val result = tracker.processHardwareReading(
            currentHardwareSteps = 0L,
            todayDate = "2026-08-15",
            currentTimeMillis = 1010000L
        )

        // Today's steps remain at 200 without being corrupted
        assertEquals(200L, result)
        // Baseline remains intact
        assertEquals(4172000L, fakePrefs.getLong(Constants.KEY_LAST_HARDWARE_COUNTER, -1L))
    }

    @Test
    fun `oem driver drop to lower value does not add raw counter as delta and rebaselines safely`() = runBlocking {
        // Baseline established at 4,172,954
        fakePrefs.edit()
            .putLong(Constants.KEY_LAST_HARDWARE_COUNTER, 4172954L)
            .putLong(Constants.KEY_LAST_HARDWARE_TIMESTAMP, 1000000L)
            .putString(Constants.KEY_LAST_RECORDED_DATE, "2026-08-15")
            .apply()
        fakeRepository.setTodayRecord(DailyStepRecord(id = 1, userId = "u1", date = "2026-08-15", steps = 200L, goal = 8000, isSynced = true))

        // Sensor hub driver drops to 160,240
        val result = tracker.processHardwareReading(
            currentHardwareSteps = 160240L,
            todayDate = "2026-08-15",
            currentTimeMillis = 1050000L
        )

        // Should NOT add 160,240 to today's steps (must stay 200)
        assertEquals(200L, result)
        // Baseline re-aligned to 160,240
        assertEquals(160240L, fakePrefs.getLong(Constants.KEY_LAST_HARDWARE_COUNTER, -1L))
    }

    @Test
    fun `long idle window of 87 minutes rejects 4 million step spike and preserves today steps`() = runBlocking {
        // App at 160,240 with 200 today steps at 6:50 PM
        fakePrefs.edit()
            .putLong(Constants.KEY_LAST_HARDWARE_COUNTER, 160240L)
            .putLong(Constants.KEY_LAST_HARDWARE_TIMESTAMP, 1000000L)
            .putString(Constants.KEY_LAST_RECORDED_DATE, "2026-08-15")
            .apply()
        fakeRepository.setTodayRecord(DailyStepRecord(id = 1, userId = "u1", date = "2026-08-15", steps = 200L, goal = 8000, isSynced = true))

        // 87 minutes (5,220,000 ms) later at 8:17 PM, sensor restores 4,172,954 (+4,012,714 jump)
        val result = tracker.processHardwareReading(
            currentHardwareSteps = 4172954L,
            todayDate = "2026-08-15",
            currentTimeMillis = 6220000L
        )

        // Spurious 4 million jump MUST be rejected, keeping today's steps at 200
        assertEquals(200L, result)
        // Baseline safely updated to 4,172,954
        assertEquals(4172954L, fakePrefs.getLong(Constants.KEY_LAST_HARDWARE_COUNTER, -1L))
    }
}

// Minimal in-memory SharedPreferences fake for unit testing
class FakeSharedPreferences : SharedPreferences {
    private val data = mutableMapOf<String, Any>()

    override fun getAll(): MutableMap<String, *> = data
    override fun getString(key: String?, defValue: String?): String? = data[key] as? String ?: defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = null
    override fun getInt(key: String?, defValue: Int): Int = data[key] as? Int ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = data[key] as? Long ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = data[key] as? Float ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = data[key] as? Boolean ?: defValue
    override fun contains(key: String?): Boolean = data.containsKey(key)
    override fun edit(): SharedPreferences.Editor = FakeEditor(data)
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    class FakeEditor(private val data: MutableMap<String, Any>) : SharedPreferences.Editor {
        private val temp = mutableMapOf<String, Any>()

        override fun putString(key: String?, value: String?): SharedPreferences.Editor { value?.let { temp[key!!] = it }; return this }
        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = this
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor { temp[key!!] = value; return this }
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor { temp[key!!] = value; return this }
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor { temp[key!!] = value; return this }
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor { temp[key!!] = value; return this }
        override fun remove(key: String?): SharedPreferences.Editor { temp.remove(key); return this }
        override fun clear(): SharedPreferences.Editor { temp.clear(); return this }
        override fun commit(): Boolean { data.putAll(temp); return true }
        override fun apply() { data.putAll(temp) }
    }
}

// Minimal in-memory StepRepository fake for unit testing
class FakeStepRepository : StepRepository {
    private val todayFlow = MutableStateFlow<DailyStepRecord?>(null)
    var lastSavedSteps: Long = 0L
    var lastSavedDate: String = ""

    fun setTodayRecord(record: DailyStepRecord?) {
        todayFlow.value = record
        if (record != null) {
            lastSavedSteps = record.steps
            lastSavedDate = record.date
        }
    }

    override fun getTodaySteps(): Flow<DailyStepRecord?> = todayFlow.asStateFlow()

    override fun getAllStepHistory(): Flow<List<DailyStepRecord>> = MutableStateFlow(emptyList())

    override suspend fun saveDailySteps(date: String, steps: Long): Resource<Unit> {
        lastSavedDate = date
        lastSavedSteps = steps
        todayFlow.value = DailyStepRecord(id = 1, userId = "u1", date = date, steps = steps, goal = 8000, isSynced = false)
        return Resource.Success(Unit)
    }

    override suspend fun deleteStepRecord(date: String): Resource<Unit> = Resource.Success(Unit)
    override suspend fun clearAllHistory(): Resource<Unit> = Resource.Success(Unit)
    override suspend fun syncPendingSteps(): Resource<Int> = Resource.Success(0)
    override fun getTotalLifetimeSteps(): Flow<Long> = MutableStateFlow(0L)
}
