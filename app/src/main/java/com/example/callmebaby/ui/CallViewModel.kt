package com.example.callmebaby.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.callmebaby.data.CallDatabase
import com.example.callmebaby.data.CallEntity
import com.example.callmebaby.data.CallRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallViewModel(application: Application) : AndroidViewModel(application){
    private val callRepository: CallRepository =
        CallRepository(CallDatabase.getDatabase(application, viewModelScope))

    var allPhoneNumber: LiveData<List<CallEntity>> = callRepository.allPhoneNumber
    var allTurePhoneNumber : LiveData<List<CallEntity>>  = callRepository.allTurePhoneNumber


    fun insert(callEntity: CallEntity) = viewModelScope.launch(Dispatchers.IO) {
        callRepository.insert(callEntity)
    }

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        callRepository.deleteAll()
    }

    fun update(callEntity: CallEntity) = viewModelScope.launch(Dispatchers.IO) {
        callRepository.update(callEntity)
    }


    fun delete(callEntity: CallEntity) = viewModelScope.launch(Dispatchers.IO) {
        callRepository.delete(callEntity)
    }

    fun getAll(): LiveData<List<CallEntity>>{
        return allPhoneNumber
    }

    fun getTureAll(): LiveData<List<CallEntity>>{
        return allTurePhoneNumber
    }

}