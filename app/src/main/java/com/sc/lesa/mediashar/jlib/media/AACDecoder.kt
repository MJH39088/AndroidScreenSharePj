package com.sc.lesa.mediashar.jlib.media

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 *
 * @param ChannelCount 채널 수
 * [AacFormat.ChannleOutOne] 또는 [AacFormat.ChannleOutTwo]
 *
 * @param ByteRate 비트 전송률(예: 384000 256000 128000)
 * 지원되는 범위는 [AacFormat.ByteRate64Kbs]부터 [AacFormat.ByteRate384Kbs]까지입니다.
 *
 * @param SampleRate 샘플링 주파수
 * [AacFormat.SampleRate44100] [AacFormat.SampleRate48000]
 */
class AACDecoder(val ChannelCount: Int, val ByteRate: Int,val SampleRate: Int) {

    //디코더
    private lateinit var mDecoder: MediaCodec
    private val info = MediaCodec.BufferInfo()

    //디코딩 실패 횟수를 반환합니다.
    //디코딩에 실패한 프레임 수를 기록하는 데 사용됩니다.
    var count = 0
        private set

    private var onDecodeDone: OnDecodeDone? = null

    /**
     * 모든 변수 초기화
     * @throws Exception 인코더를 초기화하지 못했습니다.
     */
    fun init() {
        prepare()
    }

    fun setOnDecodeDone(onDecodeDone: OnDecodeDone) {
        this.onDecodeDone = onDecodeDone
    }

    /**
     * 디코더 초기화
     *
     * @return 초기화에 실패하면 false를 반환하고 성공하면 true를 반환합니다.
     */
    fun prepare(): Boolean {
        //디코딩해야 하는 데이터 유형
        val mine = "audio/mp4a-latm"
        //디코더 초기화
        mDecoder = MediaCodec.createDecoderByType(mine)
        //MediaFormat은 오디오 및 비디오 데이터의 관련 매개변수를 설명하는 데 사용됩니다.
        val mediaFormat = MediaFormat()
        //데이터 유형
        mediaFormat.setString(MediaFormat.KEY_MIME, mine)
        //채널 수
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, ChannelCount)
        //샘플링 속도
        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, SampleRate)
        //비트 전송률
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, ByteRate)
        //AAC에 adts 헤더가 있는지 여부를 표시하는 데 사용됩니다(1->예).
        mediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, 1)
        //aac를 표시하는 데 사용되는 유형
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        //ByteBuffer 키(아직 이 매개변수의 의미를 모르지만 설정해야 함)
        val data = byteArrayOf(0x11.toByte(), 0x90.toByte())
        val csd_0 = ByteBuffer.wrap(data)
        mediaFormat.setByteBuffer("csd-0", csd_0)
        //디코더 구성
        mDecoder.configure(mediaFormat, null, null, 0)

        mDecoder.start()
        return true
    }

    /**
     * aac解码
     */
    fun decode(buf: ByteArray, offset: Int, length: Int, ts: Long) {
        //대기시간, 0->기다리지 않음, -1->항상기다림
        val kTimeOutUs: Long = 300
        //유효한 데이터가 포함된 입력 버퍼의 인덱스를 반환합니다. -1-> 존재하지 않습니다.
        val inputBufIndex = mDecoder.dequeueInputBuffer(kTimeOutUs)
        if (inputBufIndex >= 0) {
            //현재 ByteBuffer를 가져옵니다.
            val dstBuf = mDecoder.getInputBuffer(inputBufIndex)!!
            //바이트버퍼 지우기
            dstBuf.clear()
            //데이터 투입
            dstBuf.put(buf, offset, length)
            //지정된 인덱스의 입력 버퍼를 디코더에 제출
            mDecoder.queueInputBuffer(inputBufIndex, 0, length, ts, 0)
        }
        //코덱 버퍼
        //출력 버퍼의 인덱스를 반환합니다. -1->존재하지 않습니다.
        var outputBufferIndex = mDecoder.dequeueOutputBuffer(info, kTimeOutUs)
        if (outputBufferIndex < 0) {
            //记录解码失败的次数
            count++
        }
        var outputBuffer: ByteBuffer
        while (outputBufferIndex >= 0) {
            //디코딩된 ByteBuffer 가져오기
            outputBuffer = mDecoder.getOutputBuffer(outputBufferIndex)!!
            //디코딩된 데이터를 저장하는 데 사용됩니다.
            val outData = ByteArray(info.size)
            outputBuffer[outData]
            //캐시 비우기
            outputBuffer.clear()
            //디코딩된 데이터 재생
            if (onDecodeDone != null) onDecodeDone!!.onDecodeData(outData, 0, info.size)
            //디코딩된 버퍼를 해제합니다.
            mDecoder.releaseOutputBuffer(outputBufferIndex, false)
            //완료되지 않은 데이터 디코딩
            outputBufferIndex = mDecoder.dequeueOutputBuffer(info, kTimeOutUs)
        }
    }

    /**
     * 리소스 해제
     */
    fun stop() {
        mDecoder.stop()
        mDecoder.release()
        onDecodeDone?.onClose()
    }

    interface OnDecodeDone {
        fun onDecodeData(bytes: ByteArray?, offset: Int, len: Int)
        fun onClose()
    }


}