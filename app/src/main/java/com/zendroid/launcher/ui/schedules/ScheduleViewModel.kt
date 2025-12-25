package com.zendroid.launcher.ui.schedules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zendroid.launcher.data.db.AppDatabase
import com.zendroid.launcher.data.db.ScheduleEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val db: AppDatabase
) : ViewModel() {

    val schedules: StateFlow<List<ScheduleEntity>> = db.scheduleDao().getAllSchedulesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addSchedule(name: String, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        viewModelScope.launch {
            val schedule = ScheduleEntity(
                name = name,
                startHour = startHour,
                startMinute = startMinute,
                endHour = endHour,
                endMinute = endMinute,
                daysOfWeek = "1,2,3,4,5,6,7" // Every day by default
            )
            db.scheduleDao().insertSchedule(schedule)
        }
    }

    fun toggleSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            db.scheduleDao().insertSchedule(schedule.copy(isEnabled = !schedule.isEnabled))
        }
    }

    fun deleteSchedule(id: Int) {
        viewModelScope.launch {
            db.scheduleDao().deleteSchedule(id)
        }
    }
}
