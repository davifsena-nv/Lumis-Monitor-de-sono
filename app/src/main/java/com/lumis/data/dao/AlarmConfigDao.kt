package com.lumis.data.dao

import androidx.room.*
import com.lumis.data.model.AlarmConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(config: AlarmConfigEntity)

    @Query("SELECT * FROM alarm_config WHERE id = 1")
    suspend fun get(): AlarmConfigEntity?

    @Query("SELECT * FROM alarm_config WHERE id = 1")
    fun observe(): Flow<AlarmConfigEntity?>

    @Query("UPDATE alarm_config SET scheduledAlarmTime = :epochMillis WHERE id = 1")
    suspend fun updateScheduledTime(epochMillis: Long?)
}
