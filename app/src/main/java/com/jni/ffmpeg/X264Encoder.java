package com.jni.ffmpeg;

import android.util.Log;

import com.ggh.video.device.CameraConfig;
import com.ggh.video.encode.Encode;

public class X264Encoder implements Encode {

	static {
		System.loadLibrary("encoder");
	}

	public X264Encoder() {
	}

	/******************** h264编码 **************************/
	public static native int initEncoder264(int w, int h, long bitrate, int fps);

	public static native int encoder264(byte[] yuv, byte[] h264);

	public static native int closeEncoder264();

	@Override
	public byte[] encodeFrame(byte[] data) {
		byte[] after = new byte[data.length];
		int h264Size = encoder264(data,after);
		Log.d("ggh","编码大小" + h264Size);
		byte[] restlt = new byte[h264Size];
		System.arraycopy(after,0,restlt,0,h264Size);
		return restlt;
	}
}
