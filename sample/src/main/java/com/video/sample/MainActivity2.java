package com.video.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.net.DatagramSocket;

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
                        String str = "abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd abce abcd";


                        byte[] data = str.getBytes();
                        System.out.println(data.length);

                        int i = 0;
                        while (i < 100) {
                            rtpSession.sendData(data);
                            //try { Thread.currentThread().sleep(500); } catch (Exception e) {  };
                            i++;
                        }

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
