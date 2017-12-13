package com.ggh.video.encode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.ggh.video.device.CameraConfig.HEIGHT;
import static com.ggh.video.device.CameraConfig.VCODEC;
import static com.ggh.video.device.CameraConfig.WIDTH;

/**
 * android 自带硬编码
 *
 * Created by ZQZN on 2017/9/14.
 */

public class AndroidHradwareEncode implements Encode {
    private long mPresentTimeUs;
    private MediaCodec vCodec = null;

    private MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();


    public AndroidHradwareEncode() {
        if (initVideoEncode()) {
            Log.d("AndroidHradwareEncode", "初始化编码器成功");
        } else {
            Log.d("AndroidHradwareEncode", "初始化编码器失败");
        }
    }

    public boolean initVideoEncode() {
        mPresentTimeUs = System.nanoTime() / 1000;
        try {
            vCodec = MediaCodec.createEncoderByType(VCODEC);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(VCODEC, WIDTH, HEIGHT);
        // 通过参数设置高中低码率
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, WIDTH * HEIGHT * 5);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        vCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        vCodec.start();
        return true;
    }


    public byte[] onEncodeVideoData(byte[] videoData) {
        long pts = System.nanoTime() / 1000 - mPresentTimeUs;
        byte[] yuv420sp = new byte[WIDTH * HEIGHT * 3 / 2];
//        NV21ToNV12(videoData, yuv420sp, CIF_WIDTH, CIF_HEIGHT);
//        NV21toI420SemiPlanar(videoData, yuv420sp, CIF_WIDTH, CIF_HEIGHT);
//        rotateAndToNV12(videoData, yuv420sp, CIF_WIDTH, CIF_HEIGHT);
//        swapYV12toI420(videoData, yuv420sp, CIF_WIDTH, CIF_HEIGHT);
        return onEncodeVideoFrame(videoData, pts);
    }

    private byte[] onEncodeVideoFrame(byte[] yuvData, long pts) {
        ByteBuffer[] inBuffers = vCodec.getInputBuffers();
        ByteBuffer[] outBuffers = vCodec.getOutputBuffers();


        int inBufferIndex = vCodec.dequeueInputBuffer(-1);
        if (inBufferIndex >= 0) {
            ByteBuffer bb = inBuffers[inBufferIndex];
            bb.clear();
            bb.put(yuvData, 0, yuvData.length);
            vCodec.queueInputBuffer(inBufferIndex, 0, yuvData.length, pts, 0);
        }

        for (; ; ) {
            int outBufferIndex = vCodec.dequeueOutputBuffer(videoBufferInfo, 0);
            if (outBufferIndex >= 0) {
                ByteBuffer bb = outBuffers[outBufferIndex];
                byte[] outputData = new byte[videoBufferInfo.size];
                bb.get(outputData);
                vCodec.releaseOutputBuffer(outBufferIndex, false);
                return outputData;
            } else {
                break;
            }
        }
        return new byte[0];
    }

    private void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }
    }

    @Override
    public byte[] encodeFrame(byte[] data) {
        return onEncodeVideoData(data);

    }


}
