package com.ggh.video.provider;

import com.ggh.video.device.CameraConfig;
import com.ggh.video.encode.AndroidHradwareEncode;
import com.ggh.video.net.udp.Message;
import com.ggh.video.net.udp.NettyClient;
import com.ggh.video.net.udp.NettyReceiverHandler;

import example.sszpf.x264.x264sdk;

/**
 * Created by ZQZN on 2018/2/1.
 */

public class EncodeProvider {
    public static final String ENCEDE_TYPE_ANDROIDHARDWARE = "ENCEDE_TYPE_ANDROIDHARDWARE";
    public static final String ENCEDE_TYPE_FFMEPG = "ENCEDE_TYPE_FFMEPG";
    public static final String ENCEDE_TYPE_X264 = "ENCEDE_TYPE_X264";

    private AndroidHradwareEncode mEncode;
    private x264sdk x264Sdk;
    private boolean isfinish = false;
    private NettyClient nettyClient;
    private static EncodeProvider provider;

    public String currenType;
    private OnEncodeFrameCallback encodeFrameCallback;

    public void setEncodeFrameCallback(OnEncodeFrameCallback encodeFrameCallback) {
        this.encodeFrameCallback = encodeFrameCallback;
    }

    public static EncodeProvider getProvider() {
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
    public EncodeProvider(String targetIp, int targetPort, int bindPort, final NettyReceiverHandler.FrameResultedCallback frameResultedCallback, String type) {
        nettyClient = new NettyClient.
                Builder().targetIp(targetIp)
                .targetport(targetPort)
                .localPort(bindPort)
                .frameResultedCallback(frameResultedCallback)
                .build();
        if (type.equals(ENCEDE_TYPE_ANDROIDHARDWARE)) {//Android本身的硬编码
            currenType = ENCEDE_TYPE_ANDROIDHARDWARE;
            mEncode = new AndroidHradwareEncode(CameraConfig.WIDTH, CameraConfig.HEIGHT, CameraConfig.vbitrate, CameraConfig.framerate, new AndroidHradwareEncode.IEncoderListener() {
                @Override
                public void onH264(byte[] data) {

                    //发送编码后的数据
                    nettyClient.sendData(data, Message.MES_TYPE_VIDEO);
                    if (encodeFrameCallback != null) {
                        encodeFrameCallback.onEncodeData(data);
                    }
                }
            });
            isfinish = true;
        } else if (type.equals(ENCEDE_TYPE_X264)) {//x264编码
            currenType = ENCEDE_TYPE_X264;
            x264Sdk = new x264sdk(CameraConfig.WIDTH, CameraConfig.HEIGHT, CameraConfig.framerate, CameraConfig.vbitrate, new x264sdk.listener() {
                @Override
                public void h264data(byte[] buffer, int length) {
                    //发送编码后的数据
                    nettyClient.sendData(buffer, Message.MES_TYPE_VIDEO);
                    if (encodeFrameCallback != null) {
                        encodeFrameCallback.onEncodeData(buffer);
                    }
                }
            });
            isfinish = true;
        }
        provider = this;

    }

    /**
     * 发送测试数据
     * A
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
            if (currenType.equals(ENCEDE_TYPE_ANDROIDHARDWARE)) {
                mEncode.encoderYUV420(data);
            } else if (currenType.equals(ENCEDE_TYPE_X264)) {
                x264Sdk.PushOriStream(data, data.length);
            }

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

    public interface OnEncodeFrameCallback {
        void onEncodeData(byte[] data);
    }


}
