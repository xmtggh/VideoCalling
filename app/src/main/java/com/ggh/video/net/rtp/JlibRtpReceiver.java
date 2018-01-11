package com.ggh.video.net.rtp;

import android.util.Log;

import com.ggh.video.entity.Packet;
import com.ggh.video.net.Frame;
import com.ggh.video.net.LocalRtpSocketProvider;
import com.ggh.video.net.Receiver;
import com.ggh.video.net.ReceiverCallback;
import com.ggh.video.netty.EchoSeverHandler;
import com.ggh.video.rtp.RtpPacket;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import ggh.video.com.rtplib.DataFrame;
import ggh.video.com.rtplib.Participant;
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

public class JlibRtpReceiver extends Receiver implements JlibRtpManager.receiverResulted {
    private String tag = "RtpReceiver";
    /**
     * 是否接收的标志
     */
    private boolean isReceiver = true;

    private Map<Integer, Packet> params;


    private ReceiverCallback callback;
    private LinkedList<RtpPacket> bufferFrameList;
    private RtspPacketReceiver receiver;

    public void setCallback(ReceiverCallback callback) {
        this.callback = callback;
    }

    public JlibRtpReceiver() {
        params = new HashMap<>();
        //初始化socketBuffer改变时rtp_Packet也跟着改变
        receiver = new RtspPacketReceiver(640, 480);
        JlibRtpManager.getInstance().setCallback(this);
        bufferFrameList = new LinkedList<>();
    }

    @Override
    public void reveiver(final DataFrame frame, Participant participant) {
        Observable.create(new ObservableOnSubscribe<byte[]>() {
            @Override
            public void subscribe(ObservableEmitter<byte[]> e) throws Exception {
                 byte[] framed = connectFrame(frame.getConcatenatedData(), frame.getConcatenatedData().length, frame.marked(), frame.sequenceNumbers()[0], frame.rtpTimestamp());
                if (framed.length <= 0) {
                    return;
                }
                e.onNext(framed);
            }
        }).subscribeOn(Schedulers.newThread()).subscribe(new Observer<byte[]>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(byte[] fram) {
                if (callback!=null){
                    callback.callback(fram);
                }
                Log.w("ggh", "拼帧大小" + fram.length);
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


    @Override
    public void startRecivice() {
        isReceiver = true;
    }

    @Override
    public void stopRecivice() {
        isReceiver = false;
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
