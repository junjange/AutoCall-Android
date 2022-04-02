package com.example.callmebaby.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.Call
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.callmebaby.R
import com.example.callmebaby.adapter.CallRecyclerAdapter
import com.example.callmebaby.data.CallEntity
import com.example.callmebaby.service.MyService
import com.example.callmebaby.databinding.ActivityMainBinding
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    var isService = false
    var currPhoneNumIdx = 0 // 통화중인 전화 인덱스
    var autoCallNumListSize = 0
    var autoCallFalseNumListSize = 0
    var autoCallTotalNumListSize = 0
    var phoneBook  = listOf<CallEntity>()

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CallViewModel by viewModels()
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_main)
        binding.mainActivity = this


        // Get permission
        val permissionList = arrayOf<String>(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,

        )


        // 권한 요청
        ActivityCompat.requestPermissions(this@MainActivity, permissionList, 1)



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            getPermission()
        }

        // DB에 저장된 번호를 recyclerview를 통해 UI에 뿌려준다.
        val mAdapter = CallRecyclerAdapter(this, viewModel)

        recyclerview.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(applicationContext)

        }

        viewModel.allPhoneNumber.observe(this@MainActivity, Observer { call ->
            // Update the cached copy of the users in the adapter.
            call?.let { mAdapter.setUsers(it) }
        })

//
//        viewModel.getAll().observe(this, Observer { num ->
//
//
//            CoroutineScope(Dispatchers.Main).launch {
//                phoneBook = num
//                autoCallTotalNumListSize = num.size
//
//            }
//
//        })
//
//        viewModel.getFalseAll().observe(this,{ falseNum ->
//
//
//            CoroutineScope(Dispatchers.Main).launch {
//                autoCallFalseNumListSize = falseNum.size
//                autoCallNumListSize = autoCallTotalNumListSize - autoCallFalseNumListSize
//
//                binding.callCheck.text = "전체 ${autoCallTotalNumListSize}개 중 ${autoCallNumListSize}번 째 통화 완료"
//            }
//
//
//        })

        Log.d("Ttt2", autoCallFalseNumListSize.toString())

        CoroutineScope(Dispatchers.Main).launch {

            viewModel.getAll().observe(this@MainActivity, Observer { num ->


                CoroutineScope(Dispatchers.IO).launch {
                    phoneBook = num
                    autoCallTotalNumListSize = num.size

                }

            })

            viewModel.getFalseAll().observe(this@MainActivity,{ falseNum ->


                CoroutineScope(Dispatchers.IO).launch {
                    autoCallFalseNumListSize = falseNum.size
                    autoCallNumListSize = autoCallTotalNumListSize - autoCallFalseNumListSize

                }


            })

        }

        CoroutineScope(Dispatchers.Main).launch {
            binding.callCheck.text = "전체 ${autoCallTotalNumListSize}개 중 ${autoCallNumListSize}번 째 통화 완료"


        }








    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun onClickFileImportButton(view: View){

        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {

                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    getPermission()
                } else {
                    // 권한이 설정돼있으면 MyService 실행
                    val intent = Intent(this@MainActivity, FileImportActivity::class.java)
                    startActivity(intent)


                }
            }
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                Toast.makeText(this@MainActivity,"저장공간 액세스 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setDeniedMessage("[권한] 에서 저장공간 액세스 권한을 모든 파일 관리를 승인해야 파일 불러오기가 가능합니다.")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .check()
        }


    }


    // 전화 상태 확인
    private val phoneListener = object : PhoneStateListener() {
        @SuppressLint("SetTextI18n")
        override fun onCallStateChanged(state: Int, incomingNumber: String) {

            binding.callCheck.text = "전체 ${autoCallTotalNumListSize}개 중 ${autoCallNumListSize + currPhoneNumIdx}번 째 통화 완료"

            Log.d("ttt1", autoCallFalseNumListSize.toString())



            when (state) {
                TelephonyManager.CALL_STATE_IDLE -> {

                    if(isServiceRunningCheck()){

                            // 전화할 번호가 있으면 통화
                        if(autoCallNumListSize + currPhoneNumIdx < autoCallTotalNumListSize){
                            Log.d("ttt", phoneBook.toString())

                            phoneBook[currPhoneNumIdx].phoneNumberState = true
                            viewModel.update(phoneBook[currPhoneNumIdx])
                            callMe(phoneBook[currPhoneNumIdx].phoneNumber)

                            recyclerview.apply {
                                layoutManager = LinearLayoutManager(applicationContext)

                            }

                            isService = true

                        }
                        else {
                            if (isService){
                                val intent = Intent(this@MainActivity, MyService::class.java)
                                stopService(intent)
                                isService = false

                            }
                        }


                    }


                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    Log.d("ttt", "통화벨 울리는중")

                }
                TelephonyManager.CALL_STATE_RINGING -> {
                    Log.d("ttt", "통화중")
                }
            }
            Log.d("Dddd",isService.toString())

            Log.d("ttt", "phone state : $state");
            Log.d("ttt", "phone currentPhoneNumber : $incomingNumber")
        }
    }

    // 버튼
    @RequiresApi(Build.VERSION_CODES.M)
    fun onClickCallButton(view: View){

        if (autoCallNumListSize + currPhoneNumIdx < autoCallTotalNumListSize){
            val mTelephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            // 전화 연결
            val permissionListener = object : PermissionListener {
                override fun onPermissionGranted() {

                    if (!Settings.canDrawOverlays(this@MainActivity)) {
                        getPermission()
                    } else {
                        // 권한이 설정돼있으면 MyService 실행
                        val intent = Intent(this@MainActivity, MyService::class.java)
                        startService(intent)

                        mTelephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE)

                    }
                }
                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(this@MainActivity,"전화 연결 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setDeniedMessage("[권한] 에서 전화 액세스 권한을 승인해야 전화 연결이 가능합니다.")
                .setPermissions(Manifest.permission.CALL_PHONE)
                .check()

        }else{
            Toast.makeText(this@MainActivity,"리스트에 있는 번호를 모두 통화 연결 했습니다.", Toast.LENGTH_SHORT).show()

        }

    }

    fun onClickAllDeleteButton(view: View){
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deleteAll()
        }

    }

    // 전화 연결
    private fun callMe(num : String){

        if(isServiceRunningCheck()){
            Log.d("ttt", currPhoneNumIdx.toString())
            val myUri = Uri.parse("tel:${num}")
            val intent = Intent(Intent.ACTION_CALL, myUri)
            startActivity(intent)
            currPhoneNumIdx++



        }




    }

    // M 버전(안드로이드 6.0 마시멜로우 버전) 보다 같거나 큰 API에서만 설정창 이동 가능
    private fun getPermission() {
        // 지금 창이 오버레이 설정창이 아니라면
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse(
                "package:$packageName"))
            startActivityForResult(intent, 1)
        }
    }

    // Service 체크
    fun isServiceRunningCheck(): Boolean {
        val manager = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if ("com.example.callmebaby.service.MyService" == service.service.className) {
                return true
            }
        }
        return false
    }

}
