package com.example.labproject.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date


@Entity
data class ToDoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val title: String?,
    val note: String?,
    val date: String,
    val isCompleted: Boolean = false
): Serializable
