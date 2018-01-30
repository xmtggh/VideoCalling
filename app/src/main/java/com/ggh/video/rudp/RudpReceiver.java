package com.ggh.video.rudp;

import android.util.Log;

import com.ggh.video.net.NetConfig;
import com.ggh.video.net.Receiver;
import com.network.mocket.MocketException;
import com.network.mocket.builder.server.Server;
import com.network.mocket.builder.server.ServerBuilder;
import com.network.mocket.helper.Pair;

import java.net.SocketAddress;

/**
 * Created by ZQZN on 2018/1/24.
 */

public class RudpReceiver extends Receiver {
    private boolean isreceiver = true;
    private Server<byte[]> server;
    public RudpReceiver() {
        ServerBuilder<byte []> serverBuilder = new ServerBuilder<byte []>()
                .port(NetConfig.REMOTEPORT);
        try {
            Server<byte[]> server = serverBuilder.build();
        } catch (MocketException e) {
            e.printStackTrace();
        }

        while (isreceiver) {
            // blocking read
            try {
                Pair<SocketAddress, byte[]> readBytes = server.read();
                Log.d("ggh","接收数据"+readBytes.getSecond().length);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void startRecivice() {

    }

    @Override
    protected void stopRecivice() {

    }
}
