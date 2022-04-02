package com.example.callmebaby.data

import android.telecom.Call
import android.util.Log
import androidx.lifecycle.LiveData

class CallRepository(callDatabase: CallDatabase) {

    private val callDao = callDatabase.callDao()
    val allPhoneNumber: LiveData<List<CallEntity>> = callDao.getAll()
    val allFalsePhoneNumber: LiveData<List<CallEntity>> = callDao.getFalseAll()
    companion object {
        private var sInstance: CallRepository? = null
        fun getInstance(database: CallDatabase): CallRepository {
            return sInstance
                ?: synchronized(this) {
                    val instance = CallRepository(database)
                    sInstance = instance
                    instance
                }
        }
    }


    fun insert(callEntity: CallEntity) {
        callDao.insert(callEntity)
    }

    fun update(callEntity: CallEntity){
        callDao.update(callEntity)
        Log.d("tt", "ㅎㅇㅎㅇ")
    }

    fun delete(callEntity: CallEntity) {
        callDao.delete(callEntity)
    }

    fun deleteAll(){
        callDao.deleteAll()
    }

}