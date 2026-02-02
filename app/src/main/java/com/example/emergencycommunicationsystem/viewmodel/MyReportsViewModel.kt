package com.example.emergencycommunicationsystem.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.data.IncidentReport
import com.example.emergencycommunicationsystem.data.repository.IncidentRepository
import com.example.emergencycommunicationsystem.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyReportsViewModel(private val repository: IncidentRepository) : ViewModel() {

    private val _reportsState = MutableStateFlow<Resource<List<IncidentReport>>>(Resource.Loading)
    val reportsState: StateFlow<Resource<List<IncidentReport>>> = _reportsState.asStateFlow()

    fun fetchUserReports(userId: Int) {
        viewModelScope.launch {
            _reportsState.value = Resource.Loading
            try {
                val result = repository.getUserReports(userId)
                when (result) {
                    is Resource.Success -> {
                        _reportsState.value = Resource.Success(result.data?.reports ?: emptyList())
                    }
                    is Resource.Error -> {
                        _reportsState.value = Resource.Error(result.message ?: "Failed to load reports")
                    }
                    is Resource.Loading -> {
                         _reportsState.value = Resource.Loading
                    }
                }
            } catch (e: Exception) {
                _reportsState.value = Resource.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
}

class MyReportsViewModelFactory(private val repository: IncidentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyReportsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyReportsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
