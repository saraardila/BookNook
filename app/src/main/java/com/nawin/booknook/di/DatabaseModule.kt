package com.nawin.booknook.di

import android.content.Context
import androidx.room.Room
import com.nawin.booknook.data.local.AppDatabase
import com.nawin.booknook.data.local.dao.BookDao
import com.nawin.booknook.data.local.dao.BookNoteDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "booknook_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideBookDao(db: AppDatabase): BookDao = db.bookDao()

    @Provides
    fun provideBookNoteDao(db: AppDatabase): BookNoteDao = db.bookNoteDao()
}