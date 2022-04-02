package com.example.callmebaby.utils


import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.callmebaby.adapter.CallRecyclerAdapter
import com.example.callmebaby.common.FileModel
import com.example.callmebaby.common.FileType
import com.example.callmebaby.ui.CallViewModel
import java.io.BufferedReader
import java.io.File
import java.io.FileReader


// 파일 경로, 용량, 파일 모델을 구성하기 위한 함수를 utils 패키지
fun getFilesFromPath( path : String, showHiddenFiles : Boolean = false, onlyFolders:Boolean = false):
        List<File>{
    val file = File(path)
    return file.listFiles()
        .filter { showHiddenFiles || !it.name.startsWith(".")}
        .filter { !onlyFolders || it.isDirectory}
        .toList()
}


// 파일 리스트를 확인
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

// 파일을 선택하면 파일 확장자에 연결된 앱을 실행하기 위해서 이벤트를 연동
// launchFileintent 함수는 fileModel 경로에 확인된 확장자에 따라서 설치된 앱을 실행
//fun Context.launchFileIntent(fileModel: FileModel) {
//
//    val buf: BufferedReader = BufferedReader(FileReader(fileModel.path))
//    Log.d("Ttt", buf.readLine())
//
//
//    // 확장자가 xlsx, txt 만 실행되게 설정
//    if(fileModel.extension == "xlsx" || fileModel.extension == "txt"){
//        val intent = Intent(Intent.ACTION_VIEW)
//        intent.data = FileProvider.getUriForFile(this, packageName, File(fileModel.path))
//        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//        startActivity(Intent.createChooser(intent, "Select Application"))
//    }
//
//
//}

