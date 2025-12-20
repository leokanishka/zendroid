package com.zendroid.launcher.di

import android.content.Context
import androidx.room.Room
import com.zendroid.launcher.data.db.AppDao
import com.zendroid.launcher.data.db.AppDatabase
import com.zendroid.launcher.data.db.SessionDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "zendroid_db"
        )
        .fallbackToDestructiveMigration() // TODO: Add proper migration for production
        .build()
    }

    @Provides
    fun provideAppDao(database: AppDatabase): AppDao {
        return database.appDao()
    }

    @Provides
    fun provideSessionDao(database: AppDatabase): SessionDao {
        return database.sessionDao()
    }
}
