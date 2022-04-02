package com.example.callmebaby.common

import java.io.File

// 파일 타입, 파일 모델을 확인할 수 있는 두 개의 클래스를 정의
enum class FileType {
    FILE, FOLDER;

    companion object{


        fun getFileType(file: File) = when(file.isDirectory){
            true -> FOLDER
            false -> FILE
        }
    }
}

// FileModel은 경로, 타입, 이름 사이즈, 확장자로 구분
data class FileModel(
    val path : String,
    val fileType : FileType,
    val name : String,
    val sizeInMB: Double,
    val extension: String ="",
    val subFiles: Int = 0 )

