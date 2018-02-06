package com.ggh.video.binder;

import android.util.Log;

import com.ggh.video.device.CameraConfig;
import com.ggh.video.encode.AndroidHradwareEncode;
import com.ggh.video.encode.Encode;
import com.jni.ffmpeg.X264Encoder;
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
    private boolean isfinish = false;

    public EncodeBinder(String type) {
        sender = new UDPSender();
        if (type.equals(ENCEDE_TYPE_ANDROIDHARDWARE)) {
            mEncode = new AndroidHradwareEncode();
            isfinish = true;
        }else if (type.equals(ENCEDE_TYPE_X264)){
            if (X264Encoder.initEncoder264(CameraConfig.WIDTH,CameraConfig.HEIGHT,CameraConfig.vbitrate,CameraConfig.framerate) < 0) {
                Log.d("ggh", "初始化失败");
            }else {
                isfinish = true;
                mEncode = new X264Encoder();

            }
        }


    }

    public void receiver(byte[] data) {
        if (isfinish){
            byte[] encode = mEncode.encodeFrame(data);
            sender.addData(encode);
        }

    }
}
