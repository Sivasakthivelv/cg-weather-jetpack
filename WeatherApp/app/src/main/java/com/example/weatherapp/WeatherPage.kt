package com.example.weatherapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weatherapp.viewmodel.WeatherViewModel

@Composable
fun WeatherPage(weatherViewModel: WeatherViewModel) {
    var city by remember { mutableStateOf("") }

    Column {
        Row {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text(text = "Search for any city ") },
                modifier = Modifier
                    .padding(40.dp)
            )

            IconButton(onClick = {weatherViewModel.getData(city)}, modifier = Modifier.padding(top= 40.dp)) {
                Icon(imageVector = Icons.Default.Search,contentDescription = "Search for any location")
            }

        }
    }
}