package com.ggh.video.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by ZQZN on 2017/12/12.
 */

public class NetConfig {

    public static final String REMOTEIP = "192.168.1.207";
    public static final int REMOTEPORT = 19999;

    /**
     * 获取ip地址
     * @return
     * @throws UnknownHostException
     */
    public static InetAddress getIpAddress() throws UnknownHostException {
        return InetAddress.getByName(REMOTEIP);
    }
}
