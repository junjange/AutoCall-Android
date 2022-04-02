package com.example.callmebaby.fileList

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.callmebaby.R
import com.example.callmebaby.adapter.FilesRecyclerAdapter
import com.example.callmebaby.common.FileModel
import com.example.callmebaby.utils.getFileModelsFromFiles
import com.example.callmebaby.utils.getFilesFromPath
import kotlinx.android.synthetic.main.fragment_file_list.*


class FilesListFragment : Fragment() {
    private lateinit var mFilesAdapter: FilesRecyclerAdapter
    private lateinit var PATH: String
    private lateinit var eCallback: OnItemClickListener

    // OnItemClickListener 이벤트 변수를 선언
    interface OnItemClickListener{
        fun onClick(fileModel : FileModel)
        fun onLongClick(fileModel : FileModel)
    }

    companion object {
        private const val ARG_PATH: String = "com.example.callmebaby"
        fun build(block: Builder.() -> Unit) = Builder().apply(block).build()

    }
    // 모든 변수에 연결할 수 있는 Builder 클래스는 입력받은 FilesListFragment 정보를 연결
    class Builder{
        var path : String = ""
        fun build(): FilesListFragment {
            val fragment = FilesListFragment()
            val args = Bundle()
            args.putString(ARG_PATH, path)
            fragment.arguments = args
            return fragment
        }
    }

    // OnItemClickListener로 캐스팅하여 eCallback에 연결
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{ eCallback = context as OnItemClickListener }
        catch( e: Exception){
            throw Exception("${context} FileListFragment onAttach")
        }
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_file_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val filePath = arguments?.getString(ARG_PATH)
        if (filePath == null) {
            Toast.makeText(context, "Path should not be null!", Toast.LENGTH_SHORT).show()
            return
        }
        PATH = filePath

        initViews()
    }

    private fun initViews() {
        filesRecyclerView.layoutManager = LinearLayoutManager(context)
        mFilesAdapter = FilesRecyclerAdapter()
        filesRecyclerView.adapter = mFilesAdapter

        // onItemClickListener, onItemLongClickListener 함수에 eCallback 이벤트를 연동하면
        // 리스트 클릭 시점에 eCallback가 호출되면서 이벤트가 연동된다.
        mFilesAdapter.onItemClickListener = {
            eCallback.onClick(it)
        }
        mFilesAdapter.onItemLongClickListener = {
            eCallback.onLongClick(it)
        }

        upDate()
    }

    private fun upDate() {
        val files = getFileModelsFromFiles(getFilesFromPath(PATH))

        if (files.isEmpty()) {
            emptyFolderLayout.visibility = View.VISIBLE
        } else {
            emptyFolderLayout.visibility = View.INVISIBLE
        }

        mFilesAdapter.updateData(files)
    }
}
