package com.video.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

import ggh.video.com.rtplib.DataFrame;
import ggh.video.com.rtplib.Participant;
import ggh.video.com.rtplib.RTPAppIntf;
import ggh.video.com.rtplib.RTPSession;

public class MainActivity2 extends Activity implements RTPAppIntf {
    private Button btn_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //try { Thread.currentThread().sleep(10000); } catch (Exception e) {  };
                        long teststart = System.currentTimeMillis();
                        byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
                        byte[] data1 = {11, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
                        byte[] data2 = {21, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
                        byte[] data3 = {31, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
                        byte[] data4 = {41, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
                        List<byte[]> tem = new ArrayList<>();
                        tem.add(data);
                        tem.add(data1);
                        tem.add(data2);
                        tem.add(data3);
                        tem.add(data4);

                        long time = System.currentTimeMillis();
                        List<Integer> seq =new ArrayList<>();
                        seq.add(0);
                        seq.add(1);
                        seq.add(2);
                        seq.add(3);
                        seq.add(4);
                        List<Boolean> boo = new ArrayList<>();
                        boo.add(false);
                        boo.add(false);
                        boo.add(false);
                        boo.add(false);
                        boo.add(true);
                        rtpSession.sendData(tem,null,boo, time, seq);
                        //try { Thread.currentThread().sleep(500); } catch (Exception e) {  };
                        long testend = System.currentTimeMillis();

                        Log.d("ggh", "" + (testend - teststart));
                    }
                }).start();

            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                init();

            }
        }).start();
    }

    public RTPSession rtpSession = null;

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
        Participant p = new Participant("192.168.1.207", 6004, 6005);
        rtpSession.addParticipant(p);
    }


    @Override
    public void receiveData(DataFrame frame, Participant p) {
        String s = new String(frame.getConcatenatedData());
        Log.d("ggh", "The Data has been received: " + s + " , thank you ");

    }

    @Override
    public void userEvent(int type, Participant[] participant) {
        //Do nothing
    }

    @Override
    public int frameSize(int payloadType) {
        return 1;
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
