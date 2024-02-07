package com.sc.lesa.mediashar.jlib.media

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface

open class Encoder(private val videoW: Int, private val videoH: Int, private val videoBitrate: Int,
                   private val videoFrameRate: Int, private var encoderListener: EncoderListener?)
    : Thread(TAG) {

    companion object {
        private const val TAG = "Encoder"
        private const val MIME = "Video/AVC"
    }

    private lateinit var codec: MediaCodec
    private lateinit var mSurface: Surface
    private val TIMEOUT_USEC = -1
    private lateinit var configbyte: ByteArray
    private var exit = false
    private val mBufferInfo = MediaCodec.BufferInfo()

    open fun init(){
        initMediaCodec()
    }

    fun getmSurface(): Surface {
        return mSurface
    }

    private fun initMediaCodec() {
        val format = MediaFormat.createVideoFormat(MIME, videoW, videoH)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface) //색상형식
        format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate) // 비트레이트
        format.setInteger(MediaFormat.KEY_FRAME_RATE, videoFrameRate) //프레임
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3) // 키 프레임 5초
        codec = MediaCodec.createEncoderByType(MIME)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mSurface = codec.createInputSurface()
        codec.start()
    }

    /**
     * h264 데이터 가져오기
     */
    override fun run() {
        try {
            while (!exit) {
                var outputBufferIndex = codec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC.toLong())
                while (outputBufferIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputBufferIndex)!!
                    val outData = ByteArray(mBufferInfo.size)
                    outputBuffer[outData]
                    if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) { // 코덱 초기화/ 특정 데이터 포함
                        configbyte = outData
                        Log.d(this.javaClass.name,"구성 생성")

                    } else if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) { // 키 프레임
                        Log.d(this.javaClass.name,"키 프레임 생성")
                        val keyframe = ByteArray(mBufferInfo.size + configbyte.size)
                        System.arraycopy(configbyte, 0, keyframe, 0, configbyte.size)
                        System.arraycopy(outData, 0, keyframe, configbyte.size, outData.size)
                        encoderListener!!.onH264(keyframe, 1, mBufferInfo.presentationTimeUs)
                    } else {
                        // 다른 프레임의 끝
                        encoderListener!!.onH264(outData, 2, mBufferInfo.presentationTimeUs)
                    }
                    codec.releaseOutputBuffer(outputBufferIndex, false)
                    outputBufferIndex = codec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC.toLong())
                }
            }
        } catch (e: Exception) {
            encoderListener?.onError(e)
            e.printStackTrace()
        }
        onClose()
    }

    open fun onClose() {
        codec.stop()
        codec.release()
        encoderListener?.onCloseH264()
        encoderListener = null
    }


    fun exit() {
        exit = true
    }

    interface EncoderListener {
        fun onH264(buffer: ByteArray, type: Int, ts: Long)
        fun onError(t:Throwable)
        fun onCloseH264()
    }
}