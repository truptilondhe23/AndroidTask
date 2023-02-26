package com.example.truptiassignment.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.truptiassignment.viewmodels.MainViewModel

class MainViewModelFactory(private val repository: RocketRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}