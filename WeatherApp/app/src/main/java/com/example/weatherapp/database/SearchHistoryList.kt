package com.example.weatherapp.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Search_history_table")
data class SearchHistoryList(
    @PrimaryKey(autoGenerate = true) val id: Int,
    var searchedName: String=""
)
