package com.ggh.video.encode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import com.ggh.video.device.CameraConfig;

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

public class AndroidHradwareEncode {
    private MediaCodec codec = null;

    private int videoW;
    private int videoH;
    private int videoBitrate;
    private int videoFrameRate;

    private static final String TAG = "Encode";
    private static final String MIME = "Video/AVC";
    private IEncoderListener encoderListener;
    public AndroidHradwareEncode(int videoW, int videoH, int videoBitrate, int videoFrameRate, IEncoderListener encoderListener) {
        this.videoW = videoW;
        this.videoH = videoH;
        this.videoBitrate = videoBitrate;
        this.videoFrameRate = videoFrameRate;
        this.encoderListener = encoderListener;

        initMediaCodec();
    }
    private void initMediaCodec() {
        try {
            codec = MediaCodec.createEncoderByType(MIME);

            MediaFormat format = MediaFormat.createVideoFormat(MIME, videoW, videoH);
            format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, videoFrameRate);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, CameraConfig.IFRAME_INTERVAL);

            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            codec.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void encoderYUV420(byte[] input) {
        byte[] nv12 = new byte[input.length];
        NV21ToNV12(input, nv12, CameraConfig.WIDTH, CameraConfig.HEIGHT); //nv12转nv21

        try {
            int inputBufferIndex = codec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferIndex);
                inputBuffer.clear();
                inputBuffer.put(input);
                codec.queueInputBuffer(inputBufferIndex, 0, input.length, System.currentTimeMillis(), 0);
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
            while (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferIndex);
                byte[] outData = new byte[outputBuffer.remaining()];
                outputBuffer.get(outData, 0, outData.length);
                if (encoderListener != null) {
                    encoderListener.onH264(outData);
                }
                codec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void releaseMediaCodec() {
        if (codec != null) {
            codec.stop();
            codec.release();
            codec = null;
        }
    }

    public interface IEncoderListener {
        void onH264(byte[] data);
    }

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
