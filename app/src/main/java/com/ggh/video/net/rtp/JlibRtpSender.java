package com.ggh.video.net.rtp;

import com.ggh.video.entity.Packet;
import com.ggh.video.net.Frame;
import com.ggh.video.net.LocalRtpSocketProvider;
import com.ggh.video.net.NetConfig;
import com.ggh.video.net.ReceiverCallback;
import com.ggh.video.net.Send;
import com.ggh.video.rtp.RtpPacket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ZQZN on 2018/1/8.
 */

public class JlibRtpSender extends Send {
    /**
     * 创建RTP发送包
     */
    private RtpPacket rtpSendPacket = null;

    /**
     * 单个包大小
     */
    int siglepackSize = NetConfig.packSize;
    /**
     * Rtp缓冲区
     */
    private byte[] socketSendBuffer = new byte[20000];
    /**
     * 包的尺寸
     */
    final int[] sendPacketSize = new int[NetConfig.packSize];

    private RtspPacketSender sender;

    private boolean doTask = true;

    private Map<Integer, Packet> params;

    private ReceiverCallback callback;

    public void setCallback(ReceiverCallback callback) {
        this.callback = callback;
    }

    public JlibRtpSender() {
        params = new HashMap<>();
        rtpSendPacket = new RtpPacket(socketSendBuffer, 0);
        sender = new RtspPacketSender(new RtspPacketSender.H264ToRtpLinsener() {
            @Override
            public void h264ToRtpResponse(byte[] out, int len) {
                try {
                    LocalRtpSocketProvider.getInstance().getLocalRTPSocket().send(new RtpPacket(out, len));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void addData(Frame frame) {
        if (doTask) {
            sendData(frame);
        }
    }

    @Override
    public void startSender() {
        doTask = true;

    }

    @Override
    public void stopSender() {
        doTask = false;

    }

    @Override
    public void destroy() {

    }

    private void sendData(final Frame frame) {
        rxSend(frame);
//        JlibRtpManager.getInstance().testSendData();
    }

    private void rxSend(final Frame frame) {
//        connectPacket1(frame);
        Observable.create(new ObservableOnSubscribe<Frame>() {
            @Override
            public void subscribe(ObservableEmitter<Frame> e) throws Exception {
                e.onNext(frame);
            }
        }).subscribeOn(Schedulers.io()).subscribe(new Observer<Frame>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Frame fram) {
                connectPacket1(fram);
//                JlibRtpManager.getInstance().testSendData();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void connectPacket1(Frame fram) {
        List<byte[]> frameData = new ArrayList<>();
        List<Boolean> market = new ArrayList<>();
        long timestamp = System.currentTimeMillis();
        List<Integer> seq = new ArrayList<>();
        //最后一个包
        int lastpacket = -1;
        //获取发送包的分割数目
        int sendPacketNum = fram.getSize() % siglepackSize == 0 ? fram.getSize() / siglepackSize : fram.getSize() / siglepackSize + 1;
        //判断是否整数分割
        int allSize = fram.getSize();
        for (int j = 0; j < sendPacketNum; j++) {
            allSize = allSize - siglepackSize;
            if (allSize > 0) {
                sendPacketSize[j] = siglepackSize;
            } else {
                sendPacketSize[j] = siglepackSize + allSize;
            }

        }
        int pos = 0;
        if (sendPacketNum > 0) {
            for (int i = 0; i < sendPacketNum; i++) {
                byte[] data = new byte[sendPacketSize[i]];
                System.arraycopy(fram.getData(), pos, data, 0, sendPacketSize[i]);
                pos +=sendPacketSize[i];
                frameData.add(data);
                seq.add(i);
                if (i == sendPacketNum - 1) {
                    market.add(true);
//                    callback.callback(connectFrame(data, data.length, true, i, timestamp));
                } else {
                    market.add(false);
//                    callback.callback(connectFrame(data, data.length, false, i, timestamp));
                }

            }

        }
        //发送包
        JlibRtpManager.getInstance().sendData(frameData, market, timestamp, seq);

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
