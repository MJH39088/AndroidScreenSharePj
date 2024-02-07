package com.sc.lesa.mediashar.jlib.threads

import android.media.projection.MediaProjection
import android.util.Log
import com.sc.lesa.mediashar.jlib.io.VideoPack
import com.sc.lesa.mediashar.jlib.media.Encoder.EncoderListener
import com.sc.lesa.mediashar.jlib.media.MediaReader
import com.sc.lesa.mediashar.jlib.server.SocketServerThread

/**
 *
 * @param st 전송 스레드
 * @param mp  MediaProjection
 * @param width 비디오 너비 1080
 * @param height 비디오 높이 1920
 * @param videoBitrate 비디오 비트레이트 16777216
 * @param videoFrameRate 비디오 프레임 속도 24
 */
class VideoSender(var socketServerThread: SocketServerThread, mp: MediaProjection,
                  var width: Int, var height: Int,
                  var videoBitrate: Int, var videoFrameRate: Int
) : EncoderListener {

    val TAG = VideoSender::class.java.name
    var mediaReader: MediaReader = MediaReader(width, height, videoBitrate,
            videoFrameRate, this, mp)

    override fun onH264(buffer: ByteArray, type: Int, ts: Long) {
        val datas = ByteArray(buffer.size)
        System.arraycopy(buffer, 0, datas, 0, buffer.size)
        val pack = VideoPack(datas, width, height, videoBitrate,
                videoFrameRate, type, ts)
        socketServerThread.putVideoPack(pack)
    }

    override fun onError(t: Throwable) {

    }

    fun exit() {
        Log.d(TAG, "비디오샌더 종료")
        mediaReader.exit()
    }

    override fun onCloseH264() {
        Log.d(TAG, "비디오샌더 종료 완료")
    }


    init {
        mediaReader.init()
        mediaReader.start()
    }
}