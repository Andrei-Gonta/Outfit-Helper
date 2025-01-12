package com.example.labproject.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.labproject.model.ClothingItem

import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingItemDao {

    @Query("SELECT * FROM ClothingItem ORDER BY id")
    fun getClothingItems() : Flow<List<ClothingItem>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClothingItem(clothingItem: ClothingItem): Long


    // First way
    @Delete
    suspend fun deleteClothingItem(clothingItem: ClothingItem) : Int


    // Second Way
    @Query("DELETE FROM ClothingItem WHERE id == :clothingItemId")
    suspend fun deleteClothingItemUsingId(clothingItemId: String) : Int


    @Update
    suspend fun updateClothingItem(clothingItem: ClothingItem): Int


    @Query("UPDATE ClothingItem SET name=:name WHERE id = :clothingItemId")
    suspend fun updateClothingItemPaticularField(clothingItemId: String,name:String): Int


    @Query("SELECT * FROM ClothingItem WHERE name LIKE :query ")
    fun searchClothingList(query: String) : Flow<List<ClothingItem>>
}