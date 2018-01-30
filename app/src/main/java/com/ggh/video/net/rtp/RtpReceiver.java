package com.ggh.video.net.rtp;

import android.util.Log;

import com.ggh.video.entity.Packet;
import com.ggh.video.net.LocalRtpSocketProvider;
import com.ggh.video.net.NetConfig;
import com.ggh.video.net.Receiver;
import com.ggh.video.net.ReceiverCallback;
import com.ggh.video.netty.EchoSeverHandler;
import com.ggh.video.rtp.RtpPacket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
 * Created by ZQZN on 2018/1/8.
 */

public class RtpReceiver extends Receiver {
    private String tag = "RtpReceiver";
    /**
     * 是否接收的标志
     */
    private boolean isReceiver = true;
    /**
     * 包缓存
     */
    private byte[] socketReceiveBuffer = new byte[20000];
    /**
     * 创建RTP接受包
     */
    private RtpPacket rtpReceivePacket = null;
    /**
     * 缓冲区
     */
    private byte[] buffer = new byte[20000];


    private ReceiverCallback callback;
    private LinkedList<RtpPacket> bufferFrameList;
    private RtspPacketReceiver receiver;

    public void setCallback(ReceiverCallback callback) {
        this.callback = callback;
    }

    public RtpReceiver() {
        params = new HashMap<>();
        //初始化socketBuffer改变时rtp_Packet也跟着改变
        rtpReceivePacket = new RtpPacket(socketReceiveBuffer, 0);
        receiver = new RtspPacketReceiver(640, 480);
        bufferFrameList = new LinkedList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                init();

            }
        }).start();
//        initRxReceiver();
//        initThreadReceiver();
//        DecoderThread decoder = new DecoderThread();
//        decoder.start(); //启动一个线程
//        ConnectPacket connectPacket = new ConnectPacket();
//        connectPacket.start();
    }

    /**
     * 接收rtp数据并解码 线程
     */
    class DecoderThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    LocalRtpSocketProvider.getInstance().getLocalRTPSocket().receive(rtpReceivePacket); //接收一个包
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i("ggh", "序列号   " + rtpReceivePacket.getSequenceNumber() + "时间戳   " + rtpReceivePacket.getTimestamp());

//                bufferFrameList.add(rtpReceivePacket);
                if (LocalRtpSocketProvider.getInstance().getLocalRTPSocket().getDatagramSocket() != null && !LocalRtpSocketProvider.getInstance().getLocalRTPSocket().getDatagramSocket().isClosed()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            connectPack(rtpReceivePacket);
                        }
                    }).start();
                }

            }


        }
    }

    class ConnectPacket extends Thread {
        @Override
        public void run() {
            while (true) {
                if (bufferFrameList.size() > 0) {
                    RtpPacket packet = bufferFrameList.remove(0);
                    if (packet != null)
                        connectPack(packet);
                }
                continue;
            }
        }
    }

    private void initThreadReceiver() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isReceiver) {
                    try {
                        Log.d("ggh1", "接收数据");
                        LocalRtpSocketProvider.getInstance().getLocalRTPSocket().receive(rtpReceivePacket);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    Log.d("ggh1", "收到数据");
                    connectPack(rtpReceivePacket);
                }
            }
        }).start();
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
            b.bind(19999).sync().channel().closeFuture().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化接收器
     */
    private void initRxReceiver() {
        Observable.create(new ObservableOnSubscribe<RtpPacket>() {
            @Override
            public void subscribe(ObservableEmitter<RtpPacket> e) {
                while (isReceiver) {
                    try {
                        LocalRtpSocketProvider.getInstance().getLocalRTPSocket().receive(rtpReceivePacket);
                        rtpReceivePacket.getSequenceNumber();
                        e.onNext(rtpReceivePacket);
                        continue;
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        continue;
                    }

                }
            }
        }).observeOn(Schedulers.newThread()).subscribeOn(Schedulers.io()).subscribe(new Observer<RtpPacket>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(RtpPacket packet) {
//                receiver.rtp2h264(packet.getPacket(), packet.getLength());
                connectPack(packet);
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

    public void connectPack(RtpPacket rtp_receive_packet) {
        //获取包的大小
        int packetSize = rtp_receive_packet.getPayloadLength();

        if (packetSize <= 0) {
            return;
        }
        //确认负载类型为2
        if (rtp_receive_packet.getPayloadType() != 2) {
            return;
        }
        /**
         * 第一个参数:源数组
         * 第二个参数:源数组要复制的起始位置
         * 第三个参数:目的数组
         * 第四个参数:目的数组放置的起始位置
         * 第五个参数：复制的长度
         */
        //socketBuffer->buffer

        try {
            System.arraycopy(socketReceiveBuffer, 12, buffer, 0, packetSize);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        //获取序列号
        int sequence = rtp_receive_packet.getSequenceNumber();
        //获取时间戳
        long timestamp = rtp_receive_packet.getTimestamp();
        Log.i("ggh", "收到数据   序列号   "+timestamp+"时间戳   " + sequence);

        /*final byte[] frame = connectFrame(buffer, packetSize, rtp_receive_packet.hasMarker(), sequence, timestamp);
        if (frame.length <= 0) {
            return;
        }
        if (callback != null) {
            callback.callback(frame);
        }*/
    }

    private Map<Integer, Packet> params;

    /**
     * 拼包
     *
     * @param buffer
     * @param packSize
     * @param islaskpack
     * @param sequence
     * @param time
     * @return
     */
    public byte[] connectFrame(byte[] buffer, int packSize, boolean islaskpack, int sequence, long time) {

        if (!params.containsKey(sequence)) {
            byte[] currentpack = new byte[packSize];
            System.arraycopy(buffer, 0, currentpack, 0, packSize);
            Packet current = new Packet(currentpack, packSize, islaskpack, sequence, time);
            params.put(sequence, current);
        }

        //判断收包是否完成
        if (islaskpack) {
            int frameSize = 0;
            for (int i = 0; i < params.size(); i++) {
                //map是否有这个key
                if (params.containsKey(i)) {
                    frameSize = frameSize + params.get(i).getBuffer().length;
                }
            }
            int lenghtafter = 0;
            byte[] frame = new byte[frameSize];
            for (int i = 0; i < params.size(); i++) {
                if (params.containsKey(i)) {
                    Packet packet = params.get(i);
                    System.arraycopy(packet.getBuffer(), 0, frame, lenghtafter, packet.getPackSize());
                    lenghtafter = lenghtafter + packet.getPackSize();
                }
            }
            clearBuffer();
            return frame;

        }
        return new byte[0];
    }


    public void clearBuffer() {
        if (params != null) {
            params.clear();
        }
    }
}
