package com.ggh.video.base;

import com.ggh.video.encode.IEncoderCallback;

/**
 * @author xmtggh
 * @time 2019/8/14
 * @email 626393661@qq.com
 **/
public abstract class EncodeManager {
    /**
     * 视频分辨率宽
     */
    protected int vWidth;
    /**
     * 视频分辨率高
     */
    protected int vHeight;
    /**
     * 视频比特率
     */
    protected int vBitrate;
    /**
     * 视频帧率
     */
    protected int vFrameRate;

    protected IEncoderCallback mEncodeCallback;

    public EncodeManager() {
    }

    public EncodeManager(int vWidth, int vHeight, int vBitrate, int vFrameRate) {
        this.vWidth = vWidth;
        this.vHeight = vHeight;
        this.vBitrate = vBitrate;
        this.vFrameRate = vFrameRate;
    }

    /**
     * 设置编码结果回调
     * @param mEncodeCallback
     */
    public void setEncodeCallback(IEncoderCallback mEncodeCallback) {
        this.mEncodeCallback = mEncodeCallback;
    }

    protected abstract void initEncode();
    protected abstract void destory();
    /**
     * 传入编码数据
     * @param data
     */
    public abstract void onEncodeData(byte[] data);
}
