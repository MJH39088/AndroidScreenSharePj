package com.sc.lesa.mediashar.jlib.media

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log

/**
 *
 * @param ChannelCount 채널 수
 * [AacFormat.CannleOutOne] 또는 [AacFormat.CannleOutTwo]
 *
 * @param ByteRate 384000 256000 128000과 같은 비트 전송률
 * 지원되는 범위는 [AacFormat.ByteRate64Kbs]부터 [AacFormat.ByteRate384Kbs]까지입니다.
 *
 * @param SampleRate 샘플링 주파수
 * [AacFormat.SampleRate44100] [AacFormat.SampleRate48000]
 */
class AACEncoder(val ChannelCount: Int,val ByteRate: Int, val SampleRate: Int) {

    companion object {
        private val TAG = AACEncoder::class.java.name
    }

    init {
        Log.d(TAG, "ChannelCount:$ChannelCount ByteRate:$ByteRate SampleRate:$SampleRate")
    }

    var NOW_TIME = System.currentTimeMillis()

    private lateinit var mEncoder: MediaCodec
    private val  mBufferInfo= MediaCodec.BufferInfo()

    private val MIME_TYPE = "audio/mp4a-latm"

    private val KEY_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC
    private var mFrameByte: ByteArray?=null
    private var onEncodeDone: OnEncodeDone? = null


    /**
     * @throws Exception 인코더를 초기화하지 못했습니다.
     */
    fun init() {
        NOW_TIME = System.currentTimeMillis()

        mEncoder = MediaCodec.createEncoderByType(MIME_TYPE)
        val mediaFormat = MediaFormat.createAudioFormat(MIME_TYPE,
                SampleRate, ChannelCount)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, ByteRate)
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                KEY_AAC_PROFILE)
        mEncoder.configure(mediaFormat, null, null,
                MediaCodec.CONFIGURE_FLAG_ENCODE)
        mEncoder.start()
    }

    fun setOnEncodeDone(onEncodeDone: OnEncodeDone) {
        this.onEncodeDone = onEncodeDone
    }

    fun encode(data: ByteArray) {
        val inputBufferIndex = mEncoder.dequeueInputBuffer(-1)
        if (inputBufferIndex >= 0) {
            val inputBuffer = mEncoder.getInputBuffer(inputBufferIndex)!!
            inputBuffer.clear()
            inputBuffer.put(data)
            inputBuffer.limit(data.size)
            mEncoder.queueInputBuffer(inputBufferIndex, 0, data.size,
                    System.nanoTime(), 0)
        }
        var outputBufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, 0)
        while (outputBufferIndex >= 0) {
            val outputBuffer = mEncoder.getOutputBuffer(outputBufferIndex)!!
            //adts 헤더 필드에 7바이트를 비워 두세요.
            val length = mBufferInfo.size + 7

            if (mFrameByte == null ) {
                mFrameByte = ByteArray(length)
            }
            mFrameByte?.also {
                if (it.size<length){
                    mFrameByte=ByteArray(length)
                }
            }
            AacAdstUtil.addADTStoPacketType(mFrameByte, AacAdstUtil.TYPE_MEPG_2, AacAdstUtil.UNUSE_CRC,
                    AacAdstUtil.AAC_LC,
                    if (SampleRate == AacFormat.SampleRate44100) AacAdstUtil.SAMPLING_RATE_44_1KHZ else AacAdstUtil.SAMPLING_RATE_48KHZ,
                    ChannelCount, length)
            outputBuffer[mFrameByte, 7, mBufferInfo.size]
            if (onEncodeDone != null) onEncodeDone!!.onEncodeData(mFrameByte!!, 0, length,
                    System.currentTimeMillis() - NOW_TIME)
            mEncoder.releaseOutputBuffer(outputBufferIndex, false)
            outputBufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, 0)
        }
    }

    fun release() {
        mEncoder.stop()
        mEncoder.release()
        if (onEncodeDone != null) {
            onEncodeDone!!.onClose()
            onEncodeDone = null
        }
    }

    interface OnEncodeDone {
        fun onEncodeData(bytes: ByteArray, offset: Int, len: Int, ts: Long)
        fun onClose()
    }
}