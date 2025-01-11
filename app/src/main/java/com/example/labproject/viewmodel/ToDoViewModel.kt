package com.example.labproject.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.labproject.database.ToDoDataBase
import com.example.labproject.model.ToDoItem
import com.example.labproject.repository.ToDoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ToDoViewModel(application: Application): AndroidViewModel(application) {
    private val repository: ToDoRepository
    val allTodo : LiveData<List<ToDoItem>>

    init {
        val dao = ToDoDataBase.getDatabase(application).getToDoDao()
        repository = ToDoRepository(dao)
        allTodo = repository.allTodos
    }

    fun insertTodo(todo: ToDoItem) = viewModelScope.launch(Dispatchers.IO){
        repository.insert(todo)
    }

    fun updateTodo(todo: ToDoItem) = viewModelScope.launch(Dispatchers.IO){
        repository.update(todo)
    }

    fun deleteTodo(todo: ToDoItem) = viewModelScope.launch(Dispatchers.IO){
        repository.delete(todo)
    }
}