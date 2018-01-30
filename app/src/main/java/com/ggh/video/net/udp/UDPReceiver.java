package com.ggh.video.net.udp;

import android.annotation.SuppressLint;
import android.util.Log;

import com.ggh.video.entity.Packet;
import com.ggh.video.net.Frame;
import com.ggh.video.net.LocalRtpSocketProvider;
import com.ggh.video.net.LocalUDPSocketProvider;
import com.ggh.video.net.Receiver;
import com.ggh.video.net.ReceiverCallback;
import com.ggh.video.net.rtp.RtspPacketReceiver;
import com.ggh.video.netty.EchoSeverHandler;
import com.ggh.video.rtp.RtpPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ZQZN on 2017/12/12.
 */

public class UDPReceiver extends Receiver {
    private boolean isReceiver = false;
    private DatagramPacket mPacket;
    private byte[] packetBuf = new byte[20000];
    private int packetSize = 20000;

    private Frame mFrame;

    private ReceiverCallback callback;

    public void setCallback(ReceiverCallback callback) {
        this.callback = callback;
    }

    private RtspPacketReceiver receiver;

    public UDPReceiver() {
        mPacket = new DatagramPacket(packetBuf, packetSize);
        mFrame = new Frame();
        receiver = new RtspPacketReceiver(640, 480);
//        initRxReceiver();


        new Thread(new Runnable() {
            @Override
            public void run() {
//                init();
                receiver();
            }
        }).start();
    }

    @SuppressLint("NewApi")
    public void receiver(){
        DatagramChannel datagramChannel = null;
        try {
            datagramChannel = DatagramChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            datagramChannel.socket().bind(new InetSocketAddress(1234));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteBuffer buffer = ByteBuffer.allocate(60000);
        byte b[];
        while(true) {
            buffer.clear();
            SocketAddress socketAddress = null;
            try {
                socketAddress = datagramChannel.receive(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (socketAddress != null) {
                int position = buffer.position();
                b = new byte[position];
                buffer.flip();
                for(int i=0; i<position; ++i) {
                    b[i] = buffer.get();
                }
                Log.d("ggh","接收到  "+b.length);
                if (callback!=null){
                    callback.callback(b);

                }
            }
        }
    }



    /**
     * netty接收
     */
    private void init() {
        Bootstrap b = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        b.group(group)
                .channel(NioDatagramChannel.class)
                .handler(new EchoSeverHandler());

        // 服务端监听端口
        try {
            b.bind(19998).sync().channel().closeFuture().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化接收器
     */
    private void initRxReceiver() {
        Log.d("ggh1", "初始化接收器");
        Observable.create(new ObservableOnSubscribe<Frame>() {
            @Override
            public void subscribe(ObservableEmitter<Frame> e) {
                while (isReceiver) {
                    try {
                        LocalUDPSocketProvider.getInstance().getLocalUDPSocket().receive(mPacket);
                        mFrame.setData(mPacket.getData());
                        mFrame.setSize(mPacket.getLength());
                        e.onNext(mFrame);
                        continue;
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        continue;
                    }

                }
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(Schedulers.newThread()).subscribe(new Observer<Frame>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Frame packet) {
                byte[] frame = receiver.rtp2h264(packet.getData(), packet.getSize());
                if (frame != null && frame.length > 0) {
                    try {
//                        Log.d("ggh", "接收到数据" + frame.length);
                        callback.callback(frame);

                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }


//                callback.callback(receiver.rtp2h264(packet.getData(), packet.getSize()));
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }


    @Override
    public void startRecivice() {
        isReceiver = true;
    }

    @Override
    public void stopRecivice() {
        isReceiver = false;
    }
}
