package com.kokoromi.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kokoromi.domain.model.FieldNote
import com.kokoromi.domain.usecase.GetFieldNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface FieldNotesUiState {
    data object Loading : FieldNotesUiState
    data class Success(val notes: List<FieldNote>) : FieldNotesUiState
    data class Error(val message: String) : FieldNotesUiState
}

@HiltViewModel
class FieldNotesViewModel @Inject constructor(
    getFieldNotes: GetFieldNotesUseCase,
) : ViewModel() {

    val uiState: StateFlow<FieldNotesUiState> = getFieldNotes()
        .map<List<FieldNote>, FieldNotesUiState> { FieldNotesUiState.Success(it) }
        .catch { e -> emit(FieldNotesUiState.Error(e.message ?: "Failed to load notes")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FieldNotesUiState.Loading,
        )
}
