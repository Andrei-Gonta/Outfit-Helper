package com.example.labproject.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity()
data class ClothingItem(
    @PrimaryKey(autoGenerate = false)

    val id: String,
    val name: String,
    // alte atribute pentru a ajuta algoritmul in a decide ce haine sa aleaga

    )
