package com.example.labproject.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.labproject.model.ToDoItem

@Dao
interface ToDoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(todo: ToDoItem): Void

    @Delete
    suspend fun delete(todo: ToDoItem):Int

    @Query("SELECT * from ToDoItem order by id ASC")
    fun getAllTodos(): LiveData<List<ToDoItem>>

    @Update
    suspend fun update(todo: ToDoItem): Int
}