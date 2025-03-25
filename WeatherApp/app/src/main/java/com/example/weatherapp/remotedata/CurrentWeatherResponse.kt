package com.example.weatherapp.remotedata

data class CurrentWeatherResponse(
    val current: CurrentX,
    val location: LocationX
)