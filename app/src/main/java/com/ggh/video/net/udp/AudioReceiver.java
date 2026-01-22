package com.ggh.video.net.udp;

import android.util.Log;

import com.ggh.video.decode.AudioDecoder;
import com.ggh.video.net.NetConfig;
import com.ggh.video.net.ReceiverCallback;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by ZQZN on 2018/2/6.
 */

public class AudioReceiver {
    private volatile boolean isReceiver = true;


    public AudioReceiver() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                receiver();
            }
        }).start();
    }

    public void receiver() {
        DatagramChannel datagramChannel = null;
        try {
            datagramChannel = DatagramChannel.open();
            datagramChannel.socket().bind(new InetSocketAddress(NetConfig.REMOTE_AUDIO_PORT));
            
            ByteBuffer buffer = ByteBuffer.allocate(60000);
            byte b[];

            while (isReceiver) {
                buffer.clear();
                SocketAddress socketAddress = null;
                try {
                    socketAddress = datagramChannel.receive(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("ggh", "接收数据失败", e);
                    break;
                }
                if (socketAddress != null) {
                    int position = buffer.position();
                    b = new byte[position];
                    buffer.flip();
                    for (int i = 0; i < position; ++i) {
                        b[i] = buffer.get();
                    }
                    Log.d("ggh", "接收到  " + b.length);
                    AudioDecoder.getInstance().addData(b, b.length);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ggh", "音频接收器初始化失败", e);
        } finally {
            if (datagramChannel != null) {
                try {
                    datagramChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void startRecivice() {
        isReceiver = true;
        //开始解码
        AudioDecoder.getInstance().startDecoding();
    }

    public void stopRecivice() {
        isReceiver = false;
        AudioDecoder.getInstance().stopDecoding();
    }
}
