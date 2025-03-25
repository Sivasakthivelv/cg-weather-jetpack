package com.example.weatherapp.remotedata

data class WeatherResponse(
    val location: Location,
    val current: Current,
    val forecast: Forecast,

)