package example.sszpf.x264;

import java.nio.ByteBuffer;

public class x264sdk {
    private int fps = 30;
    private int timespan = 90000 / fps;

    private long time;
    private int width;
    private int height;
    private int ifps;
    private int bite;

    public interface listener {
        void h264data(byte[] buffer, int length);
    }


    private listener _listener;

    public x264sdk(int width, int height, int fps, int bite, listener l) {
        _listener = l;
        this.width = width;
        this.height = height;
        this.ifps = fps;
        this.bite = bite;
        initX264Encode(width, height, fps, bite);
    }

    static {
        System.loadLibrary("x264encoder");
    }

    private ByteBuffer mVideobuffer;


    public void PushOriStream(byte[] buffer, int length) {
        time += timespan;
        byte[] yuv420 = new byte[width*height*3/2];
        YUV420SP2YUV420(buffer,yuv420,width,height);
        if (mVideobuffer == null || mVideobuffer.capacity() < length) {
            mVideobuffer = ByteBuffer.allocateDirect(((length / 1024) + 1) * 1024);
        }
        mVideobuffer.rewind();
        mVideobuffer.put(yuv420, 0, length);
        encoderH264(length, time);
    }

    public native void initX264Encode(int width, int height, int fps, int bite);

    public native int encoderH264(int length, long time);

    public native void CloseX264Encode();

    private void H264DataCallBackFunc(byte[] buffer, int length) {
        _listener.h264data(buffer, length);
    }

    private void YUV420SP2YUV420(byte[] yuv420sp, byte[] yuv420, int width, int height) {
        if (yuv420sp == null || yuv420 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        //copy y
        for (i = 0; i < framesize; i++) {
            yuv420[i] = yuv420sp[i];
        }
        i = 0;
        for (j = 0; j < framesize / 2; j += 2) {
            yuv420[i + framesize * 5 / 4] = yuv420sp[j + framesize];
            i++;
        }
        i = 0;
        for (j = 1; j < framesize / 2; j += 2) {
            yuv420[i + framesize] = yuv420sp[j + framesize];
            i++;
        }
    }



}
