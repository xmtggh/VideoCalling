package com.ggh.video.encode;

import com.ggh.video.Contants;
import com.ggh.video.base.EncodeManager;

import example.sszpf.x264.x264sdk;

/**
 * X264 编码
 * @author xmtggh
 * @time 2019/8/14
 * @email 626393661@qq.com
 **/
public class X264Encoder extends EncodeManager {
    private x264sdk x264sdkEncode;

    /**
     * 默认创建
     */
    public X264Encoder() {
        super(Contants.WIDTH,Contants.HEIGHT,Contants.VBITRATE,Contants.FRAMERATE);
        initEncode();
    }

    /**
     * 自定义创建
     * @param vWidth
     * @param vHeight
     * @param vBitrate
     * @param vFrameRate
     */
    public X264Encoder(int vWidth, int vHeight, int vBitrate, int vFrameRate) {
        super(vWidth, vHeight, vBitrate, vFrameRate);
        initEncode();
    }

    @Override
    protected void initEncode() {
        x264sdkEncode = new x264sdk(vWidth, vHeight, vFrameRate, vBitrate, new x264sdk.listener() {
            @Override
            public void h264data(byte[] buffer, int length) {
                if (mEncodeCallback!=null){
                    mEncodeCallback.onEncodeCallback(buffer);
                }
            }
        });
    }

    @Override
    protected void destory() {
        if (x264sdkEncode!=null){
            x264sdkEncode.CloseX264Encode();
            x264sdkEncode = null;
        }

    }


    @Override
    public void onEncodeData(byte[] data) {
        if (x264sdkEncode!=null){
            x264sdkEncode.PushOriStream(data,data.length);
        }
    }
}
