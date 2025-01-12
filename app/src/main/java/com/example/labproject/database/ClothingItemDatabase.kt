package com.example.labproject.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.labproject.converter.TypeConverter
import com.example.labproject.dao.ClothingItemDao
import com.example.labproject.dao.TaskDao
import com.example.labproject.model.ClothingItem
import com.example.labproject.model.Task

@Database(
    entities = [ClothingItem::class],
    version = 1,
    exportSchema = false
)

abstract class ClothingItemDatabase : RoomDatabase() {

    abstract val clothingItemDao : ClothingItemDao

    companion object {
        @Volatile
        private var INSTANCE: ClothingItemDatabase? = null
        fun getInstance(context: Context): ClothingItemDatabase {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ClothingItemDatabase::class.java,
                    "clothing_db"
                ).build().also {
                    INSTANCE = it
                }
            }

        }
    }

}