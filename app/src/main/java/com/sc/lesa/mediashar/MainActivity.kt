package com.sc.lesa.mediashar

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.DataBindingUtil
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.sc.lesa.mediashar.databinding.DialoInputadreassBinding
import java.util.Date

class MainActivity : AppCompatActivity(), View.OnClickListener {
    var preTime: Long = 0

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // 권한 승인됨
        } else {
            // 권한 승인되지 않음
        }
    }

    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    Toast.makeText(this@MainActivity, "알림 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } else {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else { }
        }
    }
// 추가
    val mInputModel = InputModel()
    lateinit var inputDialog: InputDialog
    lateinit var decorView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.first_activity_main)
        inputDialog = InputDialog(this)
        val imageBtn = findViewById<Button>(R.id.imageButton) // 추가
        imageBtn.setOnClickListener(this) // 추가
        val settingBtn = findViewById<Button>(R.id.settingBut) //add
        settingBtn.setOnClickListener(this) //add
        val wifisetting = findViewById<Button>(R.id.wifi) //add
        wifisetting.setOnClickListener(this) // 추가
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            CheckAndSetPermissions(this)
        }
        //추가했음
        decorView = window.decorView
        val textViewWifiIp = findViewById<TextView>(R.id.textViewWifiIp)
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        textViewWifiIp.text = resources.getString(R.string.disconnect)

//추가
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // UI 업데이트를 UI 스레드에서 수행
                runOnUiThread {
                    val wifiInfo: WifiInfo = wifiManager.connectionInfo
                    val ipAddress = wifiInfo.ipAddress
                    val ipString = String.format(
                        "%d.%d.%d.%d",
                        (ipAddress and 0xff),
                        (ipAddress shr 8 and 0xff),
                        (ipAddress shr 16 and 0xff),
                        (ipAddress shr 24 and 0xff)
                    )

                        textViewWifiIp.text = "Wi-Fi IP 주소: $ipString"

                    }

            }

            override fun onLost(network: Network) {
                runOnUiThread {
                    textViewWifiIp.text = resources.getString(R.string.disconnected)
                }
            }
        }
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
//추가
    override fun onResume() {
        super.onResume()
        requestNotificationPermission()
        val actionBar = supportActionBar
        //actionBar!!.hide()
        actionBar?.hide()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            window.statusBarColor = Color.TRANSPARENT
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }

    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onClick(v: View) {
        val id = v.id
        when (id) {
            R.id.but_main_share -> {
                val intent = Intent(this, MediaProjectionActivity::class.java)
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    // 액티비티를 시작할 수 없는 경우에 대한 처리
                    Log.e("MediaProjectionActivity", "Activity not found")
                }
            }

            R.id.but_main_seach -> {
                inputDialog.show(supportFragmentManager, this.javaClass.name)
            }
        } // 추가했음
        when (v.id) {
            R.id.imageButton -> {
           /*     val builder = AlertDialog.Builder(this)
                    .setTitle("도움말")
                    .setMessage("안녕하세요 테스트입니다.")
                    .setPositiveButton("확인",
                        DialogInterface.OnClickListener { dialog, which ->
                        })
                    .setNegativeButton("취소",
                        DialogInterface.OnClickListener { dialog, which ->
                        })
                Log.d("!@", "message")
                builder.show() */
                val intent = Intent(this, TutorialActivity::class.java)
                startActivity(intent)
            }
        }
        // 추가했음
        when (v.id) {
            R.id.settingBut -> {
                val intent = Intent(this, SettingActivity::class.java)
                intent.putExtra("from_activity", "MainActivity")
                startActivity(intent)
            }
        }
        // 추가했음
        when (v.id) {
            R.id.wifi -> {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivityForResult(intent, 0) // 설정 액티비티를 실행하고 결과를 받음
            }
        }

    }
//추가
override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
        val currentTime = Date().time
        if (currentTime - preTime > 2000) {
            Toast.makeText(this, getText(R.string.app_back_exit), Toast.LENGTH_SHORT).show()
            preTime = currentTime
        } else {
            finish()
        }
        return true
    }
    return super.onKeyDown(keyCode, event)
}
    companion object {
        fun CheckAndSetPermissions(activity: AppCompatActivity?) {
            val REQUEST = 1
            val PERMISSIONS = arrayOf(
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.FOREGROUND_SERVICE",
                Manifest.permission.RECORD_AUDIO
            )
            for (ps in PERMISSIONS) {
                val permission = ActivityCompat.checkSelfPermission(activity!!, ps)
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, PERMISSIONS, REQUEST)
                }
            }
        }
    }

    @SuppressLint("ValidFragment")
    class InputDialog(val mainactivity:MainActivity): DialogFragment() {
        lateinit var binding:DialoInputadreassBinding

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            binding = DataBindingUtil.inflate(inflater, R.layout.dialo_inputadreass, null, false)
            binding.model = mainactivity.mInputModel
            binding.callback = this
            return binding.root
        }

        override fun onStart() {
            super.onStart()
            val window = dialog?.window!!
            val params: WindowManager.LayoutParams = window.attributes
            params.gravity = Gravity.CENTER
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }

        fun onClick() {
            val intent = WatchContect.buildIntent(Intent(mainactivity, WatchContect::class.java), mainactivity.mInputModel.ipaddr)
            mainactivity.startActivity(intent)
            dismiss()


        }

        fun onCancel() {
            dismiss()
        }
    }

    class InputModel : BaseObservable() {
        @Bindable
        var ipaddr = "192.168.1.128"
            set(value) {
                field = value
                notifyChange()
            }
    }
}
