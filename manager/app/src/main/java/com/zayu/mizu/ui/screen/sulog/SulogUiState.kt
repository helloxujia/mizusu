package com.zayu.mizu.ui.screen.sulog

import com.zayu.mizu.ui.util.SulogEntry
import com.zayu.mizu.ui.util.SulogEventFilter
import com.zayu.mizu.ui.util.SulogFile

data class SulogScreenState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val sulogStatus: String = "",
    val isSulogEnabled: Boolean = false,
    val searchText: String = "",
    val selectedFilters: Set<SulogEventFilter> = emptySet(),
    val files: List<SulogFile> = emptyList(),
    val selectedFilePath: String? = null,
    val entries: List<SulogEntry> = emptyList(),
    val visibleEntries: List<SulogEntry> = emptyList(),
    val errorMessage: String? = null,
)

data class SulogActions(
    val onBack: () -> Unit,
    val onRefresh: () -> Unit,
    val onEnableSulog: () -> Unit,
    val onCleanFile: () -> Unit,
    val onSearchTextChange: (String) -> Unit,
    val onToggleFilter: (SulogEventFilter) -> Unit,
    val onSelectFile: (String) -> Unit,
)

data class SulogFileSelector(
    val items: List<String>,
    val selectedIndex: Int,
)
