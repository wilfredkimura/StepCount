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
        // Day 1: reading is at 10,000 steps
        tracker.processHardwareReading(currentHardwareSteps = 10000L, todayDate = "2026-08-14")

        // Day 2 morning: user walked 1,200 steps before opening app (counter is at 11,200)
        // Switch fake repository to report no steps for new day 2026-08-15
        fakeRepository.setTodayRecord(null)

        val day2Steps = tracker.processHardwareReading(currentHardwareSteps = 11200L, todayDate = "2026-08-15")

        // Delta of 1,200 is preserved and assigned to today
        assertEquals(1200L, day2Steps)
        assertEquals("2026-08-15", fakePrefs.getString(Constants.KEY_LAST_RECORDED_DATE, null))
        assertEquals(11200L, fakePrefs.getLong(Constants.KEY_LAST_HARDWARE_COUNTER, -1L))
        assertEquals(1200L, fakeRepository.lastSavedSteps)
    }

    @Test
    fun `device reboot resets hardware counter and continues tracking from zero`() = runBlocking {
        // Establish baseline before reboot at 8,000
        tracker.processHardwareReading(currentHardwareSteps = 8000L, todayDate = "2026-08-15")
        fakeRepository.setTodayRecord(DailyStepRecord(id = 1, userId = "u1", date = "2026-08-15", steps = 2000L, goal = 8000, isSynced = true))

        // Phone reboots! Hardware counter starts at 0, user walks 150 steps
        val stepsAfterReboot = tracker.processHardwareReading(currentHardwareSteps = 150L, todayDate = "2026-08-15")

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
