package com.sc.lesa.mediashar

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.KeyEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import com.sc.lesa.mediashar.jlib.server.SocketClientThread
import com.sc.lesa.mediashar.jlib.threads.VideoPlayThread
import com.sc.lesa.mediashar.jlib.threads.VoicePlayThread
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

class   WatchContect : AppCompatActivity(), SurfaceHolder.Callback {
    private lateinit var mSurfaceView: SurfaceView
    private lateinit var mSurfaceHolder: SurfaceHolder
    private lateinit var ip: String
    private lateinit var socketClientThread: SocketClientThread
    private var mediaPlayThread: VideoPlayThread? = null
    private var voicePlayThread: VoicePlayThread? = null

    companion object {
        fun buildIntent(intent: Intent, ip: String): Intent {
            intent.putExtra("Address", ip)
            return intent
        }
    }

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
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_contect)

        mSurfaceView = findViewById(R.id.surfaceView_watch)
        mSurfaceHolder = mSurfaceView.holder
        val intent = intent
        ip = intent.getStringExtra("Address")!!

        mSurfaceHolder.addCallback(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 액티비티가 종료될 때 수행해야 할 작업
        clear()
    }

    private fun init() {
        thread(true) {
            socketClientThread = ClientThread(ip)
            try {
                socketClientThread.connect()
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@WatchContect, "${getString(R.string.error)}:${e.message}", Toast.LENGTH_SHORT).show()
                }
                return@thread
            }
            socketClientThread.start()
            mediaPlayThread = VideoPlayThread(mSurfaceHolder.surface, socketClientThread.dataPackList)
            mediaPlayThread!!.start()
            voicePlayThread = VoicePlayThread(socketClientThread.dataPackList)
            voicePlayThread!!.start()
        }
    }

    private fun clear() {
        socketClientThread.exit()
        mediaPlayThread?.exit()
        voicePlayThread?.exit()
    }

    private inner class ClientThread(ip: String) : SocketClientThread(ip, 9090) {
        override fun onError(t: Throwable) {
            runOnUiThread {
                Log.d("!@@", "error:${getString(R.string.error)}:${t.message}")
                Toast.makeText(this@WatchContect, "서버가 종료되었습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        init()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Surface 변경 처리
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Surface 파괴 처리
    }
}
