package com.example.callmebaby.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.callmebaby.R
import com.example.callmebaby.common.FileModel
import com.example.callmebaby.common.FileType
import com.example.callmebaby.data.CallEntity
import com.example.callmebaby.databinding.ActivityFileImportBinding
import com.example.callmebaby.fileList.FilesListFragment
import kotlinx.android.synthetic.main.activity_file_import.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*


// FilesListFragment 클래스를 FileImportActivity에 상속
class FileImportActivity : AppCompatActivity(), FilesListFragment.OnItemClickListener {


    private val viewModel: CallViewModel by viewModels()
    private lateinit var binding: ActivityFileImportBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_file_import)
        binding.fileImportActivity = viewModel

        val path1 = Uri.parse(Environment.getExternalStorageDirectory().toString())



        File(path1.toString()).walk().forEach {
            println(it)
            binding.pathCheck.text = "Location : $it"
            Log.d("tttaa", it.name)

        }


        if( savedInstanceState == null) {
            val filesListFragment = FilesListFragment.build {
                path = Environment.getExternalStorageDirectory().absolutePath
            }

            supportFragmentManager.beginTransaction()
                .add(R.id.container, filesListFragment)
                .addToBackStack( Environment.getExternalStorageDirectory().absolutePath)
                .commit()
        }


    }





    //onClick, onLongClick를 override하여 선언
    override fun onClick(fileModel: FileModel) {

        // 폴더일 경우 AddFileFragment를 재 호출하여 리스트를 생성
       if (fileModel.fileType == FileType.FOLDER) {
            addFileFragment(fileModel)
        }
       // 파일일 경우 launchFileIntent 함수를 사용해서 앱을 실행하고 뒤로 가기를 클릭하면 다시 폴더 리스트를 호출
       else{
            launchFileIntent(fileModel)
        }
    }

    private fun Context.launchFileIntent(fileModel: FileModel) {

        // 확장자가 csv, txt 만 실행되게 설정
        if(fileModel.extension == "csv" || fileModel.extension == "txt"){
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.deleteAll()
            }
            var line: String?

            try {
                val buf = BufferedReader(FileReader(fileModel.path))

                lifecycleScope.launch(Dispatchers.IO) {
                    while((buf.readLine().also { line = it })!=null){

                        if(fileModel.extension == "csv"){
                            val lineSize  = line!!.split(',')
                            if (lineSize.size > 1){
                                line = lineSize[1]
                            }
                        }

                        viewModel.insert(
                            CallEntity(
                                0,
                                line.toString(),
                                false)
                        )
                    }

                }

                val intent = Intent(this@FileImportActivity, MainActivity::class.java)
                finishAffinity()
                startActivity(intent)

            }catch (e: Exception){
                Log.d("ttt", e.toString())

            }

        }
    }
    override fun onLongClick(fileModel: FileModel) {}



    private fun addFileFragment(fileModel: FileModel){
        val filesListFragment = FilesListFragment.build {
            path = fileModel.path
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, filesListFragment)
        fragmentTransaction.addToBackStack(fileModel.path)
        fragmentTransaction.commit()
    }

    // 스택에 더 이상 확인할 내용이 없을 경우 종료할 수 있게 onBackPressed를 override
    override fun onBackPressed(){
        super.onBackPressed()

        if( supportFragmentManager.backStackEntryCount ==0){
            finish()
        }
    }



}

