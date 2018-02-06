package com.ggh.video.net.udp;

import android.util.Log;

import com.ggh.video.net.NetConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by ZQZN on 2018/2/6.
 */

public class AudioSender {
    private DatagramChannel audioChannel;
    public AudioSender() {
        try {
            audioChannel = DatagramChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startSending(){

    }


    public void addData(byte[] data,int size){
        byte[] tempdata = new byte[size];
        System.arraycopy(data,0,tempdata,0,size);
        send(tempdata);
    }

    public void stopSending(){

    }

    public void send(final byte[] data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendMessage(audioChannel,data);
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
        int send = channel.send(buffer, new InetSocketAddress(NetConfig.REMOTEIP, NetConfig.REMOTE_AUDIO_PORT));
        Log.d("ggh", "音频发送 " + send + " 字节");
    }
}
