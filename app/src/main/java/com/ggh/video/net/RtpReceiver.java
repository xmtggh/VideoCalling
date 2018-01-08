package com.ggh.video.net;

import android.util.Log;

import com.ggh.video.entity.Packet;
import com.ggh.video.rtp.RtpPacket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    List<Packet> datalist;

    private ReceiverCallback callback;

    public void setCallback(ReceiverCallback callback) {
        this.callback = callback;
    }

    public RtpReceiver() {
        datalist = new ArrayList<>();
        //初始化socketBuffer改变时rtp_Packet也跟着改变
        rtpReceivePacket = new RtpPacket(socketReceiveBuffer, 0);
//        initRxReceiver();
//        initThreadReceiver();
        DecoderThread decoder = new DecoderThread();
        decoder.start(); //启动一个线程
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
                Log.d("ggh", "接收数据包   " + rtpReceivePacket.getPayloadLength());
                if (LocalRtpSocketProvider.getInstance().getLocalRTPSocket().getDatagramSocket() != null && !LocalRtpSocketProvider.getInstance().getLocalRTPSocket().getDatagramSocket().isClosed()) {
                    connectPack(rtpReceivePacket);
                }

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
     * 初始化接收器
     */
    private void initRxReceiver() {
        Log.d("ggh1", "初始化接收器");
        Observable.create(new ObservableOnSubscribe<RtpPacket>() {
            @Override
            public void subscribe(ObservableEmitter<RtpPacket> e) {
                while (isReceiver) {
                    Log.d("ggh1", "接收数据");
                    try {
                        LocalRtpSocketProvider.getInstance().getLocalRTPSocket().receive(rtpReceivePacket);
                        Log.d("ggh1", "接收数据");
                        e.onNext(rtpReceivePacket);
                        continue;
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        continue;
                    }

                }
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(Schedulers.newThread()).subscribe(new Observer<RtpPacket>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(RtpPacket packet) {
                connectPack(packet);
                Log.d("ggh", "送去解码");
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
        final byte[] frame = connectFrame(buffer, packetSize, rtp_receive_packet.hasMarker(), sequence, timestamp);
        if (frame.length <= 0) {
            return;
        }
        if (callback != null) {
            callback.callback(frame);
        }
    }

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
        int frameSize = 0;
        byte[] currentpack = new byte[packSize];
        System.arraycopy(buffer, 0, currentpack, 0, packSize);
        Packet current = new Packet(currentpack, packSize, islaskpack, sequence, time);
        datalist.add(current);
        //判断收包是否完成
        if (islaskpack) {
            try {
                Collections.sort(datalist, new Comparator<Packet>() {
                    @Override
                    public int compare(Packet lhs, Packet rhs) {
                        return (int) (lhs.getTime() - rhs.getTime());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < datalist.size(); i++) {
                frameSize = frameSize + datalist.get(i).getBuffer().length;

            }
            int lenghtafter = 0;
            byte[] frame = new byte[frameSize];
            for (int i = 0; i < datalist.size(); i++) {
                System.arraycopy(datalist.get(i).getBuffer(), 0, frame, lenghtafter, datalist.get(i).getBuffer().length);
                lenghtafter = lenghtafter + datalist.get(i).getBuffer().length;
            }
            clearBuffer();
            return frame;

        }
        return new byte[0];
    }


    public void clearBuffer() {
        if (datalist != null) {
            datalist.clear();
        }
    }
}
