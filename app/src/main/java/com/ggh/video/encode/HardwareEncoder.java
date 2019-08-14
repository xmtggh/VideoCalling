package com.ggh.video.encode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.ggh.video.Contants;
import com.ggh.video.base.EncodeManager;
import com.ggh.video.device.CameraConfig;

import java.nio.ByteBuffer;

/**
 * 视频硬编码
 *
 * @author xmtggh
 * @time 2019/8/14
 * @email 626393661@qq.com
 **/
public class HardwareEncoder extends EncodeManager {
    /**
     * 硬编码主要对象
     */
    private MediaCodec codec = null;

    /**
     * 默认创建
     */
    public HardwareEncoder() {
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
    public HardwareEncoder(int vWidth, int vHeight, int vBitrate, int vFrameRate) {
        super(vWidth, vHeight, vBitrate, vFrameRate);
        initEncode();

    }

    @Override
    protected void initEncode() {
        try {
            codec = MediaCodec.createEncoderByType(Contants.VIDEO_FORMAT_H264);

            MediaFormat format = MediaFormat.createVideoFormat(Contants.VIDEO_FORMAT_H264, vWidth, vHeight);
            format.setInteger(MediaFormat.KEY_BIT_RATE, vBitrate);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, vFrameRate);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, CameraConfig.IFRAME_INTERVAL);

            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            codec.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void destory() {
        if (codec != null) {
            codec.stop();
            codec.release();
            codec = null;
        }
    }

    @Override
    public void onEncodeData(byte[] data) {
        byte[] nv12 = new byte[data.length];
        //nv12转nv21
        NV21ToNV12(data, nv12, CameraConfig.WIDTH, CameraConfig.HEIGHT);

        try {
            int inputBufferIndex = codec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferIndex);
                inputBuffer.clear();
                inputBuffer.put(nv12);
                codec.queueInputBuffer(inputBufferIndex, 0, nv12.length, System.currentTimeMillis(), 0);
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
            while (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferIndex);
                byte[] outData = new byte[outputBuffer.remaining()];
                outputBuffer.get(outData, 0, outData.length);
                if (mEncodeCallback != null) {
                    mEncodeCallback.onEncodeCallback(outData);
                }
                codec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 旋转90°
     *
     * @param data
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    private byte[] rotateYUVDegree90(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    /**
     * 旋转270°
     *
     * @param data
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    private byte[] rotateYUVDegree270AndMirror(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate and mirror the Y luma
        int i = 0;
        int maxY = 0;
        for (int x = imageWidth - 1; x >= 0; x--) {
            maxY = imageWidth * (imageHeight - 1) + x * 2;
            for (int y = 0; y < imageHeight; y++) {
                yuv[i] = data[maxY - (y * imageWidth + x)];
                i++;
            }
        }
        // Rotate and mirror the U and V color components
        int uvSize = imageWidth * imageHeight;
        i = uvSize;
        int maxUV = 0;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            maxUV = imageWidth * (imageHeight / 2 - 1) + x * 2 + uvSize;
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[maxUV - 2 - (y * imageWidth + x - 1)];
                i++;
                yuv[i] = data[maxUV - (y * imageWidth + x)];
                i++;
            }
        }
        return yuv;
    }

    /**
     * nv21 转nv12
     *
     * @param nv21
     * @param nv12
     * @param width
     * @param height
     */
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


    public static byte[] rotateYUV420Degree90(byte[] input, int width, int height, int rotation) {
        int frameSize = width * height;
        int qFrameSize = frameSize / 4;
        byte[] output = new byte[frameSize + 2 * qFrameSize];


        boolean swap = (rotation == 90 || rotation == 270);
        boolean yflip = (rotation == 90 || rotation == 180);
        boolean xflip = (rotation == 270 || rotation == 180);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int xo = x, yo = y;
                int w = width, h = height;
                int xi = xo, yi = yo;
                if (swap) {
                    xi = w * yo / h;
                    yi = h * xo / w;
                }
                if (yflip) {
                    yi = h - yi - 1;
                }
                if (xflip) {
                    xi = w - xi - 1;
                }
                output[w * yo + xo] = input[w * yi + xi];
                int fs = w * h;
                int qs = (fs >> 2);
                xi = (xi >> 1);
                yi = (yi >> 1);
                xo = (xo >> 1);
                yo = (yo >> 1);
                w = (w >> 1);
                h = (h >> 1);
// adjust for interleave here
                int ui = fs + (w * yi + xi) * 2;
                int uo = fs + (w * yo + xo) * 2;
// and here
                int vi = ui + 1;
                int vo = uo + 1;
                output[uo] = input[ui];
                output[vo] = input[vi];
            }
        }
        return output;
    }

}
