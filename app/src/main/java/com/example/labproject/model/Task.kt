package com.example.labproject.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity
data class Task(

    @PrimaryKey(autoGenerate = false)

    val id: String,
    val title: String,
    val description: String,
    val starttime: Date,
    val endtime: Date,
    //location = outdoor : boolean
)