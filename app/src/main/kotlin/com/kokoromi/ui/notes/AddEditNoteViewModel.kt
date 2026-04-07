package com.kokoromi.ui.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kokoromi.domain.model.FieldNote
import com.kokoromi.domain.usecase.DeleteFieldNoteUseCase
import com.kokoromi.domain.usecase.GetFieldNoteUseCase
import com.kokoromi.domain.usecase.SaveFieldNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

sealed interface AddEditNoteUiState {
    data object Loading : AddEditNoteUiState
    data object Editing : AddEditNoteUiState
    data object Saved : AddEditNoteUiState
    data object Deleted : AddEditNoteUiState
    data class Error(val message: String) : AddEditNoteUiState
}

data class AddEditNoteForm(
    val content: String = "",
    val createdAt: Instant? = null,
)

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getFieldNote: GetFieldNoteUseCase,
    private val saveFieldNote: SaveFieldNoteUseCase,
    private val deleteFieldNote: DeleteFieldNoteUseCase,
) : ViewModel() {

    private val noteId: String? = savedStateHandle["noteId"]
    private var existingNote: FieldNote? = null

    val isNew: Boolean = noteId == null

    private val _uiState = MutableStateFlow<AddEditNoteUiState>(
        if (noteId != null) AddEditNoteUiState.Loading else AddEditNoteUiState.Editing,
    )
    val uiState: StateFlow<AddEditNoteUiState> = _uiState.asStateFlow()

    private val _form = MutableStateFlow(AddEditNoteForm())
    val form: StateFlow<AddEditNoteForm> = _form.asStateFlow()

    init {
        if (noteId != null) {
            viewModelScope.launch {
                val note = getFieldNote(noteId)
                if (note != null) {
                    existingNote = note
                    _form.value = AddEditNoteForm(
                        content = note.content,
                        createdAt = note.createdAt,
                    )
                }
                _uiState.value = AddEditNoteUiState.Editing
            }
        }
    }

    fun onContentChange(value: String) {
        _form.update { it.copy(content = value) }
    }

    fun onSave() {
        viewModelScope.launch {
            saveFieldNote(
                content = _form.value.content,
                existingNote = existingNote,
            ).onSuccess {
                _uiState.value = AddEditNoteUiState.Saved
            }.onFailure { e ->
                _uiState.value = AddEditNoteUiState.Error(e.message ?: "Failed to save note")
            }
        }
    }

    fun onDelete() {
        val id = noteId ?: return
        viewModelScope.launch {
            deleteFieldNote(id).onSuccess {
                _uiState.value = AddEditNoteUiState.Deleted
            }.onFailure { e ->
                _uiState.value = AddEditNoteUiState.Error(e.message ?: "Failed to delete note")
            }
        }
    }

    fun onErrorDismissed() {
        _uiState.value = AddEditNoteUiState.Editing
    }
}
