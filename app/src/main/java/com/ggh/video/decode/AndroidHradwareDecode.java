package com.ggh.video.decode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;

import com.ggh.video.device.CameraConfig;
import com.ggh.video.encode.Frame;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by ZQZN on 2017/9/14.
 */

public class AndroidHradwareDecode {
    private long mPresentTimeUs;
    private MediaCodec vDeCodec = null;
    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

    private boolean isStartDecode = false;

    public AndroidHradwareDecode(SurfaceHolder holder) {
        initVideoEncode(holder);
    }

    public boolean initVideoEncode(SurfaceHolder holder) {
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, CameraConfig.WIDTH, CameraConfig.HEIGHT);
        format.setInteger(MediaFormat.KEY_ROTATION,90);
        format.setInteger(MediaFormat.KEY_BIT_RATE, CameraConfig.vbitrate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, CameraConfig.framerate);
//        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
//                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        try {
            // Get an instance of MediaCodec and give it its Mime type
            vDeCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            // Configure the codec
            vDeCodec.configure(format, holder.getSurface(), null, 0);
            // Start the codec
            vDeCodec.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    public void onDecodeData(byte[] codeData) {
        Log.e("ggh1", "解码前");
        ByteBuffer[] inputBuffer = vDeCodec.getInputBuffers();
        int inputIndex = vDeCodec.dequeueInputBuffer(0);

        if (inputIndex >= 0) {
            ByteBuffer buffer = inputBuffer[inputIndex];

            try {
                buffer.put(codeData);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            vDeCodec.queueInputBuffer(inputIndex, 0, codeData.length, 0, 0);
        }

        int outputIndex = vDeCodec.dequeueOutputBuffer(info, 0);
        if (outputIndex >= 0) {
            vDeCodec.releaseOutputBuffer(outputIndex, true);
            Log.e("ggh1", "解码后");
        }

    }

}
