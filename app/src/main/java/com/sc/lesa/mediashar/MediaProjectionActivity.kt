package com.sc.lesa.mediashar

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.DataBindingUtil
import com.sc.lesa.mediashar.databinding.StratServerActivityBinding
import java.util.Date

class MediaProjectionActivity : AppCompatActivity(), View.OnClickListener {

    private var isForegroundServiceStarted = false
    lateinit var myApplication: MyApplication
    lateinit var mediaProjectionManager: MediaProjectionManager
    var preTime: Long = 0
    lateinit var binding: StratServerActivityBinding
    private lateinit var testLauncher: ActivityResultLauncher<Intent>
    private lateinit var Button_Setting: Button //추가
    private lateinit var Button_Help : Button //add
    private lateinit var Button_Set: Button //add
    private lateinit var Button_del:Button //add
    private lateinit var Button_server:Button //add
    companion object {
        const val REQUEST_MEDIA_PROJECTION = RESULT_OK

    }

    val viewmodel = ViewModel()

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.strat_server_activity)
        binding.model = viewmodel
        myApplication = application as MyApplication
        Button_Setting = findViewById(R.id.but_server_setting)// 추가했음
        Button_Set = findViewById(R.id.settingBut_1) //추가했음
        Button_Help = findViewById(R.id.imageButton_1) //추가했음
        Button_del = findViewById(R.id.delete)  //add
        Button_server=findViewById(R.id.but_server_start)   //add
        //   val intent = intent
        /*   if (intent != null) {
               val message = intent.getStringExtra("message")
               if (message != null && message == "exit_media_projection") {
                   // Handle the message and perform any necessary actions
                   // This is where you can return to the MediaProjection class
               }
           } */

        //추가
        Button_Setting.visibility =
            if (myApplication.serverStatus == MediaReaderService.ServerStatus.UNSTART) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }

        //추가
        Button_Set.setOnClickListener{
            val intent = Intent(this, SettingActivity::class.java)
            intent.putExtra("from_activity", "MediaProjectionActivity")
            startActivity(intent)
        } //추가
        Button_Help.setOnClickListener {
            /*   val builder = AlertDialog.Builder(this)
                   .setTitle("도움말")
                   .setMessage("안녕하세요 테스트입니다-1")
                   .setPositiveButton("확인",
                       DialogInterface.OnClickListener { dialog, which ->
                       })
                   .setNegativeButton("취소",
                       DialogInterface.OnClickListener { dialog, which ->
                       })
               Log.d("!@", "message-1")
               builder.show() */
            val intent = Intent(this, TutorialActivity::class.java)
            startActivity(intent)
        }
        //추가
        Button_del.setOnClickListener {
            stopService(intent)
            stopForegroundServiceIfNeeded()
            Button_server.visibility=View.VISIBLE
            //버튼 이미지 변경
            Button_server.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.cast_main),null,null,null) ;
            Button_del.visibility=View.INVISIBLE
            Button_Setting.visibility = View.VISIBLE
        }
        // ActivityResultLauncher 설정
        testLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == REQUEST_MEDIA_PROJECTION) {
                    val data: Intent? = result.data
                    Log.d("!@", "onCreate: ${result.data}")
                    if (data != null) {
                        Log.d("!@", "onCreate: 1111")

                        // 미디어 프로젝션을 가져오기 전에 포그라운드 서비스를 시작
                        startForegroundServiceIfNeeded()

                        Handler(Looper.getMainLooper()).postDelayed({
                            val mediaProjection = mediaProjectionManager.getMediaProjection(result.resultCode, data)    //add
                            if (mediaProjection == null) {
                                // 권한 부여를 사용자에게 요청
                                testLauncher.launch(data)
                            } else {
                                // mediaProjection을 사용하여 계속 진행
                                myApplication.mediaProjection = mediaProjection
                                startServer()

                                // 미디어 프로젝션을 가져온 후에 서비스를 포그라운드에서 백그라운드로 전환
//                            stopForegroundServiceIfNeeded()

                                viewmodel.step = ModelStatus.STARTED
                                myApplication.serverStatus = MediaReaderService.ServerStatus.STARTED
                                Button_server.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.stop_main),null,null,null) ;

                            }
                        }, 1000)
                    } else {
                        Log.d("!@", "onCreate: 4444")
                        Log.e(this.javaClass.name, "data is null")
                    }
                } else {
                    viewmodel.step = ModelStatus.UNSTART
                }
            }
    }

    private fun startForegroundServiceIfNeeded() {
        if (!isForegroundServiceStarted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(this, MediaReaderService::class.java)
            intent.putExtra("CMD", 0)
            startForegroundService(intent)
            isForegroundServiceStarted = true
        }
    }

    private fun stopForegroundServiceIfNeeded() {
        if (isForegroundServiceStarted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(this, MediaReaderService::class.java)
            intent.putExtra("CMD", 0)
            stopService(intent)
            isForegroundServiceStarted = false
        }
    }

    //추가
    override fun onResume() {
        super.onResume()
        val actionBar = supportActionBar
        actionBar?.hide()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            window.statusBarColor = Color.TRANSPARENT
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
        if (myApplication.serverStatus == MediaReaderService.ServerStatus.UNSTART) {
            viewmodel.step = ModelStatus.UNSTART
        } else {
            viewmodel.step = ModelStatus.STARTED
        }

    }


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onClick(v: View) {

        Log.d("!@@", "startServer: onClick")
        if (v.id == R.id.but_server_start) {
            Log.d("!@@", "startServer: onClick11")
            if (viewmodel.step == ModelStatus.UNSTART) {
                Log.d("!@@", "startServer: 2222")
                viewmodel.step = ModelStatus.STARTING
                requestCapturePermission()
                //버튼 이미지 변경
//                Button_server.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.stop_main),null,null,null) ;


            } else if (viewmodel.step == ModelStatus.STARTED) {
                Log.d("!@@", "startServer: 33333")
                //stop
                stopServer()
                viewmodel.step = ModelStatus.UNSTART
                myApplication.serverStatus = MediaReaderService.ServerStatus.UNSTART
                Button_server.visibility=View.INVISIBLE
                Button_del.visibility=View.VISIBLE
            }

        } else if (v.id == R.id.but_server_setting) {
            Log.d("!@", "setting")
            val intent = Intent(this, SettingParam::class.java)
            startActivity(intent)
        }
        else if (v.id == R.id.delete) {
            Log.d("!@", "del")
            stopService(intent)
            stopForegroundServiceIfNeeded()
        }

    }


    //추가wnd()
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val currentTime = Date().time
            if (currentTime - preTime > 2000) {
                Toast.makeText(this, getText(R.string.app_back_exit), Toast.LENGTH_SHORT).show()
                preTime = currentTime
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun requestCapturePermission() {

        Handler(Looper.getMainLooper()).postDelayed({

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {

                Log.d("!@", "startServer: 5555")

                // 5.0 이상에서는 화면 캡처 권한 요청 가능
                mediaProjectionManager =
                    getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                testLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())

            } else {
                Log.d("!@", "startServer: 66666")

                Toast.makeText(this, "안드로이드 버전이 5.0 이하입니다!", Toast.LENGTH_SHORT).show()
            }

        }, 200)
    }
    // 추가했음
    fun startServer() {
        Log.d("!@@","startserver: 123")
        val intent = Intent(this, MediaReaderService::class.java)
        intent.putExtra("CMD", 1)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android Oreo (API 26) 이상에서는 startForegroundService()를 사용해야 합니다.
            startForegroundService(intent)
            Button_Setting.visibility = View.INVISIBLE
        } else {
//                stopService(intent)
            startService(intent)
        }
    }

    // 추가했음
    fun stopServer() {
        Log.d("!@", "stopServer: 123")
        val intent = Intent(this, MediaReaderService::class.java)
        intent.putExtra("CMD",2)
        startService(intent)
//        stopForegroundServiceIfNeeded()

//        Button_Setting.visibility = View.VISIBLE


    }


    //추가wnd()
    inner class ViewModel : BaseObservable() {

        var step = ModelStatus.UNSTART
            @SuppressLint("UseCompatLoadingForDrawables")
            set(value) {
                when (value) {
                    ModelStatus.UNSTART -> {
                        Log.d("!@@","button text unstart")
                        buttontext = getString(R.string.app_but_share)
                        buttonenable = true
                    }
                    ModelStatus.STARTING -> {
                        Log.d("!@@","button text starting")
                        buttontext = getString(R.string.app_but_share)
                        buttonenable = false


                    }
                    ModelStatus.STARTED -> {
                        Log.d("!@@","button text started")
                        buttontext = getString(R.string.app_but_stop)
                        buttonenable = true

                    }

                }
                field = value

            }

        @Bindable
        var buttontext = ""
            set(value) {
                field = value
                notifyPropertyChanged(BR.buttontext)
            }

        @Bindable
        var buttonenable = true
            set(value) {
                field = value
                notifyPropertyChanged(BR.buttonenable)
            }
    }

    enum class ModelStatus {
        UNSTART,
        STARTING,
        STARTED,
        deleted
    }
}
