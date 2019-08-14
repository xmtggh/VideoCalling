package com.ggh.video.decode;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.SurfaceHolder;

import com.ggh.video.Contants;
import com.ggh.video.base.DecodeManager;
import com.ggh.video.device.CameraConfig;

import java.nio.ByteBuffer;

/**
 * android硬解码
 * @author xmtggh
 * @time 2019/8/14
 * @email 626393661@qq.com
 **/
public class HardwareDecoder extends DecodeManager {
    private MediaCodec vDeCodec = null;
    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
    private SurfaceHolder holder;

    public HardwareDecoder(SurfaceHolder holder) {
        super(Contants.WIDTH,Contants.HEIGHT,Contants.VBITRATE,Contants.FRAMERATE);
        this.holder = holder;
        initDecode();
    }

    @Override
    protected void initDecode() {
        MediaFormat format = MediaFormat.createVideoFormat(Contants.VIDEO_FORMAT_H264, vWidth, vHeight);
        format.setInteger(MediaFormat.KEY_ROTATION,90);
        format.setInteger(MediaFormat.KEY_BIT_RATE, vBitrate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, vFrameRate);
//        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
//                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, Contants.IFRAME_INTERVAL);
        try {
            // Get an instance of MediaCodec and give it its Mime type
            vDeCodec = MediaCodec.createDecoderByType(Contants.VIDEO_FORMAT_H264);
            // Configure the codec
            vDeCodec.configure(format, holder.getSurface(), null, 0);
            // Start the codec
            vDeCodec.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void destory() {

    }

    @Override
    public void onDecodeData(byte[] data) {
        Log.e("ggh1", "解码前");
        ByteBuffer[] inputBuffer = vDeCodec.getInputBuffers();
        int inputIndex = vDeCodec.dequeueInputBuffer(0);

        if (inputIndex >= 0) {
            ByteBuffer buffer = inputBuffer[inputIndex];

            try {
                buffer.put(data);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            vDeCodec.queueInputBuffer(inputIndex, 0, data.length, 0, 0);
        }

        int outputIndex = vDeCodec.dequeueOutputBuffer(info, 0);
        if (outputIndex >= 0) {
            vDeCodec.releaseOutputBuffer(outputIndex, true);
            Log.e("ggh1", "解码后");
        }
    }
}
