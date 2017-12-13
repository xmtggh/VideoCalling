package com.ggh.video.net;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by ZQZN on 2017/12/12.
 */

public class UDPProvider {
    private static UDPProvider provider;

    private DatagramSocket mSocket;

    public static UDPProvider getInstance() {
        if (provider == null) {
            provider = new UDPProvider();
        }
        return provider;
    }

    private UDPProvider() {
        initSocket();
    }


    /**
     * 初始化udp
     */
    private void initSocket() {
        try {
            mSocket = new DatagramSocket(NetConfig.REMOTEPORT, NetConfig.getIpAddress());
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public DatagramSocket getmSocket() {
        return mSocket;
    }

    public void destroy() {
        if (mSocket!=null){
            mSocket.disconnect();
            mSocket=null;
        }
    }
}
