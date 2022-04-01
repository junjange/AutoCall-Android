package com.example.callmebaby.common

import java.io.File

enum class FileType {
    FILE, FOLDER;

    companion object{


        fun getFileType(file: File) = when(file.isDirectory){
            true -> FOLDER
            false -> FILE
        }
    }
}
data class FileModel(
    val path : String,
    val fileType : FileType,
    val name : String,
    val sizeInMB: Double,
    val extension: String ="",
    val subFiles: Int = 0 )

