package com.ggh.video.binder;

import android.util.Log;

import com.ggh.video.device.CameraConfig;
import com.ggh.video.encode.AndroidHradwareEncode;
import com.ggh.video.encode.Encode;
import com.ggh.video.net.udp.Message;
import com.ggh.video.net.udp.NettyClient;
import com.ggh.video.net.udp.NettyReceiverHandler;
import com.jni.ffmpeg.X264Encoder;
import com.ggh.video.net.udp.UDPSender;

/**
 * Created by ZQZN on 2018/2/1.
 */

public class FrameProvider {
    public static final String ENCEDE_TYPE_ANDROIDHARDWARE = "ENCEDE_TYPE_ANDROIDHARDWARE";
    public static final String ENCEDE_TYPE_FFMEPG = "ENCEDE_TYPE_FFMEPG";
    public static final String ENCEDE_TYPE_X264 = "ENCEDE_TYPE_X264";

    private AndroidHradwareEncode mEncode;
    private boolean isfinish = false;
    private NettyClient nettyClient;
    private static FrameProvider provider;

    private OnEncodeFrameCallback encodeFrameCallback;

    public void setEncodeFrameCallback(OnEncodeFrameCallback encodeFrameCallback) {
        this.encodeFrameCallback = encodeFrameCallback;
    }

    public static FrameProvider getProvider() {
        if (provider != null) {
            return provider;
        }
        return null;
    }

    /**
     * 外界初始化
     *
     * @param targetIp
     * @param targetPort
     * @param bindPort
     * @param frameResultedCallback
     * @param type
     */
    public FrameProvider(String targetIp, int targetPort, int bindPort, final NettyReceiverHandler.FrameResultedCallback frameResultedCallback, String type) {
        nettyClient = new NettyClient.
                Builder().targetIp(targetIp)
                .targetport(targetPort)
                .localPort(bindPort)
                .frameResultedCallback(frameResultedCallback)
                .build();
        if (type.equals(ENCEDE_TYPE_ANDROIDHARDWARE)) {//Android本身的硬编码
            mEncode = new AndroidHradwareEncode(CameraConfig.WIDTH, CameraConfig.HEIGHT, CameraConfig.vbitrate, CameraConfig.framerate, new AndroidHradwareEncode.IEncoderListener() {
                @Override
                public void onH264(byte[] data) {
                    //发送编码后的数据
                    nettyClient.sendData(data, Message.MES_TYPE_VIDEO);
                    if (encodeFrameCallback!=null){
                        encodeFrameCallback.onEncodeData(data);
                    }
                }
            });
            isfinish = true;
        } else if (type.equals(ENCEDE_TYPE_X264)) {//x264编码
            if (X264Encoder.initEncoder264(CameraConfig.WIDTH, CameraConfig.HEIGHT, CameraConfig.vbitrate, CameraConfig.framerate) < 0) {
                Log.d("ggh", "初始化失败");
            } else {
                isfinish = true;
//                mEncode = new X264Encoder();

            }
        }
        provider = this;

    }

    /**
     * 发送测试数据
     *
     * @param msg
     */
    public void setTestData(String msg) {
        nettyClient.sendData(msg, Message.MES_TYPE_NOMAL);

    }

    /**
     * 接收编码的数据
     *
     * @param data
     */
    public void sendVideoFrame(byte[] data) {
        if (isfinish) {
            //发送数据
            mEncode.encoderYUV420(data);

        }
    }

    /**
     * 接收编码的数据
     *
     * @param data
     */
    public void sendAudioFrame(byte[] data) {
        if (isfinish) {
            //发送编码后的数据
            nettyClient.sendData(data, Message.MES_TYPE_AUDIO);
        }
    }

    public interface OnEncodeFrameCallback{
        void onEncodeData(byte[] data);
    }
}
