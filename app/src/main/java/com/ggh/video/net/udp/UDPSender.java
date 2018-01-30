package com.ggh.video.net.udp;

import android.util.Log;

import com.ggh.video.net.Frame;
import com.ggh.video.net.LocalUDPSocketProvider;
import com.ggh.video.net.NetConfig;
import com.ggh.video.net.Send;
import com.ggh.video.net.rtp.RtspPacketReceiver;
import com.ggh.video.net.rtp.RtspPacketSender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by ZQZN on 2017/12/12.
 */

public class UDPSender extends Send {
    private List<Frame> bufferFrameList;

    private DatagramPacket mPacket;

    private DatagramSocket mSocket;

    private boolean isStratSendData = false;

    private InetAddress ip;

    private RtspPacketSender sender;

    //nio udp
    DatagramChannel channel;


    public UDPSender() {
       /* bufferFrameList = new ArrayList<>();
        mSocket = LocalUDPSocketProvider.getInstance().getLocalUDPSocket();
        sender = new RtspPacketSender(new RtspPacketSender.H264ToRtpLinsener() {
            @Override
            public void h264ToRtpResponse(byte[] out, int len) {
                sendData(out, len);
            }
        });
        try {
            ip = NetConfig.getIpAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }*/
//        initDataPipe();
//        initTaskSend();
        try {
            channel = DatagramChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 初始化数据流，在一个新的线程开启并发送
     */
    private void initDataPipe() {
        Observable.create(new ObservableOnSubscribe<Frame>() {
            @Override
            public void subscribe(ObservableEmitter<Frame> e) throws Exception {
                while (isStratSendData) {
                    if (bufferFrameList.size() > 0) {
                        e.onNext(bufferFrameList.get(0));
                        Log.d("pre", "预处理成功");
                    }
                    Log.d("pre", "预处理成功11111");
                    continue;
                }
            }
        }).subscribe(new Observer<Frame>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Frame frame) {
//                sendData(frame.getData(),frame.getSize());
                Log.d("pre", "onNext");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }


    private void initTaskSend() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                isStratSendData = true;
                while (isStratSendData) {
                    if (bufferFrameList != null && bufferFrameList.size() > 0) {
                        Frame frame = bufferFrameList.get(0);
                        bufferFrameList.remove(0);
                        sendData(frame.getData(), frame.getSize());
                    }
                }
            }
        }).start();

    }

    /**
     * 发送数据
     *
     * @param data
     * @param size
     */
    public void sendData(byte[] data, int size) {
        try {
            mPacket = new DatagramPacket(data, size, ip, NetConfig.REMOTEPORT);
            mPacket.setData(data);
            mSocket.send(mPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testSendData(byte[] data, int size) {
        try {
            mPacket = new DatagramPacket(data, size, NetConfig.getIpAddress(), NetConfig.REMOTEPORT);
            mPacket.setData(data);
            Log.e("udpsender", "测试发送一段数据 " + data.length + ":" + size);
            mSocket.send(mPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addData(final Frame frame) {
        /*new Thread(new Runnable() {
            @Override
            public void run() {
//              sendData(frame.getData(), frame.getSize());
                try {
                    sender.h264ToRtp(frame.getData(), frame.getSize());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();*/
        send(frame.getData());

        /*if (null != bufferFrameList) {
            bufferFrameList.add(frame);
        }*/
    }

    @Override
    public void startSender() {
        isStratSendData = true;

    }

    @Override
    public void stopSender() {
        isStratSendData = false;
    }

    @Override
    public void destroy() {

    }

    public void send(final byte[] data) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sendMessage(channel,data);
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
        int send = channel.send(buffer, new InetSocketAddress("127.0.0.1", 1234));
        Log.d("ggh", "发送 " + send + " 字节");
    }
}
