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
import com.example.callmebaby.common.FileModel
import com.example.callmebaby.utils.getFileModelsFromFiles
import com.example.callmebaby.utils.getFilesFromPath
import kotlinx.android.synthetic.main.fragment_file_list.*


class FilesListFragment : Fragment() {
    private lateinit var mFilesAdapter: FilesRecyclerAdapter
    private lateinit var PATH: String
    private lateinit var eCallback: OnItemClickListener

    interface OnItemClickListener{
        fun onClick(fileModel : FileModel)
        fun onLongClick(fileModel : FileModel)
    }




    companion object {
        private const val ARG_PATH: String = "com.example.callmebaby"
        fun build(block: Builder.() -> Unit) = Builder().apply(block).build()

    }

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
        Log.d("ttt", PATH)
        Log.d("ttt", filePath)

        initViews()
    }

    private fun initViews() {
        filesRecyclerView.layoutManager = LinearLayoutManager(context)
        mFilesAdapter = FilesRecyclerAdapter()
        filesRecyclerView.adapter = mFilesAdapter

        mFilesAdapter.onItemClickListener = {
            eCallback.onClick(it)
        }
        mFilesAdapter.onItemLongClickListener = { eCallback.onLongClick(it) }

        upDateDate()
    }

    fun upDateDate() {
        val files = getFileModelsFromFiles(getFilesFromPath(PATH))

        if (files.isEmpty()) {
            emptyFolderLayout.visibility = View.VISIBLE
        } else {
            emptyFolderLayout.visibility = View.INVISIBLE
        }

        mFilesAdapter.updateData(files)
    }
}
