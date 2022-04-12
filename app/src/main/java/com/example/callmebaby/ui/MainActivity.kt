package com.example.callmebaby.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.callmebaby.R
import com.example.callmebaby.adapter.CallRecyclerAdapter
import com.example.callmebaby.common.SwipeHelperCallback
import com.example.callmebaby.data.CallEntity
import com.example.callmebaby.databinding.ActivityMainBinding
import com.example.callmebaby.service.MyService
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_file_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    var message: String = ""
    var isService = false
    var autoCallTotalNumListSize = 0 // 총 전화번호 개수
    var autoCallTureNumListSize = 0 // 전화한 전화번호 개수, 통화중인 전화 인덱스
    var phoneBook = listOf<CallEntity>() // 전화번호부

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CallViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
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

        getStoragePermission()
        getPermission()


        // DB에 저장된 번호를 recyclerview를 통해 UI에 뿌려준다.
        val mAdapter = CallRecyclerAdapter(this, viewModel)

        recyclerview.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(applicationContext)

        }

        // 리사이클러뷰에 스와이프, 드래그 기능 달기
        val swipeHelperCallback = SwipeHelperCallback(mAdapter).apply {
            // 스와이프한 뒤 고정시킬 위치 지정
            setClamp(resources.displayMetrics.widthPixels.toFloat() / 4)    // 1080 / 4 = 270
        }

        ItemTouchHelper(swipeHelperCallback).attachToRecyclerView(binding.recyclerview)

        // 구분선 추가
        binding.recyclerview.addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))

        // 다른 곳 터치 시 기존 선택했던 뷰 닫기
        binding.recyclerview.setOnTouchListener { _, _ ->
            swipeHelperCallback.removePreviousClamp(binding.recyclerview)
            false
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

                if(autoCallTotalNumListSize == 0){
                    binding.emptyView.visibility = View.VISIBLE
                    binding.recyclerview.visibility = View.GONE
                }else{
                    binding.emptyView.visibility = View.GONE
                    binding.recyclerview.visibility = View.VISIBLE

                }
            })

            viewModel.getTureAll().observe(this@MainActivity,{ falseNum ->


                autoCallTureNumListSize = falseNum.size
                binding.callCheck.text =
                    "전체 ${autoCallTotalNumListSize}개 중 ${autoCallTureNumListSize}번 째 통화 완료"

                if (autoCallTureNumListSize >= 1){
                    binding.CallButton.text = "다음 통화"

                }else if(autoCallTureNumListSize == 0){
                    binding.CallButton.text = "통화 걸기"

                }

            })
        }
    }

    fun onClickFileImportButton(view: View){

        val permissionListener = object : PermissionListener {
            @RequiresApi(Build.VERSION_CODES.R)
            override fun onPermissionGranted() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ){

                    if (Environment.isExternalStorageManager()) {
                        // 권한이 설정돼있으면 FileImportActivity 이동
                        val intent = Intent(this@MainActivity, FileImportActivity::class.java)
                        startActivity(intent)
                    }else{
                        val intent = Intent()
                        intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }

                }else{
                    // 권한이 설정돼있으면 FileImportActivity 이동
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
                        if(autoCallTureNumListSize < autoCallTotalNumListSize ){

                                phoneBook[autoCallTureNumListSize].phoneNumberState = true
                                viewModel.update(phoneBook[autoCallTureNumListSize])
                                callMe(phoneBook[autoCallTureNumListSize].phoneNumber)

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
                // 통화벨 울리는중
                TelephonyManager.CALL_STATE_OFFHOOK -> {

                }
                // 통화중
                TelephonyManager.CALL_STATE_RINGING -> {
                }
            }
        }
    }

    fun onClickCallButton(view: View){

        if(!isServiceRunningCheck()){
            callMission()
        }
    }

    // 버튼
    private fun callMission(){

        if (autoCallTureNumListSize < autoCallTotalNumListSize){
            val mTelephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager


            // 전화 연결
            val permissionListener = object : PermissionListener {
                @RequiresApi(Build.VERSION_CODES.R)
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
            if (autoCallTotalNumListSize == 0){
                message = "리스트에 전화번호가 없습니다."

            }else{
                message = "리스트에 있는 전화번호를 모두 통화했습니다."

            }
            Toast.makeText(this@MainActivity,message, Toast.LENGTH_SHORT).show()

        }

    }

    fun onClickAllDeleteDialogButton(view: View){

        // 다이어로그 생성
        val mHandler = Handler(Looper.getMainLooper())
        mHandler.post {
            val ad = AlertDialog.Builder(this@MainActivity)
            ad.setIcon(R.drawable.auto_call)
            ad.setTitle("전체 삭제")
            ad.setMessage("정말 전화번호를 전체 삭제하시겠습니까?\n전화번호 전체 삭제시 이전에 통화 했던 정보들이 사라집니다.")

            // 확인버튼
            ad.setPositiveButton("확인") { _, _ -> allDelete() }
            // 취소버튼
            ad.setNegativeButton("취소") { dialog, _ -> dialog.dismiss() }

            ad.show()
        }
    }


    private fun allDelete(){

        if (autoCallTotalNumListSize != 0){
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.deleteAll()
            }
            message = "모든 전화번호를 삭제했습니다."

        }else{
            message = "삭제할 전화번호가 없습니다."

        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()


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
    @RequiresApi(Build.VERSION_CODES.R)
    private fun getPermission() {
        // 지금 창이 오버레이 설정창이 아니라면
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse(
                "package:$packageName"))
            startActivityForResult(intent, 1)
        }
    }

    private fun getStoragePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            val uri = Uri.fromParts("package", this.packageName, null)
            intent.data = uri
            startActivity(intent)
        }

    }

    // Service 체크
    fun isServiceRunningCheck(): Boolean {
        val mTelephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val manager = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if ("com.example.callmebaby.service.MyService" == service.service.className) {
                return true
            }
        }
        mTelephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_NONE)

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
