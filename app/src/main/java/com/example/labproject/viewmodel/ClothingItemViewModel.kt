package com.example.labproject.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.labproject.model.ClothingItem
import com.example.labproject.model.Task
import com.example.labproject.repository.ClothingItemRepository


class ClothingItemViewModel(application: Application) : AndroidViewModel(application) {


    private val clothingItemRepository = ClothingItemRepository(application)
    val clothingItemStateFlow get() =  clothingItemRepository.clothingItemStateFlow
    val statusLiveData get() =  clothingItemRepository.statusLiveData
    val sortByLiveData get() =  clothingItemRepository.sortByLiveData

    fun setSortBy(sort:Pair<String,Boolean>){
        clothingItemRepository.setSortBy(sort)
    }

    fun getClothingItems(isAsc : Boolean, sortByName:String) {
        clothingItemRepository.getClothingItems(isAsc, sortByName)
    }

    fun insertClothingItem(clothingItem: ClothingItem){
        clothingItemRepository.insertClothingItem(clothingItem)
    }

    fun deleteClothingItem(clothingItem: ClothingItem) {
        clothingItemRepository.deleteClothingItem(clothingItem)
    }

    fun deleteClothingItemUsingId(clothingItemId: String){
        clothingItemRepository.deleteClothingItemUsingId(clothingItemId)
    }

    fun updateClothingItem(clothingItem: ClothingItem) {
        clothingItemRepository.updateClothingItem(clothingItem)
    }

    fun updateClothingItemPaticularField(clothingItemId: String, name:String) {
        clothingItemRepository.updateClothingItemPaticularField(clothingItemId, name)
    }
    fun searchClothingList(query: String){
        clothingItemRepository.searchClothingList(query)
    }
}