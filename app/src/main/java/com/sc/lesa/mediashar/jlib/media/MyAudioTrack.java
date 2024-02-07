package com.sc.lesa.mediashar.jlib.media;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class MyAudioTrack {


    private int mFrequency;// 샘플링 속도
    private int mChannel;// 성대
    private int mSampBit;// 샘플링 정확도
    private AudioTrack mAudioTrack;
    private int mStreamType;

    /**
     *
     * @param frequency 샘플링 주파수
     * {@link AacFormat#SampleRate44100} {@link AacFormat#SampleRate48000}
     *
     * @param channel 채널
     * See {@link AudioFormat#CHANNEL_OUT_MONO} and {@link AudioFormat#CHANNEL_OUT_STEREO}
     *
     * @param sampbit 샘플링 정확도
     * See {@link AudioFormat#ENCODING_PCM_16BIT},{@link AudioFormat#ENCODING_PCM_8BIT}
     *
     * @param streamType 시스템 오디오 유형
     *{@link AudioManager#STREAM_VOICE_CALL}, {@link AudioManager#STREAM_SYSTEM},
     *{@link AudioManager#STREAM_RING}, {@link AudioManager#STREAM_MUSIC},
     *{@link AudioManager#STREAM_ALARM}, and {@link AudioManager#STREAM_NOTIFICATION}.
     */
    public MyAudioTrack(int frequency, int channel, int sampbit,int streamType) {
        this.mFrequency = frequency;
        this.mChannel = channel;
        this.mSampBit = sampbit;
        this.mStreamType=streamType;
    }

    /**
     * 초기화
     */
    public void init() {
        if (mAudioTrack != null) {
            release();
        }
        // 빌드된 객체의 최소 버퍼 크기를 가져옵니다.
        int minBufSize = getMinBufferSize();
        mAudioTrack = new AudioTrack(mStreamType,
                mFrequency, mChannel, mSampBit, minBufSize, AudioTrack.MODE_STREAM);
        mAudioTrack.play();
    }

    /**
     * 리소스 해제
     */
    public void release() {
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
        }
    }

    /**
     * 재생을 위해 디코딩된 pcm 데이터를 audioTrack에 기록합니다.
     *
     * @param data   데이터
     * @param offset 오프셋
     * @param length 재생할 길이
     */
    public void playAudioTrack(byte[] data, int offset, int length) {
        if (data == null || data.length == 0) {
            return;
        }
        try {
            mAudioTrack.write(data, offset, length);
        } catch (Exception e) {
            Log.e("MyAudioTrack", "AudioTrack Exception : " + e.toString());
        }
    }

    public int getMinBufferSize() {
        return AudioTrack.getMinBufferSize(mFrequency,
                mChannel, mSampBit);
    }

}
