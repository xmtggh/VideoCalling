package com.ggh.video.decode;

import android.view.Surface;
import android.view.SurfaceHolder;

import com.ggh.video.base.DecodeManager;
import com.vonchenchen.android_video_demos.codec.CodecWrapper;

/**
 * ffmpeg 解码
 * @author xmtggh
 * @time 2019/8/14
 * @email 626393661@qq.com
 **/
public class FFmpegDecoder extends DecodeManager {
    private FFmpegHolder holder;

    private SurfaceHolder mSurfaceHolder;

    public FFmpegDecoder(SurfaceHolder mSurfaceHolder) {
        this.mSurfaceHolder = mSurfaceHolder;
        initDecode();
    }

    @Override
    protected void initDecode() {
        holder = new FFmpegHolder(mSurfaceHolder.getSurface());

    }

    @Override
    protected void destory() {
        if (holder!=null){
            holder.release();
            holder = null;
        }
    }

    @Override
    public void onDecodeData(byte[] data) {
        holder.decodeStream(data, data.length);
    }

    private class FFmpegHolder extends CodecWrapper {
        public FFmpegHolder(Surface surface) {
            super(surface);
        }

        @Override
        protected void getOneFrame(int[] data, int width, int height) {

        }
    }
}
