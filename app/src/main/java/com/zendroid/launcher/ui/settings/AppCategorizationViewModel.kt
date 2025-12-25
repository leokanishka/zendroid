package com.zendroid.launcher.ui.settings

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
class AppCategorizationViewModel @Inject constructor(
    private val repository: AppRepository,
    private val settingsRepository: com.zendroid.launcher.data.repository.SettingsRepository,
    private val iconCache: com.zendroid.launcher.util.IconCache
) : ViewModel() {

    fun getIcon(packageName: String) = iconCache.getIcon(packageName)

    private val _zenLevel = MutableStateFlow(settingsRepository.getZenTitrationLevel())
    val zenLevel: StateFlow<com.zendroid.launcher.data.repository.SettingsRepository.ZenTitrationLevel> = _zenLevel

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val appsState: StateFlow<List<AppEntity>> = repository.apps
        .combine(_searchQuery) { apps, query ->
            if (query.isBlank()) {
                apps
            } else {
                apps.filter { it.label.contains(query, ignoreCase = true) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun updateCategory(packageName: String, category: String) {
        viewModelScope.launch {
            repository.updateAppCategory(packageName, category)
        }
    }
}
