package com.ggh.video.encode;

import com.ggh.video.device.CameraConfig;

public class X264Encoder implements Encode{

	static {
		System.loadLibrary("encoder");
	}

	public X264Encoder() {
		initEncoder264(CameraConfig.WIDTH,CameraConfig.HEIGHT,CameraConfig.vbitrate,CameraConfig.framerate);
	}

	/******************** h264编码 **************************/
	public static native int initEncoder264(int w, int h, long bitrate, int fps);

	public static native int encoder264(byte[] yuv, byte[] h264);

	public static native int closeEncoder264();

	@Override
	public byte[] encodeFrame(byte[] data) {
		byte[] after = new byte[data.length];
		encoder264(data,after);
		return after;
	}
}
