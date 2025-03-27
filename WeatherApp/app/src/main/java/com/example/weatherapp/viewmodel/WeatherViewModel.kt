package com.example.weatherapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.InternetCheckWorker
import com.example.weatherapp.apiservice.NetworkResponse
import com.example.weatherapp.apiservice.RetrofitInstance
import com.example.weatherapp.database.SearchHistoryDao
import com.example.weatherapp.database.SearchHistoryDatabase
import com.example.weatherapp.database.SearchHistoryList
import com.example.weatherapp.remotedata.WeatherResponse
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val searchHistoryDao = SearchHistoryDatabase.getDatabase(application).searchHistoryDao()

    private val apiService = RetrofitInstance.getInstance
    private val _weatherResult = MutableLiveData<NetworkResponse<WeatherResponse>>()
    val weatherResult: LiveData<NetworkResponse<WeatherResponse>> = _weatherResult

    private val _internetStatus = MutableLiveData(false)
    val internetStatus: LiveData<Boolean> = _internetStatus

    fun checkInternetStatus() {
        val workRequest = OneTimeWorkRequestBuilder<InternetCheckWorker>()
            .build()

        WorkManager.getInstance(getApplication()).enqueue(workRequest)

        WorkManager.getInstance(getApplication())
            .getWorkInfoByIdLiveData(workRequest.id)
            .observeForever { workInfo ->
                _internetStatus.value = workInfo?.state == WorkInfo.State.SUCCEEDED
            }
    }

    fun getData(city: String) {
        val apikey  = BuildConfig.API_KEY
        val cityData = SearchHistoryList(0, city)
        _weatherResult.value = NetworkResponse.Loading
        viewModelScope.launch {
            try {
                val response =
                    apiService.getWeather(apikey, city, "6", "no", "no")
                if (response.isSuccessful) {
                    addTask(cityData)
                    response.body()?.let {
                        _weatherResult.value = NetworkResponse.Success(it)
                    } ?: run {
                        _weatherResult.value = NetworkResponse.Error("Empty response body")
                    }
                } else {
                    val errorResponse = response.errorBody()?.string()
                    val errorMessage =
                        errorResponse?.let { JSONObject(it).getJSONObject("error").getString("message") }
                    _weatherResult.value = errorMessage?.let { NetworkResponse.Error(it) }
                }

            } catch (e: HttpException) {
                _weatherResult.value = NetworkResponse.Error("Invalid city name")
            } catch (e: IOException) {
                _weatherResult.value = NetworkResponse.Error("No internet connection")
            } catch (e: Exception) {
                _weatherResult.value =
                    NetworkResponse.Error("An unexpected error occurred: ${e.message}")
            }
        }

    }


    private fun addTask(searchHistoryList: SearchHistoryList) {
        viewModelScope.launch {
            searchHistoryDao.insert(searchHistoryList)

        }
    }
}

