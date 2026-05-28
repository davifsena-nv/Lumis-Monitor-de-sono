package com.lumis.data.dao

import androidx.room.*
import com.lumis.data.model.SleepNight
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepNightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(night: SleepNight)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nights: List<SleepNight>)

    @Query("SELECT * FROM sleep_nights ORDER BY date DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 7): List<SleepNight>

    @Query("SELECT * FROM sleep_nights WHERE date = :date")
    suspend fun getByDate(date: String): SleepNight?

    @Query("SELECT * FROM sleep_nights ORDER BY date DESC LIMIT 7")
    fun observeLastWeek(): Flow<List<SleepNight>>

    @Query("DELETE FROM sleep_nights WHERE date < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: String)
}
