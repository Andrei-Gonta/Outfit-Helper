package com.example.labproject.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.labproject.dao.ToDoDao
import com.example.labproject.model.ToDoItem


@Database(entities = [ToDoItem::class], version = 1)
abstract class ToDoDataBase : RoomDatabase(){
    abstract fun getToDoDao(): ToDoDao


    companion object {
        @Volatile
        private var INSTANCE: ToDoDataBase? = null

        fun getDatabase(context: Context): ToDoDataBase {
        synchronized(this){
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ToDoDataBase::class.java,
                    name = "DATABASE"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}}