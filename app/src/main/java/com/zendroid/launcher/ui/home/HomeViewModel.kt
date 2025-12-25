package com.zendroid.launcher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zendroid.launcher.data.db.AppEntity
import com.zendroid.launcher.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository,
    private val sessionRepository: com.zendroid.launcher.data.repository.SessionRepository,
    private val settingsRepository: com.zendroid.launcher.data.repository.SettingsRepository,
    private val iconCache: com.zendroid.launcher.util.IconCache
) : ViewModel() {

    fun getIcon(packageName: String) = iconCache.getIcon(packageName)

    private val _zenLevel = MutableStateFlow(settingsRepository.getZenTitrationLevel())
    val zenLevel: StateFlow<com.zendroid.launcher.data.repository.SettingsRepository.ZenTitrationLevel> = _zenLevel

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    // SSOT for sessions today (Mindful Pauses)
    val mindfulPauses: StateFlow<Int> = sessionRepository.getAllSessionsTodayCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // The SSOT for UI: Combines DB apps with Search Query
    val uiState: StateFlow<HomeUiState> = repository.apps
        .combine(_searchQuery) { apps, query ->
            if (query.isBlank()) {
                HomeUiState.Success(apps)
            } else {
                HomeUiState.Success(
                    apps.filter { it.label.contains(query, ignoreCase = true) }
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState.Loading
        )

    init {
        // Trigger Async Sync on Init
        viewModelScope.launch {
            repository.syncApps()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        // ZenLevel is already reactive via StateFlow, no need to refresh here (HIGH Issue Fix 2)
    }
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val apps: List<AppEntity>) : HomeUiState
}
