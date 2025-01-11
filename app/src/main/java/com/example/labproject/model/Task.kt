package com.example.labproject.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity()
data class Task(

    @PrimaryKey(autoGenerate = false)

    val id: String,
    val title: String,
    val description: String,
    val date: Date,
    //location = outdoor : boolean
)