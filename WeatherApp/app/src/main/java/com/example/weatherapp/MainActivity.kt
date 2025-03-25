package com.example.weatherapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.weatherapp.apiservice.NetworkResponse
import com.example.weatherapp.remotedata.Forecastday
import com.example.weatherapp.remotedata.Hour
import com.example.weatherapp.remotedata.WeatherResponse
import com.example.weatherapp.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    @RequiresApi(VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            val weatherViewModel = ViewModelProvider(this)[WeatherViewModel::class.java]
            SearchTool(weatherViewModel)
        }
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }
}

@RequiresApi(VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTool(weatherViewModel: WeatherViewModel) {
    var city by remember { mutableStateOf("") }
    val weatherResult = weatherViewModel.weatherResult.observeAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var isInternetAvailable by remember { mutableStateOf(true) }
    var isCity by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(android.graphics.Color.parseColor("#55c0da")),
                        Color(android.graphics.Color.parseColor("#01152b"))
                    )
                )
            )
    )
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = city,
                    onValueChange = { city = it },
                    label = { Text(text = stringResource(R.string.search_for_any_city)) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = colorResource(R.color.white),
                        focusedTextColor = colorResource(R.color.white),
                    )

                )
                LaunchedEffect(Unit) {
                    isInternetAvailable = checkInternetConnectivity(context)
                }

                IconButton(onClick = {
                    keyboardController?.hide()
                    if (city != "") {
                        isCity = true
                        weatherViewModel.getData(city)
                    }else{
                        isCity = false
                    }

                }) {
                    Icon(
                        modifier = Modifier.size(30.dp),
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_for_any_city)
                    )
                }
            }
            if (!isInternetAvailable) {
                EmptyDatatView("No Internet Please check Connectivity")
            }
            if (!isCity) {
                EmptyDatatView("Please Enter the Location")
            }
            when (val result = weatherResult.value) {
                is NetworkResponse.Error -> {
                    Text(
                        text = result.message,
                        fontSize = 16.sp,
                        color = colorResource(R.color.white),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }

                NetworkResponse.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .size(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(R.color.white),
                            strokeCap = StrokeCap.Square
                        )
                    }
                }

                is NetworkResponse.Success -> {
                    val data = result.data
                    WeatherScreen(data)
                }

                null -> {}
            }
        }

    }
}

@RequiresApi(VERSION_CODES.O)
@OptIn(ExperimentalCoilApi::class)
@Composable
fun WeatherScreen(data: WeatherResponse) {
    val formattedDateTime = convertDateTimeFormat(data.location.localtime)
    val hourlyData = data.forecast.forecastday[0].hour
    val forecastData = data.forecast.forecastday
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = data.current.condition.text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.white),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 10.dp),
                    textAlign = TextAlign.Center
                )
                val painter = rememberImagePainter(
                    "https:${data.current.condition.icon}".replace(
                        "64*64",
                        "128*128"
                    )
                )
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(10.dp)
                )

                Text(
                    text = "${data.location.name}, ${data.location.country}",
                    fontSize = 20.sp,
                    color = colorResource(R.color.white),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${data.current.temp_c} °C",
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    color = colorResource(R.color.white),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = formattedDateTime,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colorResource(R.color.white),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 22.dp, vertical = 5.dp)
                        .fillMaxSize()
                        .background(
                            colorResource(id = R.color.theme2),
                            shape = RoundedCornerShape(20.dp),

                            )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .height(120.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        WeatherDetailItem(
                            icon = R.drawable.humidity,
                            value = "${data.current.humidity} %",
                            label = stringResource(R.string.humidity)
                        )
                        WeatherDetailItem(
                            icon = R.drawable.windy,
                            value = "${data.current.wind_kph} Km/h",
                            label = stringResource(R.string.wind_speed)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.today),
                    color = colorResource(R.color.white),
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 5.dp)
                )
            }
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(hourlyData) { item ->
                        HourlyDataViewHolder(item)
                    }
                }
            }


            /* item {
                 ForecastView(data)
             }*/
            item {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.upcoming_day),
                        color = colorResource(R.color.white),
                        fontSize = 20.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                }
            }

            items(forecastData) { item ->
                ForecastViewHolder(item)
            }


        }


    }
}

@Composable
fun EmptyDatatView(label: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = colorResource(R.color.white),
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            textAlign = TextAlign.Center
        )
    }
}

@RequiresApi(VERSION_CODES.O)
@Composable
fun ForecastView(data: WeatherResponse) {
    val forecastData = data.forecast.forecastday
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .fillMaxWidth()
            .height(300.dp)
            .background(
                colorResource(id = R.color.theme1),
                shape = RoundedCornerShape(20.dp),
            )
    ) {
        Column {
            Text(
                text = stringResource(R.string.upcoming_day),
                color = colorResource(R.color.white),
                fontSize = 20.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )
            LazyColumn(modifier = Modifier.padding(10.dp)) {
                items(forecastData) { item ->
                    ForecastViewHolder(item)
                }
            }
        }

    }
}


@RequiresApi(VERSION_CODES.O)
@OptIn(ExperimentalCoilApi::class)
@Composable
fun ForecastViewHolder(forecastData: Forecastday) {
    val weekFormatDate = convertDateToWeekFormat(forecastData.date)
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = weekFormatDate,
            color = colorResource(R.color.white),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        val painter = rememberImagePainter(
            "https:${forecastData.day.condition.icon}".replace(
                "64*64",
                "128*128"
            )
        )
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .padding(start = 30.dp)
        )
        Text(
            text = forecastData.day.condition.text,
            color = colorResource(R.color.white),
            fontSize = 14.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )

        Text(
            text = "H | ${forecastData.day.maxtemp_c} °C",
            color = colorResource(R.color.white),
            modifier = Modifier.padding(end = 10.dp),
            fontSize = 14.sp
        )
        Text(
            text = "L | ${forecastData.day.mintemp_c} °C",
            color = colorResource(R.color.white),
            modifier = Modifier.padding(end = 10.dp),
            fontSize = 14.sp
        )

    }
}

@RequiresApi(VERSION_CODES.O)
@OptIn(ExperimentalCoilApi::class)
@Composable
fun HourlyDataViewHolder(data: Hour) {
    val formattedTime = convertTimeFormat(data.time)
    Column(
        modifier = Modifier
            .width(100.dp)
            .wrapContentHeight()
            .padding(10.dp)
            .background(colorResource(id = R.color.theme2), shape = RoundedCornerShape(8.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formattedTime,
            color = colorResource(R.color.white),
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            textAlign = TextAlign.Center
        )
        val painter = rememberImagePainter(
            "https:${data.condition.icon}".replace(
                "64*64",
                "128*128"
            )
        )
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(4.dp),
            contentScale = ContentScale.Crop
        )
        Text(
            text = "${data.temp_c} °C",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WeatherDetailItem(icon: Int, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = colorResource(R.color.white),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 5.dp)
        )

        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .padding(top = 5.dp)
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.white),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 5.dp)
        )

    }

}

@RequiresApi(VERSION_CODES.O)
fun convertDateTimeFormat(date: String): String {
    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val dateTime = LocalDateTime.parse(date, inputFormatter)
    val outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
    return dateTime.format(outputFormatter)
}

@RequiresApi(VERSION_CODES.O)
fun convertTimeFormat(date: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val dates = inputFormat.parse(date)
    val outputFormat = SimpleDateFormat("hh a", Locale.getDefault())
    return outputFormat.format(dates)
}

@RequiresApi(VERSION_CODES.O)
fun convertDateToWeekFormat(date: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dates = inputFormat.parse(date)
    val outputFormat = SimpleDateFormat("EEE", Locale.getDefault())
    return outputFormat.format(dates)
}


fun checkInternetConnectivity(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
    return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}