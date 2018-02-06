package com.ggh.video.net.udp;

import android.util.Log;

import com.ggh.video.net.NetConfig;
import com.ggh.video.net.Send;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by ZQZN on 2017/12/12.
 */

public class UDPSender extends Send {

    private boolean isStratSendData = true;

    private DatagramChannel channel;

    public UDPSender() {

        try {
            channel = DatagramChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void addData(final byte[] frame) {
        if (isStratSendData){
            send(frame);
        }
    }

    @Override
    public void startSender() {
        isStratSendData = true;

    }

    @Override
    public void stopSender() {
        isStratSendData = false;
    }

    @Override
    public void destroy() {

    }

    public void send(final byte[] data) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sendMessage(channel,data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

    }

    public static void sendMessage(DatagramChannel channel, byte[] mes) throws IOException {
        if (mes == null || mes.length < 0) {
            return;
        }
        ByteBuffer buffer = ByteBuffer.allocate(60000);
        buffer.clear();
        buffer.put(mes);
        buffer.flip();
        int send = channel.send(buffer, new InetSocketAddress(NetConfig.REMOTEIP, NetConfig.REMOTE_VIDEO_PORT));
        Log.d("ggh", "发送 " + send + " 字节");
    }
}
