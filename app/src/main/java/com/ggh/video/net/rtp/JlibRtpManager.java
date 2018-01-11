package com.ggh.video.net.rtp;

import android.util.Log;

import com.ggh.video.net.ReceiverCallback;

import java.net.DatagramSocket;
import java.util.List;

import ggh.video.com.rtplib.DataFrame;
import ggh.video.com.rtplib.Participant;
import ggh.video.com.rtplib.RTPAppIntf;
import ggh.video.com.rtplib.RTPSession;

/**
 * Created by ZQZN on 2018/1/11.
 */

public class JlibRtpManager implements RTPAppIntf {

    private RTPSession rtpSession = null;

    private static JlibRtpManager manager;

    private receiverResulted callback;

    public static JlibRtpManager getInstance() {
        if (manager == null) {
            manager = new JlibRtpManager();
        }
        return manager;
    }

    public void setCallback(receiverResulted callback) {
        this.callback = callback;
    }

    private JlibRtpManager() {
        manager = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                init();
            }
        }).start();
    }

    public void testSendData() {
        byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        byte[] data1 = {11, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        byte[] data2 = {21, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        byte[] data3 = {31, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        byte[] data4 = {41, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        byte[][] tem = {data, data1, data2, data3, data4};
        long time = System.currentTimeMillis();
        long[] seq = {0, 1, 2, 3, 4};
        boolean[] boo = {false, false, false, false, true};
        rtpSession.sendData(tem, null, boo, time, seq);
    }


    /**
     * 发送数据
     * @param buffers
     * @param markers
     * @param rtpTimestamp
     * @param seqNumbers
     */
    public void sendData(List<byte[]> buffers, List<Boolean> markers, long rtpTimestamp, List<Integer> seqNumbers) {
        rtpSession.sendData(buffers, null, markers, rtpTimestamp, seqNumbers);
    }



    private void init() {
        DatagramSocket rtpSocket = null;
        DatagramSocket rtcpSocket = null;

        try {
            rtpSocket = new DatagramSocket(6004);
            rtcpSocket = new DatagramSocket(6005);
        } catch (Exception e) {
            Log.d("ggh", "RTPSession failed to obtain port");
        }

        rtpSession = new RTPSession(rtpSocket, rtcpSocket);
        rtpSession.RTPSessionRegister(this, null, null);
        Participant p = new Participant("192.168.1.247", 6002, 6003);
        rtpSession.addParticipant(p);
    }

    @Override
    public void receiveData(DataFrame frame, Participant participant) {
        if (callback != null) {
            callback.reveiver(frame, participant);
        }
    }

    @Override
    public void userEvent(int type, Participant[] participant) {

    }

    @Override
    public int frameSize(int payloadType) {
        return 0;
    }

    public interface receiverResulted {
        void reveiver(DataFrame frame, Participant participant);
    }
}
