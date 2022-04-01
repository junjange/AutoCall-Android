package com.example.callmebaby.service

import android.app.Service
import android.content.Intent
import android.os.IBinder


import android.graphics.PixelFormat

import android.os.Build
import android.util.Log
import android.view.*
import android.view.WindowManager.*
import com.example.callmebaby.R
import android.view.Gravity
import android.view.WindowManager
import android.view.LayoutInflater
import androidx.annotation.RequiresApi


class MyService : Service() {
    private var windowManager: WindowManager? = null
    var height = 0f
    var width = 0f
    private var floatingView: View? = null
    private var expandedView:View? = null
    var params: LayoutParams? = null


    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()


        //윈도우매니저 초기화
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // 우리가 만든 플로팅 뷰 레이아웃 확장
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_widget, null)

        //창에 위젯 아이콘 뷰를 추가
        params = LayoutParams(LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            LayoutParams.TYPE_APPLICATION_OVERLAY,
            LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT)

        // 보기 위치 지정
        // 처음에는보기가 왼쪽 중앙에 추가되며 필요에 따라 x-y 좌표를 변경
        params!!.gravity = Gravity.CENTER
        params!!.x = 0
        params!!.y = 100

        //윈도우에 뷰 추가
        windowManager!!.addView(floatingView, params)


        // 뷰 높이, 너비
        height = windowManager!!.defaultDisplay.height.toFloat()
        width = windowManager!!.defaultDisplay.width.toFloat()


        expandedView = floatingView!!.findViewById(R.id.stopText)

        expandedView!!.setOnClickListener {
            stopSelf()

        }

    }



    // 앱이 종료될때 실행
    override fun onDestroy() {
        super.onDestroy()
        Log.d("ttttt", floatingView.toString())
        Log.d("ttttt", expandedView.toString())

        if (floatingView != null) {
            windowManager!!.removeView(floatingView)
        }

    }
}