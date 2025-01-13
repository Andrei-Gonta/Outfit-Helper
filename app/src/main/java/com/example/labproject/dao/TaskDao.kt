package com.example.labproject.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.labproject.model.Task
import kotlinx.coroutines.flow.Flow


@Dao
interface TaskDao {

/*
    @Query("""SELECT * FROM Task ORDER BY
        CASE WHEN :isAsc = 1 THEN taskTitle END ASC, 
        CASE WHEN :isAsc = 0 THEN taskTitle END DESC""")
    fun getTaskListSortByTaskTitle(isAsc: Boolean) : Flow<List<Task>>

    @Query("""SELECT * FROM Task ORDER BY
        CASE WHEN :isAsc = 1 THEN date END ASC, 
        CASE WHEN :isAsc = 0 THEN date END DESC""")
    fun getTaskListSortByTaskDate(isAsc: Boolean) : Flow<List<Task>>
*/
    @Query("SELECT * FROM Task ORDER BY starttime")
    fun getTaskList() : Flow<List<Task>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long


    // First way
    @Delete
    suspend fun deleteTask(task: Task) : Int


    // Second Way
    @Query("DELETE FROM Task WHERE id == :taskId")
    suspend fun deleteTaskUsingId(taskId: String) : Int


    @Update
    suspend fun updateTask(task: Task): Int



    @Query("SELECT * FROM Task WHERE title LIKE :query ORDER BY starttime DESC")
    fun searchTaskList(query: String) : Flow<List<Task>>
}