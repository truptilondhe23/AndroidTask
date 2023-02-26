package com.example.truptiassignment.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.models.LocationModel
import com.example.truptiassignment.API.RocketService

class  RocketRepository(private val rocketService: RocketService) {

    private val rocketLiveData = MutableLiveData<LocationModel>()

    val rockets :LiveData<LocationModel>
    get() =rocketLiveData

    suspend fun getRocket()
    {
        var rocket = rocketService.getRocket()
        if(rocket?.body() !=null)
        {
            rocketLiveData.postValue(rocket.body())
        }
    }
}