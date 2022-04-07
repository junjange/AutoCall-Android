package com.example.callmebaby.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.callmebaby.R
import com.example.callmebaby.data.CallEntity
import com.example.callmebaby.ui.CallViewModel
import com.example.callmebaby.ui.MainActivity
import kotlinx.android.synthetic.main.item_recycler_call.view.*
import kotlinx.coroutines.*
import java.util.*

class CallRecyclerAdapter internal constructor(context: MainActivity, var onDeleteListener: CallViewModel)
    : RecyclerView.Adapter<CallRecyclerAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var call = emptyList<CallEntity>() // Cached copy of words


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val linearLayout : LinearLayout = itemView.linearLayout
        val num: TextView = itemView.text
        val deleteButton: TextView = itemView.delete_button
        val idx: TextView = itemView.number

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val itemView = inflater.inflate(R.layout.item_recycler_call, parent, false)

        return ViewHolder(itemView)
    }

    // position 위치의 데이터를 삭제 후 어댑터 갱신
    private fun removeData(position: Int) {
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount - position)

    }



    // 현재 선택된 데이터와 드래그한 위치에 있는 데이터를 교환
    fun swapData(fromPos: Int, toPos: Int) {
        Collections.swap(call, fromPos, toPos)
        notifyItemMoved(fromPos, toPos)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.linearLayout.translationX = 0f

        val callNumber = call[position]
        holder.num.text = callNumber.phoneNumber
        holder.idx.text = callNumber.id.toString()

        if (callNumber.phoneNumberState){

            holder.num.paintFlags = holder.num.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.num.setTextColor(Color.GRAY)
        }else{
            holder.num.paintFlags = 0
            holder.num.setTextColor(Color.BLACK)

        }

        holder.deleteButton.setOnClickListener(View.OnClickListener {


            CoroutineScope(Dispatchers.Main).launch{
                CoroutineScope(Dispatchers.Main).async{
                    removeData(holder.layoutPosition)
                }.await()
                onDeleteListener.delete(callNumber)

            }
            Toast.makeText(holder.deleteButton.context, "전화번호를 삭제했습니다.", Toast.LENGTH_SHORT).show()
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