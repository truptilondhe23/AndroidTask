package com.example.truptiassignment.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.models.LocationModel
import com.example.truptiassignment.repository.RocketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val repository: RocketRepository) : ViewModel() {


    init {
        viewModelScope.launch (Dispatchers.IO){
            repository.getRocket()
        }
    }

    val rockets:LiveData<LocationModel>
    get() = repository.rockets

}