package com.ggh.video.decode;

import android.media.MediaCodec;
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

public class VideoDecodeManager {
    private long mPresentTimeUs;
    private MediaCodec vDeCodec = null;
    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

    private boolean isStartDecode = false;

    public VideoDecodeManager(SurfaceHolder holder) {
        initVideoEncode(holder);
    }

    public boolean initVideoEncode(SurfaceHolder holder) {
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, CameraConfig.WIDTH, CameraConfig.HEIGHT);
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

    public void decode(byte[] buf) {
        ByteBuffer[] inputBuffers = vDeCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = vDeCodec.getOutputBuffers();
        int inputBufferIndex = vDeCodec.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buf, 0, buf.length);
            vDeCodec.queueInputBuffer(inputBufferIndex, 0, buf.length, 0, 0);
        }

        // TO DO
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

    List<Frame> datalist = new ArrayList<>();


    public byte[] connectFrame(byte[] buffer, int packSize, boolean islaskpack, int sequence, long time) {
        int frameSize = 0;
        byte[] currentpack = new byte[packSize];

        System.arraycopy(buffer, 0, currentpack, 0, packSize);
        Log.w("ggh", "收到解析包,长度" + currentpack.length + "序列号    " + sequence);
        Frame current = new Frame(currentpack, packSize, islaskpack, sequence, time);
        datalist.add(current);

        //判断收包是否完成
        if (islaskpack) {
            try {
                Collections.sort(datalist, new Comparator<Frame>() {
                    @Override
                    public int compare(Frame lhs, Frame rhs) {
                        return (int) (lhs.getTime() - rhs.getTime());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (int i = 0; i < datalist.size(); i++) {
                frameSize = frameSize + datalist.get(i).getBuffer().length;

            }

            int lenghtafter = 0;
            byte[] frame = new byte[frameSize];
            for (int i = 0; i < datalist.size(); i++) {
                System.arraycopy(datalist.get(i).getBuffer(), 0, frame, lenghtafter, datalist.get(i).getBuffer().length);
                lenghtafter = lenghtafter + datalist.get(i).getBuffer().length;
            }
            clearBuffer();
            return frame;

        }

        return new byte[0];
    }


    public Long getKey(Map<Long, byte[]> data) {
        Set<Long> setKey = data.keySet();
        Iterator<Long> iterator = setKey.iterator();
        // 从while循环中读取key
        while (iterator.hasNext()) {
            long key = iterator.next();
            // 此时的String类型的key就是我们需要的获取的值
            return key;
        }
        return 0L;

    }

    public void clearBuffer() {
        if (datalist != null) {
            datalist.clear();
        }
    }
}
