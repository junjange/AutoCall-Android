package com.example.callmebaby

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil.setContentView
import com.example.callmebaby.service.MyService
import com.example.callmebaby.databinding.ActivityMainBinding
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission


class MainActivity : AppCompatActivity() {

    var isService = false
    var currPhoneNumIdx = 0 // 통화중인 전화 인덱스
    val autoCallNumList = listOf("01033032391","010215") // 전화 리스트
    private lateinit var binding: ActivityMainBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_main)
        binding.mainActivity = this

        binding.callCheck.text = "전체 ${autoCallNumList.size}개 중 ${currPhoneNumIdx}번 째 통화 완료"

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
            Log.d("ttt","왔어?${state}")
            Log.d("ttt", TelephonyManager.CALL_STATE_OFFHOOK.toString())

            binding.callCheck.text = "전체 ${autoCallNumList.size}개 중 ${currPhoneNumIdx}번 째 통화 완료"

            when (state) {
                TelephonyManager.CALL_STATE_IDLE -> {
                    Log.d("ttt", "통화종료 혹은 통화벨 종료")
                    Log.d("tttddd", isServiceRunningCheck().toString())

                    if(isServiceRunningCheck()){
                        // 전화할 번호가 있으면 통화
                        if(currPhoneNumIdx < autoCallNumList.size){
                            callMe(autoCallNumList[currPhoneNumIdx])
                            isService = true


                        }
                        else {
                            if (isService){
                                val intent = Intent(this@MainActivity, MyService::class.java)
                                stopService(intent)
                                isService = false

                            }
                        }

                        currPhoneNumIdx++
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

        if (currPhoneNumIdx < autoCallNumList.size){
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

    // 전화 연결
    private fun callMe(num : String){
        Log.d("ttt", "hi")

        val myUri = Uri.parse("tel:${num}")
        val intent = Intent(Intent.ACTION_CALL, myUri)
        startActivity(intent)

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
            Log.d("Tttdd", service.service.className.toString())
            if ("com.example.callmebaby.service.MyService" == service.service.className) {
                return true
            }
        }
        return false
    }

}
