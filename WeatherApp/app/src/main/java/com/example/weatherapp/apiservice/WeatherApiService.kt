package com.example.weatherapp.apiservice

import com.example.weatherapp.remotedata.CurrentWeatherResponse
import com.example.weatherapp.remotedata.WeatherResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("v1/forecast.json?")
    suspend fun getWeather(
        @Query("key") apiKey: String,
        @Query("q") city: String,
        @Query("days") forecastDay: String,
        @Query("aqi") aqi: String = "no",
        @Query("alerts") alerts: String = "no"
    ): Response<WeatherResponse>


}