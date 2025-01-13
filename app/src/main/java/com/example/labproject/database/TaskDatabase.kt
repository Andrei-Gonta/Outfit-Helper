package com.example.labproject.database


import android.content.Context
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.labproject.converter.TypeConverter
import com.example.labproject.dao.TaskDao
import com.example.labproject.model.Task



@Database(
    entities = [Task::class],
    version = 2,
)
@TypeConverters(TypeConverter::class)
abstract class TaskDatabase : RoomDatabase() {

    abstract val taskDao : TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null
            fun getInstance(context: Context): TaskDatabase {
                synchronized(this)
                {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_db"
                ).fallbackToDestructiveMigration().build().also{
                        INSTANCE = it}

                }

        }
    }

}