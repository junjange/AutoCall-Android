package com.example.callmebaby.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.callmebaby.R
import com.example.callmebaby.data.CallEntity
import com.example.callmebaby.ui.CallViewModel
import com.example.callmebaby.ui.MainActivity
import kotlinx.android.synthetic.main.item_recycler_call.view.*

class CallRecyclerAdapter internal constructor(context: MainActivity, var onDeleteListener: CallViewModel)
    : RecyclerView.Adapter<CallRecyclerAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var call = emptyList<CallEntity>() // Cached copy of words


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val num: TextView = itemView.text
        val deleteButton: Button = itemView.delete_button

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val itemView = inflater.inflate(R.layout.item_recycler_call, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val callNumber = call[position]

        holder.num.text = callNumber.phoneNumber

        if (call[position].phoneNumberState){


            holder.num.paintFlags = holder.num.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.num.setTextColor(Color.GRAY)
        }

        holder.deleteButton.setOnClickListener(View.OnClickListener {
            onDeleteListener.delete(callNumber)
            return@OnClickListener
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    internal fun setUsers(calls: List<CallEntity>) {
        this.call = calls
        notifyDataSetChanged()
    }

    override fun getItemCount() = call.size


}