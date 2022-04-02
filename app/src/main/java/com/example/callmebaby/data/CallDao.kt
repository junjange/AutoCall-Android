package com.example.callmebaby.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CallDao {
    // 데이터 베이스 불러오기
    @Query("SELECT * from call")
    fun getAll(): LiveData<List<CallEntity>>

    @Query("SELECT * from call where phoneNumberState =:phoneNumberState")
    fun getFalseAll(phoneNumberState : Boolean = false): LiveData<List<CallEntity>>

    // 데이터 추가
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(callEntity: CallEntity)

    // 데이터 전체 삭제
    @Query("DELETE FROM call")
    fun deleteAll()

    // 데이터 업데이트
    @Update
    fun update(callEntity: CallEntity)

    // 데이터 삭제
    @Delete
    fun delete(callEntity: CallEntity)


}