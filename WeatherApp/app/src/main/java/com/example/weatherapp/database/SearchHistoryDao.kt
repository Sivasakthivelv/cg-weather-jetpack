package com.example.weatherapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface SearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(searchHistoryList: SearchHistoryList)

    @Delete
    suspend fun delete(searchHistoryList: SearchHistoryList)

    @Update
    suspend fun update(searchHistoryList: SearchHistoryList)


    @Query("SELECT * FROM Search_history_table")
    fun getAllUsers(): LiveData<List<SearchHistoryList>>

}