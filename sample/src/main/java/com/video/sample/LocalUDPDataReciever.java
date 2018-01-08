package com.video.sample;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;


/**
 * Created by Administrator on 2018/1/8 0008.
 */

public class LocalUDPDataReciever {
    private static final String TAG = LocalUDPDataReciever.class.getSimpleName();
    private static LocalUDPDataReciever instance = null;
    private Thread thread = null;

    public static LocalUDPDataReciever getInstance()
    {
        if (instance == null)
            instance = new LocalUDPDataReciever();
        return instance;
    }

    public void startup()
    {
        this.thread = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    Log.d(LocalUDPDataReciever.TAG, "本地UDP端口侦听中，端口=" + ConfigEntity.localUDPPort + "...");

                    //开始侦听
                    LocalUDPDataReciever.this.udpListeningImpl();
                }
                catch (Exception eee)
                {
                    Log.w(LocalUDPDataReciever.TAG, "本地UDP监听停止了(socket被关闭了?)," + eee.getMessage(), eee);
                }
            }
        });
        this.thread.start();
    }

    private void udpListeningImpl() throws Exception
    {
        while (true)
        {
            byte[] data = new byte[1024];
            // 接收数据报的包
            DatagramPacket packet = new DatagramPacket(data, data.length);

            DatagramSocket localUDPSocket = LocalUDPSocketProvider.getInstance().getLocalUDPSocket();
            if ((localUDPSocket == null) || (localUDPSocket.isClosed()))
                continue;

            // 阻塞直到收到数据
            localUDPSocket.receive(packet);

            // 解析服务端发过来的数据
            String pFromServer = new String(packet.getData(), 0 , packet.getLength(), "UTF-8");
            Log.w(LocalUDPDataReciever.TAG, "【NOTE】>>>>>> 收到服务端的消息："+pFromServer);
        }
    }
}
