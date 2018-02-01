package com.ggh.video.binder;

import com.ggh.video.encode.AndroidHradwareEncode;
import com.ggh.video.encode.Encode;
import com.ggh.video.encode.X264Encoder;
import com.ggh.video.net.Send;
import com.ggh.video.net.udp.UDPSender;

/**
 * Created by ZQZN on 2018/2/1.
 */

public class EncodeBinder {
    public static final String ENCEDE_TYPE_ANDROIDHARDWARE = "ENCEDE_TYPE_ANDROIDHARDWARE";
    public static final String ENCEDE_TYPE_FFMEPG = "ENCEDE_TYPE_FFMEPG";
    public static final String ENCEDE_TYPE_X264 = "ENCEDE_TYPE_X264";

    private UDPSender sender;
    private Encode mEncode;

    public EncodeBinder(String type) {
        sender = new UDPSender();
        if (type.equals(ENCEDE_TYPE_ANDROIDHARDWARE)) {
            mEncode = new AndroidHradwareEncode();
        }else if (type.equals(ENCEDE_TYPE_X264)){
            mEncode = new X264Encoder();
        }
    }

    public void receiver(byte[] data) {
        byte[] encode = mEncode.encodeFrame(data);
        sender.addData(encode);
    }
}
