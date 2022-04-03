package com.example.callmebaby.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
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
import kotlinx.coroutines.launch
import android.os.Looper





class MainActivity : AppCompatActivity() {

    var isService = false
    var autoCallTotalNumListSize = 0 // 총 전화번호 개수
    var autoCallTureNumListSize = 0 // 전화한 전화번호 개수, 통화중인 전화 인덱스
    var phoneBook = listOf<CallEntity>() // 전화번호부

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CallViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.R)
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
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE

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

        CoroutineScope(Dispatchers.Main).launch  {
            viewModel.getAll().observe(this@MainActivity, Observer { num ->

                phoneBook = num
                autoCallTotalNumListSize = num.size
                binding.callCheck.text =
                    "전체 ${autoCallTotalNumListSize}개 중 ${autoCallTureNumListSize}번 째 통화 완료"

            })

            viewModel.getTureAll().observe(this@MainActivity,{ falseNum ->

                autoCallTureNumListSize = falseNum.size
                binding.callCheck.text =
                    "전체 ${autoCallTotalNumListSize}개 중 ${autoCallTureNumListSize}번 째 통화 완료"

            })
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

        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setDeniedMessage("[권한] 에서 저장공간 액세스 권한을 모든 파일 관리를 승인해야 파일 불러오기가 가능합니다.")
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            .check()
    }


    // 전화 상태 확인
    private val phoneListener = object : PhoneStateListener() {
        @SuppressLint("SetTextI18n")
        override fun onCallStateChanged(state: Int, incomingNumber: String) {

            binding.callCheck.text = "전체 ${autoCallTotalNumListSize}개 중 ${autoCallTureNumListSize}번 째 통화 완료"


            when (state) {
                TelephonyManager.CALL_STATE_IDLE -> {

                    if(isServiceRunningCheck()){

                        // 전화할 번호가 있으면 통화
                        if(autoCallTureNumListSize < autoCallTotalNumListSize){

                            phoneBook[autoCallTureNumListSize].phoneNumberState = true
                            viewModel.update(phoneBook[autoCallTureNumListSize])
                            callMe(phoneBook[autoCallTureNumListSize].phoneNumber)

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

            Log.d("ttt", "phone state : $state");
            Log.d("ttt", "phone currentPhoneNumber : $incomingNumber")
        }
    }

    // 버튼
    @RequiresApi(Build.VERSION_CODES.M)
    fun onClickCallButton(view: View){

        if (autoCallTureNumListSize < autoCallTotalNumListSize){
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
            val myUri = Uri.parse("tel:${num}")
            val intent = Intent(Intent.ACTION_CALL, myUri)
            startActivity(intent)

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

    // 뒤로가기 2번 눌러야 종료
    private val FINISH_INTERVAL_TIME: Long = 2500
    private var backPressedTime: Long = 0
    private var toast: Toast? = null
    override fun onBackPressed() {
        val tempTime = System.currentTimeMillis()
        val intervalTime = tempTime - backPressedTime


        // 뒤로 가기 할 경우 홈 화면으로 이동
        if (intervalTime in 0..FINISH_INTERVAL_TIME) {
            super.onBackPressed()
            // 앱 종료시 뒤로가기 토스트 종료
            toast!!.cancel()
            finish()
        } else {
            backPressedTime = tempTime
            toast =
                Toast.makeText(applicationContext, "'뒤로'버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT)
            toast!!.show()
        }
    }

}
