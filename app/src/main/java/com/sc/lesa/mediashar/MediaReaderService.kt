package com.sc.lesa.mediashar

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sc.lesa.mediashar.config.Config
import com.sc.lesa.mediashar.jlib.server.SocketServerThread
import com.sc.lesa.mediashar.jlib.threads.VideoSender
import com.sc.lesa.mediashar.jlib.threads.VoiceSender


class MediaReaderService : Service() {

    companion object {
        private val TAG = MediaReaderService::class.java.simpleName
        const val START_SERVER = 1
        const val STOP_SERVER = 2
        private const val UNLOCK_NOTIFICATION_CHANNEL_ID = "unlock_notification"
        private const val NOTIFICATION_ID_ICON = 1
    }

    var serverStatus = STOP_SERVER

    lateinit var socketServerThread: SocketServerThread
    lateinit var videoSender: VideoSender
    lateinit var voiceSender: VoiceSender
    lateinit var myApplication: MyApplication
    val handler = Handler(Looper.getMainLooper())
  
    override fun onCreate() {
        super.onCreate()
        myApplication = application as MyApplication
        initNotificationChannel()
        Log.d("!@", "onCreate()1111")
    }

    private fun startSendServer() {
        serverStatus = START_SERVER
//        buildNotification(
//            R.mipmap.ic_launcher,
//            getString(R.string.app_name),
//            getString(R.string.app_title_running)
//        )
        socketServerThread = SendThread()
        socketServerThread.start()
        val config: Config = Config.getConfig(this)
        try {
            videoSender = VideoSender(
                socketServerThread, myApplication.mediaProjection,
                config.width.toInt(), config.height.toInt(),
                config.videoBitrate.toInt(), config.videoFrameRate.toInt()
            )
            voiceSender = VoiceSender(
                socketServerThread,
                config.channelMode, config.encodeFormat, config.channelCount.toInt(),
                config.voiceByteRate.toInt(), config.voiceSampleRate.toInt()
            )
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            return
        }

    }

    private fun stopServer() {
        if (::videoSender.isInitialized) {
            videoSender.exit()
        }
        if (::voiceSender.isInitialized) {
            voiceSender.exit()
        }
        if (::socketServerThread.isInitialized) {
            socketServerThread.exit()
        }
        stopForeground(true)
        deleteNotification()
        serverStatus = STOP_SERVER

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("!@", "onstart2222")
        intent?.let {

            val cmd = it.getIntExtra("CMD", 0)
            when (cmd) {
                START_SERVER -> {
                    startSendServer()
                    serverStatus = START_SERVER
                    Log.d("!@@","serverstatus:$serverStatus")

                }
                STOP_SERVER -> {
                    stopServer()
                    serverStatus = STOP_SERVER
                    Log.d("!@@","serverstatus:$serverStatus")
                }

                else -> {}
            }

        }

        buildNotification(
            R.mipmap.ic_launcher,
            getString(R.string.app_name),
            getString(R.string.app_title_running)
        )

        // START_STICKY를 반환하여 서비스가 종료되어도 시스템이 자동으로 다시 시작하도록 합니다.
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d("!@", "onDestroy()3333")
        super.onDestroy()
        deleteNotification()
    }



    private fun initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 노티피케이션 채널 생성
            val name: CharSequence = "Running Notification"
            val description = "Service is running"
            val channelId = UNLOCK_NOTIFICATION_CHANNEL_ID // 채널 ID
            val importance = NotificationManager.IMPORTANCE_DEFAULT // 중요도 수준
            val mChannel = NotificationChannel(channelId, name, importance)
            mChannel.description = description // 채널 설명
            mChannel.enableLights(false) // 통지 램프 표시 여부
            mChannel.enableVibration(false) // 진동 여부
            val notificationManager = getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            notificationManager.createNotificationChannel(mChannel) // 노티피케이션 채널 생성
        }
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun buildNotification(resId: Int, title: String, contentText: String) {
        val builder = NotificationCompat.Builder(this, UNLOCK_NOTIFICATION_CHANNEL_ID) // 수정된 채널 ID 사용

        // 필수 노티피케이션 내용
        builder.setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(resId)
        val notifyIntent = Intent(this, MediaProjectionActivity::class.java)
        val notifyPendingIntent =
            PendingIntent.getService(
                this, 0, notifyIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        builder.setContentIntent(notifyPendingIntent)
        val notification = builder.build()
        //      notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
        //     notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
     //   notification.flags = notification.flags and (Notification.FLAG_ONGOING_EVENT.inv() or Notification.FLAG_NO_CLEAR.inv())
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID_ICON, notification)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID_ICON, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else {
            startForeground(NOTIFICATION_ID_ICON, notification)
        }

    }

    private fun deleteNotification() {
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.cancel(NOTIFICATION_ID_ICON)
    }

    enum class ServerStatus {
        UNSTART,
        STARTED
    }

    private inner class SendThread : SocketServerThread(9090) {
        override fun onError(t: Throwable) {
            myApplication.serverStatus = ServerStatus.UNSTART
            handler.post {
                Toast.makeText(
                    this@MediaReaderService,
                    "${getString(R.string.server_error_start)}:${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
