/*
 * Copyright (C) 2017  即时通讯网(52im.net) & Jack Jiang.
 * The MobileIMSDK_X (MobileIMSDK v3.x) Project. 
 * All rights reserved.
 * 
 * > Github地址: https://github.com/JackJiang2011/MobileIMSDK
 * > 文档地址: http://www.52im.net/forum-89-1.html
 * > 即时通讯技术社区：http://www.52im.net/
 * > 即时通讯技术交流群：320837163 (http://www.52im.net/topic-qqgroup.html)
 *  
 * "即时通讯网(52im.net) - 即时通讯开发者社区!" 推荐开源工程。
 * 
 * LocalUDPSocketProvider.java at 2017-5-1 21:06:42, code by Jack Jiang.
 * You can contact author with jack.jiang@52im.net or jb2011@163.com.
 */
package com.ggh.video.net;

import android.util.Log;

import com.ggh.video.rtp.RtpSocket;
import com.ggh.video.rtp.SipdroidSocket;

import java.net.InetAddress;


public class LocalRtpSocketProvider {
    private final static String TAG = LocalRtpSocketProvider.class.getSimpleName();

    private static LocalRtpSocketProvider instance = null;

    private RtpSocket localRTPSocket = null;

    public static LocalRtpSocketProvider getInstance() {
        if (instance == null) {
            instance = new LocalRtpSocketProvider();
        }
        return instance;
    }

    private LocalRtpSocketProvider() {
        //
    }

    //重置本地udp socket
    public RtpSocket resetLocalRTPSocket() {
        try {
            //先关闭本地udp
            closeLocalRTPSocket();
            localRTPSocket = new RtpSocket(new SipdroidSocket(NetConfig.REMOTEPORT), InetAddress.getByName(NetConfig.REMOTEIP), NetConfig.REMOTEPORT);
            return localRTPSocket;
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            closeLocalRTPSocket();
            return null;
        }
    }

    private boolean isLocalUDPSocketReady() {
        return localRTPSocket != null && !localRTPSocket.getDatagramSocket().isClosed();
    }

    public RtpSocket getLocalRTPSocket() {
        if (isLocalUDPSocketReady()) {
            return localRTPSocket;
        } else {
            return resetLocalRTPSocket();
        }
    }

    public void closeLocalRTPSocket() {
        try {
            //关闭socket
            if (localRTPSocket != null) {
                localRTPSocket.close();
                localRTPSocket = null;
            } else {
                Log.d("UDPProvider", "Socket处于未初化状态（可能是您还未登陆），无需关闭。");
            }
        } catch (Exception e) {
            Log.w(TAG, "lcloseLocalUDPSocket时出错，原因是：" + e.getMessage(), e);
        }
    }
}
