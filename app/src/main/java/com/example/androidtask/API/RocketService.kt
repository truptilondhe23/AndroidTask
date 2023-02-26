package com.example.truptiassignment.API

import com.example.myapplication.models.LocationModel
import retrofit2.Response
import retrofit2.http.GET

interface RocketService {

    @GET("tempfiles/tracker.json")
    suspend fun getRocket() : Response<LocationModel>
}