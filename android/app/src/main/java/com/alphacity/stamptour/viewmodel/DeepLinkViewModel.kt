package com.alphacity.stamptour.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.dto.FestivalItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeepLinkViewModel @Inject constructor(
    private val repository: HomeRepository,
) : ViewModel() {

    private val _program = MutableStateFlow<ProgramItem?>(null)
    val program: StateFlow<ProgramItem?> = _program

    private val _festival = MutableStateFlow<FestivalItem?>(null)
    val festival: StateFlow<FestivalItem?> = _festival

    fun loadProgram(programId: Int) {
        viewModelScope.launch {
            repository.getProgramById(programId)
                .onSuccess { _program.value = it }
                .onFailure { _program.value = null }
        }
    }

    fun loadFestival(festivalId: Int) {
        viewModelScope.launch {
            repository.getFestivals()
                .onSuccess { list -> _festival.value = list.firstOrNull { it.id == festivalId } }
                .onFailure { _festival.value = null }
        }
    }

    fun clear() {
        _program.value = null
        _festival.value = null
    }
}
