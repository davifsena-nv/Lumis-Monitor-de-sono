package com.lumis.data.di

import android.content.Context
import androidx.room.Room
import com.lumis.data.AppDatabase
import com.lumis.data.dao.AlarmConfigDao
import com.lumis.data.dao.SleepNightDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "lumis.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideSleepNightDao(db: AppDatabase): SleepNightDao = db.sleepNightDao()

    @Provides
    fun provideAlarmConfigDao(db: AppDatabase): AlarmConfigDao = db.alarmConfigDao()
}
