package com.sc.lesa.mediashar.jlib.media

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.io.IOException

class Decoder(width: Int, height: Int, fps: Int, surface: Surface) {
    private lateinit var mCodec: MediaCodec
    private val mSurface: Surface
    private var VIDEO_WIDTH = 1440
    private var VIDEO_HEIGHT = 2560
    private var TIME_INTERNAL = 24 // 비디오 프레임 속도
    var mCount = 0

    companion object {
        // Video Constants
        private const val MIME_TYPE = "video/avc" // H.264 Advanced Video
    }

    /**
     * @throws Exception 디코더 초기화 실패
     */
    fun init() {
        mCodec = MediaCodec.createDecoderByType(MIME_TYPE)
        val mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,
                VIDEO_WIDTH, VIDEO_HEIGHT)
        mCodec.configure(mediaFormat, mSurface,
                null, 0)
        mCodec.start()
    }


    fun onFrame(buf: ByteArray, offset: Int, length: Int, ts: Long): Boolean {
        // Get input buffer index
        try {
            val inputBufferIndex = mCodec.dequeueInputBuffer(10000)
            if (inputBufferIndex >= 0) {
                val inputBuffer = mCodec.getInputBuffer(inputBufferIndex)!!
                inputBuffer.clear()
                inputBuffer.put(buf, offset, length)
                mCodec.queueInputBuffer(inputBufferIndex, 0, length, ts, 0)
                mCount++
            } else {
                return false
            }

            // Get output buffer index
            val bufferInfo = MediaCodec.BufferInfo()
            var outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 100)
            while (outputBufferIndex >= 0) {
                mCodec.releaseOutputBuffer(outputBufferIndex, true)
                outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0)
            }
            return true
        } catch (e: Throwable) {
            Log.e("Media", "onFrame failed")
            e.printStackTrace()
            return false
        }
    }

    fun release() {
        mCodec.stop()
        mCodec.release()
    }

    init {
        VIDEO_WIDTH = width
        VIDEO_HEIGHT = height
        TIME_INTERNAL = fps
        mSurface = surface
    }
}