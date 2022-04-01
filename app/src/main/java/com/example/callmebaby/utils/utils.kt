package com.example.callmebaby.utils


import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.example.callmebaby.common.FileModel
import com.example.callmebaby.common.FileType
import java.io.File

fun getFilesFromPath( path : String, showHiddenFiles : Boolean = false, onlyFolders:Boolean = false):
        List<File>{
    val file = File(path)
    return file.listFiles()
        .filter { showHiddenFiles || !it.name.startsWith(".")}
        .filter { !onlyFolders || it.isDirectory}
        .toList() }
fun getFileModelsFromFiles(files: List<File>) :
        List<FileModel>{
    return files.map {
        Log.d("tt", it.toString())

        FileModel(it.path,
            FileType.getFileType(it),
            it.name,
            convertFileSizeToMB(it.length()),
            it.extension,
            it.listFiles()?.size?:0
        )
    }

}

fun convertFileSizeToMB(sizeInBytes: Long) : Double{
    return ( sizeInBytes.toDouble()) / (1024 * 1024)
}

fun Context.launchFileIntent(fileModel: FileModel) {
    Log.d("ttt", fileModel.toString())
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = FileProvider.getUriForFile(this, packageName, File(fileModel.path))
    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    startActivity(Intent.createChooser(intent, "Select Application")) }

