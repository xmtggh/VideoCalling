package com.ggh.video;

/**
 * 存放一些全局数据或者配置
 * @author xmtggh
 * @time 2019/8/14
 * @email 626393661@qq.com
 **/
public class Contants {
    public static final String VIDEO_FORMAT_H264 = "Video/AVC";
    //摄像头采集分辨率高度
    public static int WIDTH = 640;
    //摄像头采集分辨率宽度
    public static int HEIGHT = 480;
    //摄像头采集码流
    public static final int VBITRATE = 1000000;
    //摄像头采集帧率
    public static final int FRAMERATE = 30;
    //关键镇间隔
    public static final int IFRAME_INTERVAL = 1;

    public static final long DEFAULT_TIMEOUT_US = 0;//1000 * 10;
}
