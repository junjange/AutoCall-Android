package com.example.callmebaby.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.callmebaby.R
import com.example.callmebaby.common.FileModel
import kotlinx.android.synthetic.main.item_file_breadcrumb.view.*

class BreadcrumbFileAdapter : RecyclerView.Adapter<BreadcrumbFileAdapter.ViewHolder>() {
    var onItemClickListener: ((FileModel) -> Unit)? = null
    var files = listOf<FileModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file_breadcrumb, parent, false)
        return ViewHolder(view)

    }
    override fun getItemCount() = files.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bindView(position)

    fun updateData(files: List<FileModel>) {
        this.files = files
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onItemClickListener?.invoke(files[adapterPosition])
        }

        fun bindView(position: Int) {
            val file = files[position]
            itemView.nameTextView.text = file.name
        }
    }
}

