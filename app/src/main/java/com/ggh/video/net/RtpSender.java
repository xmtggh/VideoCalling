package com.ggh.video.net;

import android.util.Log;

import com.ggh.video.rtp.RtpPacket;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ZQZN on 2018/1/8.
 */

public class RtpSender extends Send {
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

    private boolean doTask = true;

    public RtpSender() {
        rtpSendPacket = new RtpPacket(socketSendBuffer, 0);
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
//        send(frame);
        rxSend(frame);
    }

    private void rxSend(final Frame frame) {
        Observable.create(new ObservableOnSubscribe<Frame>() {
            @Override
            public void subscribe(ObservableEmitter<Frame> e) throws Exception {
                e.onNext(frame);
            }
        }).subscribeOn(Schedulers.newThread()).subscribe(new Observer<Frame>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Frame fram) {
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
                if (sendPacketNum > 0) {
                    //通过RTP协议发送帧
                    //从码流头部开始取
                    int pos = 0;
                    //设定时间戳
                    final long timestamp = System.currentTimeMillis();
                    //因为可能传输数据过大 会将一次数据分割成好几段来传输
                    //接受方 根据序列号和结束符 来将这些数据拼接成完整数据
                    //定义负载类型，视频为2
                    for (int i = 0; i < sendPacketNum; i++) {
                        rtpSendPacket.setPayloadType(2);
                        //是否是最后一个RTP包
                        rtpSendPacket.setMarker(i == sendPacketNum - 1 ? true : false);
                        //序列号依次加1
                        rtpSendPacket.setSequenceNumber(i);
                        //时间戳
                        rtpSendPacket.setTimestamp(timestamp);
                        rtpSendPacket.setPayloadLength(sendPacketSize[i]);
                        //从码流stream的pos处开始复制，从socketBuffer的第12个字节开始粘贴，packetSize为粘贴的长度
                        //把一个包存在socketBuffer中
                        System.arraycopy(fram.getData(), pos, socketSendBuffer, 12, sendPacketSize[i]);
                        //重定义下次开始复制的位置
                        pos = pos + sendPacketSize[i];
                        sendPacketSize[i] = 0;
                        try {
                            LocalRtpSocketProvider.getInstance().getLocalRTPSocket().send(rtpSendPacket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

}
