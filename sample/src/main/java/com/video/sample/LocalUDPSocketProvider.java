package com.video.sample;

import android.util.Log;

import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Administrator on 2018/1/8 0008.
 */

public class LocalUDPSocketProvider {
    private static final String TAG = LocalUDPSocketProvider.class.getSimpleName();
    private static LocalUDPSocketProvider instance = null;
    private DatagramSocket localUDPSocket = null;

    public static LocalUDPSocketProvider getInstance()
    {
        if (instance == null)
            instance = new LocalUDPSocketProvider();
        return instance;
    }

    public void initSocket()
    {
        try
        {
            // UDP本地监听端口（如果为0将表示由系统分配，否则使用指定端口）
            this.localUDPSocket = new DatagramSocket(ConfigEntity.localUDPPort);
            // 调用connect之后，每次send时DatagramPacket就不需要设计目标主机的ip和port了
            // * 注意：connect方法一定要在DatagramSocket.receive()方法之前调用，
            // * 不然整send数据将会被错误地阻塞。这或许是官方API的bug，也或许是调
            // * 用规范就应该这样，但没有找到官方明确的说明
            this.localUDPSocket.connect(
                    InetAddress.getByName(ConfigEntity.serverIP), ConfigEntity.serverUDPPort);
            this.localUDPSocket.setReuseAddress(true);
            Log.d(TAG, "new DatagramSocket()已成功完成.");
        }
        catch (Exception e)
        {
            Log.w(TAG, "localUDPSocket创建时出错，原因是：" + e.getMessage(), e);
        }
    }

    public DatagramSocket getLocalUDPSocket()
    {
        return this.localUDPSocket;
    }
}
