package com.example.labproject.repository

import androidx.lifecycle.LiveData
import com.example.labproject.dao.ToDoDao
import com.example.labproject.model.ToDoItem

class ToDoRepository(private val ToDoDao: ToDoDao) {

    val allTodos: LiveData<List<ToDoItem>> = ToDoDao.getAllTodos()

    suspend fun insert(todo: ToDoItem){
        ToDoDao.insert(todo)
    }

    suspend fun delete(todo: ToDoItem){
        ToDoDao.delete(todo)
    }

    suspend fun update(todo: ToDoItem){
        ToDoDao.update(todo)
    }
}