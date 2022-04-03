package com.example.callmebaby.utils



import com.example.callmebaby.common.FileModel
import com.example.callmebaby.common.FileType
import java.io.File


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



