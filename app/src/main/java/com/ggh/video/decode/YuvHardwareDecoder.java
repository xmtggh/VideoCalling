package com.ggh.video.decode;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.view.SurfaceHolder;

import com.ggh.video.Contants;
import com.ggh.video.base.DecodeManager;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author xmtggh
 * @time 2019/8/15
 * @email 626393661@qq.com
 **/
public class YuvHardwareDecoder extends DecodeManager {
    protected MediaCodec vDeCodec = null;
    protected MediaCodec.BufferInfo info = null;
    protected SurfaceHolder holder;

    public YuvHardwareDecoder() {
        super(Contants.WIDTH,Contants.HEIGHT,Contants.VBITRATE,Contants.FRAMERATE);
        initDecode();
    }

    @Override
    protected void initDecode() {

        try {
            vDeCodec = MediaCodec.createDecoderByType(Contants.VIDEO_FORMAT_H264);
        } catch (IOException e) {
            e.printStackTrace();
        }
        info = new MediaCodec.BufferInfo();
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(Contants.VIDEO_FORMAT_H264, vWidth, vHeight);
        vDeCodec.configure(mediaFormat, null, null, 0);
        vDeCodec.start();
       /* MediaFormat format = MediaFormat.createVideoFormat(Contants.VIDEO_FORMAT_H264, vWidth, vHeight);
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
            vDeCodec.configure(format, null, null, 0);
            // Start the codec
            vDeCodec.start();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    @Override
    protected void destory() {
        if (null != vDeCodec) {
            vDeCodec.stop();
            vDeCodec.release();
            vDeCodec = null;
        }
    }

    @Override
    public void onDecodeData(byte[] h264Data) {
        int inputBufferIndex = vDeCodec.dequeueInputBuffer(Contants.DEFAULT_TIMEOUT_US);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                inputBuffer = vDeCodec.getInputBuffer(inputBufferIndex);
            } else {
                inputBuffer = vDeCodec.getInputBuffers()[inputBufferIndex];
            }
            if (inputBuffer != null) {
                inputBuffer.clear();
                inputBuffer.put(h264Data, 0, h264Data.length);
                vDeCodec.queueInputBuffer(inputBufferIndex, 0, h264Data.length, 0, 0);
            }
        }
        int outputBufferIndex = vDeCodec.dequeueOutputBuffer(info, Contants.DEFAULT_TIMEOUT_US);
        ByteBuffer outputBuffer;
        while (outputBufferIndex > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                outputBuffer = vDeCodec.getOutputBuffer(outputBufferIndex);
            } else {
                outputBuffer = vDeCodec.getOutputBuffers()[outputBufferIndex];
            }
            if (outputBuffer != null) {
                outputBuffer.position(0);
                outputBuffer.limit(info.offset + info.size);
                byte[] yuvData = new byte[outputBuffer.remaining()];
                outputBuffer.get(yuvData);

                if (null!=mEncodeCallback) {
                    mEncodeCallback.onEncodeCallback(yuvData);
                }
                vDeCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBuffer.clear();
            }
            outputBufferIndex = vDeCodec.dequeueOutputBuffer(info, Contants.DEFAULT_TIMEOUT_US);
        }
    }
}
