package com.ggh.video.decode;

public class X264Decoder {
	
	static {
		System.loadLibrary("decoder");
	}
	
	/******************** h264解码 **************************/
	public static native int initDecoder264(int width, int height);
	
	public static native int decoder264(byte[] h264, int length);
	
	public static native int render(byte[] data);

	public static native int closeDecoder264();

}
