package com.example.callmebaby.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call")
data class CallEntity(
    @PrimaryKey(autoGenerate = true)// PrimaryKey 를 자동적으로 생성
    val id: Int,
    var phoneNumber: String,
    var phoneNumberState : Boolean

)
