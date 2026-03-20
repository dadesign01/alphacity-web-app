package com.alphacity.stamptour.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TermsViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchTerms(type: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = homeRepository.getTerms(type)
            result.onSuccess { terms ->
                val activeTerm = terms.firstOrNull()
                _title.value = activeTerm?.title ?: ""
                _content.value = activeTerm?.content ?: ""
            }.onFailure {
                _title.value = ""
                _content.value = ""
            }
            _isLoading.value = false
        }
    }
}
