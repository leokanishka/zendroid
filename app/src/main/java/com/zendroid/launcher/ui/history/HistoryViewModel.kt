package com.zendroid.launcher.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zendroid.launcher.data.db.SessionEntity
import com.zendroid.launcher.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val settingsRepository: com.zendroid.launcher.data.repository.SettingsRepository,
    private val iconCache: com.zendroid.launcher.util.IconCache
) : ViewModel() {

    fun getIcon(packageName: String) = iconCache.getIcon(packageName)

    private val _zenLevel = kotlinx.coroutines.flow.MutableStateFlow(settingsRepository.getZenTitrationLevel())
    val zenLevel: StateFlow<com.zendroid.launcher.data.repository.SettingsRepository.ZenTitrationLevel> = _zenLevel

    val historyState: StateFlow<HistoryUiState> = sessionRepository.getAllSessionsFlow()
        .map { sessions ->
            _zenLevel.value = settingsRepository.getZenTitrationLevel()
            if (sessions.isEmpty()) {
                HistoryUiState.Empty
            } else {
                val analytics = calculateAnalytics(sessions)
                HistoryUiState.Success(sessions, analytics)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState.Loading
        )

    private fun calculateAnalytics(sessions: List<SessionEntity>): HistoryAnalytics {
        val reasonCounts = sessions.groupBy { it.reason }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        val totalTime = sessions.sumOf { it.durationMinutes }
        
        return HistoryAnalytics(
            topReasons = reasonCounts,
            totalMindfulMinutes = totalTime
        )
    }
}

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data object Empty : HistoryUiState
    data class Success(
        val sessions: List<SessionEntity>,
        val analytics: HistoryAnalytics
    ) : HistoryUiState
}

data class HistoryAnalytics(
    val topReasons: List<Pair<String, Int>>,
    val totalMindfulMinutes: Int
)
