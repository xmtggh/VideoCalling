package com.ggh.video.net.other;

import android.util.Log;

import com.ggh.video.net.Frame;
import com.ggh.video.net.Send;

import java.net.DatagramSocket;

import ggh.video.com.rtplib.DataFrame;
import ggh.video.com.rtplib.Participant;
import ggh.video.com.rtplib.RTPAppIntf;
import ggh.video.com.rtplib.RTPSession;

/**
 * Created by ZQZN on 2018/1/9.
 */

public class JRtpSender extends Send implements RTPAppIntf {

    public RTPSession rtpSession = null;

    public JRtpSender() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                init();

            }
        }).start();
    }

    private void init() {
        DatagramSocket rtpSocket = null;
        DatagramSocket rtcpSocket = null;

        try {
            rtpSocket = new DatagramSocket(6002);
            rtcpSocket = new DatagramSocket(6003);
        } catch (Exception e) {
            Log.d("ggh", "RTPSession failed to obtain port");
        }


        rtpSession = new RTPSession(rtpSocket, rtcpSocket);

        rtpSession.RTPSessionRegister(this, null, null);


        Participant p = new Participant("192.168.1.247", 6004, 6005);

        rtpSession.addParticipant(p);
    }

    @Override
    public void addData(final Frame frame) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                rtpSession.sendData(frame.getData());
                //try { Thread.currentThread().sleep(500); } catch (Exception e) {  };

            }
        }).start();
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

    @Override
    public void receiveData(DataFrame frame, Participant participant) {
        Log.d("ggh", "收到jrtp  " + frame.getConcatenatedData());
    }

    @Override
    public void userEvent(int type, Participant[] participant) {

    }

    @Override
    public int frameSize(int payloadType) {
        return 0;
    }
}
