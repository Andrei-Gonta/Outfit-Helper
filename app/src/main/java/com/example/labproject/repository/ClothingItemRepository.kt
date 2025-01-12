package com.example.labproject.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.labproject.dao.ClothingItemDao
import com.example.labproject.database.ClothingItemDatabase
import com.example.labproject.model.ClothingItem
import com.example.labproject.utils.Resource
import com.example.labproject.utils.Resource.Loading
import com.example.labproject.utils.Resource.Error
import com.example.labproject.utils.Resource.Success
import com.example.labproject.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClothingItemRepository(application: Application)  {

    private val clothingItemDao = ClothingItemDatabase.getInstance(application).clothingItemDao


    private val _clothingItemStateFlow = MutableStateFlow<Resource<Flow<List<ClothingItem>>>>(Resource.Loading())
    val clothingItemStateFlow: StateFlow<Resource<Flow<List<ClothingItem>>>>
        get() = _clothingItemStateFlow

    private val _statusLiveData = MutableLiveData<Resource<Util.StatusResult>>()
    val statusLiveData: LiveData<Resource<Util.StatusResult>>
        get() = _statusLiveData


    private val _sortByLiveData = MutableLiveData<Pair<String,Boolean>>().apply {
        postValue(Pair("name",true))
    }
    val sortByLiveData: LiveData<Pair<String,Boolean>>
        get() = _sortByLiveData


    fun setSortBy(sort:Pair<String,Boolean>){
        _sortByLiveData.postValue(sort)
    }

    fun getClothingItems(isAsc : Boolean, sortByName:String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _clothingItemStateFlow.emit(Resource.Loading())
                delay(500)
                val result = clothingItemDao.getClothingItems()
                _clothingItemStateFlow.emit(Resource.Success("loading", result))
            } catch (e: Exception) {
                _clothingItemStateFlow.emit(Error(e.message.toString()))
            }
        }
    }


    fun insertClothingItem(clothingItem: ClothingItem) {
        try {
            _statusLiveData.postValue(Resource.Loading())
            CoroutineScope(Dispatchers.IO).launch {
                val result = clothingItemDao.insertClothingItem(clothingItem)
                handleResult(result.toInt(), "Inserted Item Successfully", Util.StatusResult.Added)
            }
        } catch (e: Exception) {
            _statusLiveData.postValue(Error(e.message.toString()))
        }
    }


    fun deleteClothingItem(clothingItem: ClothingItem) {
        try {
            _statusLiveData.postValue(Loading())
            CoroutineScope(Dispatchers.IO).launch {
                val result = clothingItemDao.deleteClothingItem(clothingItem)
                handleResult(result, "Deleted Item Successfully", Util.StatusResult.Deleted)

            }
        } catch (e: Exception) {
            _statusLiveData.postValue(Error(e.message.toString()))
        }
    }

    fun deleteClothingItemUsingId(clothingItemId: String) {
        try {
            _statusLiveData.postValue(Resource.Loading())
            CoroutineScope(Dispatchers.IO).launch {
                val result = clothingItemDao.deleteClothingItemUsingId(clothingItemId)
                handleResult(result, "Deleted Item Successfully", Util.StatusResult.Deleted)

            }
        } catch (e: Exception) {
            _statusLiveData.postValue(Error(e.message.toString()))
        }
    }


    fun updateClothingItem(clothingItem: ClothingItem) {
        try {
            _statusLiveData.postValue(Loading())
            CoroutineScope(Dispatchers.IO).launch {
                val result = clothingItemDao.updateClothingItem(clothingItem)
                handleResult(result, "Updated Item Successfully", Util.StatusResult.Updated)

            }
        } catch (e: Exception) {
            _statusLiveData.postValue(Error(e.message.toString()))
        }
    }

    fun updateClothingItemPaticularField(clothingItemId: String,  name: String) {
        try {
            _statusLiveData.postValue(Loading())
            CoroutineScope(Dispatchers.IO).launch {
                val result = clothingItemDao.updateClothingItemPaticularField(clothingItemId, name)
                handleResult(result, "Updated Task Successfully", Util.StatusResult.Updated)

            }
        } catch (e: Exception) {
            _statusLiveData.postValue(Error(e.message.toString()))
        }
    }

    fun searchClothingList(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _clothingItemStateFlow.emit(Resource.Loading())
                val result = clothingItemDao.searchClothingList("%${query}%")
                _clothingItemStateFlow.emit(Resource.Success("loading", result))
            } catch (e: Exception) {
                _clothingItemStateFlow.emit(Error(e.message.toString()))
            }
        }
    }


    private fun handleResult(result: Int, message: String, statusResult: Util.StatusResult) {
        if (result != -1) {
            _statusLiveData.postValue(Resource.Success(message, statusResult))
        } else {
            _statusLiveData.postValue(Error("Something Went Wrong", statusResult))
        }
    }

}