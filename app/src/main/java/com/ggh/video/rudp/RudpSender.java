package com.ggh.video.rudp;

import com.ggh.video.net.Frame;
import com.ggh.video.net.NetConfig;
import com.ggh.video.net.Send;
import com.network.mocket.MocketException;
import com.network.mocket.builder.client.Client;
import com.network.mocket.builder.client.ClientBuilder;

import java.io.IOException;

/**
 * Created by ZQZN on 2018/1/24.
 */

public class RudpSender extends Send {

    ClientBuilder<byte []> builder;
    Client<byte []> client;
    public RudpSender() {
        builder = new ClientBuilder<byte []>()
                .host("127.0.0.1", 1999);
        try {
            client = builder.build();
        } catch (MocketException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void addData(Frame frame) {
        try {
            client.write(frame.getData());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void startSender() {

    }

    @Override
    protected void stopSender() {

    }

    @Override
    protected void destroy() {

    }
}
