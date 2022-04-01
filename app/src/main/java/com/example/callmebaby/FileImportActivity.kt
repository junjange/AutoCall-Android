package com.example.callmebaby

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.example.callmebaby.common.FileModel
import com.example.callmebaby.common.FileType
import com.example.callmebaby.databinding.ActivityFileImportBinding
import com.example.callmebaby.fileList.FilesListFragment
import com.example.callmebaby.utils.launchFileIntent
import java.io.File
import java.io.InputStream

class FileImportActivity : AppCompatActivity(), FilesListFragment.OnItemClickListener {
    private lateinit var binding: ActivityFileImportBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_file_import)
        binding.fileImportActivity = this
        Log.d("Ttt", "여기?")

        val path1 = Uri.parse(Environment.getExternalStorageDirectory().toString())
        Log.d("tttaa", path1.toString())
        binding.pathCheck.text = "Location : $path1"


        File(path1.toString()).walk().forEach {
            println(it)
        }


        if( savedInstanceState == null) {
            val filesListFragment = FilesListFragment.build {
                path = Environment.getExternalStorageDirectory().absolutePath
            }

            supportFragmentManager.beginTransaction()
                .add( R.id.container, filesListFragment)
                .addToBackStack( Environment.getExternalStorageDirectory().absolutePath)
                .commit()
        }



    }

    override fun onClick(fileModel: FileModel) {
        Log.d("Tttasd", fileModel.fileType.toString())
        if (fileModel.fileType == FileType.FOLDER) {
            addFileFragment(fileModel)
        }else{
            launchFileIntent(fileModel)
        }

    }
    override fun onLongClick(fileModel: FileModel) {

    }
    private fun addFileFragment(fileModel: FileModel){
        val filesListFragment = FilesListFragment.build {
            path = fileModel.path
        }
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, filesListFragment)
        fragmentTransaction.addToBackStack(fileModel.path)
        fragmentTransaction.commit()
    }

    override fun onBackPressed(){
        super.onBackPressed()
        if( supportFragmentManager.backStackEntryCount ==0){
            finish()
        }
    }



}