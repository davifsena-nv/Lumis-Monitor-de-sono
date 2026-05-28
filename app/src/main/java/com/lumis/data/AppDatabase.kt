package com.lumis.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lumis.data.dao.AlarmConfigDao
import com.lumis.data.dao.SleepNightDao
import com.lumis.data.model.AlarmConfigEntity
import com.lumis.data.model.SleepNight

@Database(
    entities = [SleepNight::class, AlarmConfigEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sleepNightDao(): SleepNightDao
    abstract fun alarmConfigDao(): AlarmConfigDao
}
