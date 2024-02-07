package com.sc.lesa.mediashar.jlib.media;

public class AacAdstUtil {
    public static final boolean TYPE_MEPG_4 = false;
    public static final boolean TYPE_MEPG_2 = true;

    public static final boolean USE_CRC = false;
    public static final boolean UNUSE_CRC = true;

    public static final int AAC_MAIN = 1;
    public static final int AAC_LC = 2;
    public static final int AAC_SSR = 3;
    public static final int AAC_LTP = 4;

    public static final int SAMPLING_RATE_44_1KHZ = 0x4;
    public static final int SAMPLING_RATE_48KHZ = 0x3;

    /**
     * 인코딩된 aac 네이키드 스트림에 adts 헤더 필드 추가
     * @param packet 원본 데이터 스트림
     *
     * @param ID MPEG 식별자. 0은 MPEG-4, 1은 MPEG-2를 의미합니다.
     * 예: {@link AacAdstUtil#TYPE_MEPG_4} 및 {@link AacAdstUtil#TYPE_MEPG_2}
     *
     * @param protection_absent 는 오류 검사 수행 여부를 식별합니다. 0은 CRC 검사가 있음을 의미하고, 1은 CRC 검사가 없음을 의미합니다.
     * {@link AacAdstUtil#UNUSE_CRC} 및 {@link AacAdstUtil#USE_CRC}
     *
     * @param profile 은 사용할 AAC 수준을 식별합니다. 1: AAC 메인 2:AAC LC(낮은 복잡도) 3:AAC SSR(확장 가능한 샘플링 속도) 4:AAC LTP(장기 예측)
     * {@link AacAdstUtil#AAC_LC}
     *
     * @param sampling_frequency_index 사용된 샘플링 속도를 식별하는 인덱스
     * {@link AacAdstUtil#SAMPLING_RATE_44_1KHZ}
     *
     * @param channel_configuration 은 채널 수를 식별합니다.
     * @param packetLen ADTS 프레임 길이에는 ADTS 길이와 AAC 사운드 데이터 길이의 합이 포함됩니다.
     */
    public static void addADTStoPacketType(byte[] packet,boolean ID,boolean protection_absent,
                                           int profile,int sampling_frequency_index,
                                           int channel_configuration,int packetLen
    ) {

        byte b1 = (byte) 0xFF;//동기화 헤더는 항상 0xFFF이고 모든 비트는 1이어야 하며 이는 ADTS 프레임의 시작을 나타냅니다.

        byte b2 = (byte) 0b11110000;
        if (ID){//MPEG 식별자, 0은 MPEG-4를 식별하고 1은 MPEG-2를 식별합니다.
            b2=(byte)(b2|(byte) 0b00001000);
        }else {
            b2=(byte)(b2|(byte) 0b00000000);
        }
        b2 |= (byte)0b00000000;//Layer always: '00'

        if (protection_absent){//오류를 확인할지 여부를 나타냅니다. 경고, CRC가 없으면 1로 설정하고 CRC가 있으면 0으로 설정합니다.
            b2 |= (byte)0b00000001;
        }else {
            b2 |= (byte)0b00000000;
        }

        byte b3 = 0;
        b3 |= ((profile-1)<<6);
        b3 |= (sampling_frequency_index<<2);
        b3 |= (channel_configuration>>2);

        byte b4 = 0;
        b4 |= ((channel_configuration&3)<<6);
        b4 |= (packetLen>>11);

        byte b5 = (byte)((packetLen&0x7FF) >> 3);
        byte b6 = (byte)(((packetLen&7)<<5) + 0x1F);
        byte b7 = (byte)0xFC;

        packet[0] = b1;
        packet[1] = b2;
        packet[2] = b3;
        packet[3] = b4;
        packet[4] = b5;
        packet[5] = b6;
        packet[6] = b7;
    }
}
