package com.ggh.video.net;

import com.ggh.video.App;
import com.ggh.video.utils.NetUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by ZQZN on 2017/12/12.
 */

public class NetConfig {

//    public static final String REMOTEIP = "192.168.1.247";
    public static final String REMOTEIP = "192.168.1.247";
    public static final int REMOTEPORT = 19998;
    public static final int packSize = 1400;

    /**
     * 获取ip地址
     * @return
     * @throws UnknownHostException
     */
    public static InetAddress getIpAddress() throws UnknownHostException {
        return InetAddress.getByName(REMOTEIP);
    }
}
