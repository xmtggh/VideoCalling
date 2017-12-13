package com.ggh.video;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.ggh.video.device.CameraManager;
import com.ggh.video.encode.AndroidHradwareEncode;
import com.ggh.video.encode.Encode;
import com.ggh.video.net.Frame;
import com.ggh.video.net.UDPReceiver;
import com.ggh.video.net.UDPSender;
import com.ggh.video.utils.CheckPermissionUtils;

/**
 * Created by ZQZN on 2017/12/12.
 */

public class VideoTalkActivity extends Activity implements CameraManager.OnFrameCallback {
    SurfaceView surfaceView;
    CameraManager manager;
    private Encode mEncode;
    private UDPSender sender;
    private UDPReceiver receiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);
        surfaceView = (SurfaceView) findViewById(R.id.surface);
        mEncode = new AndroidHradwareEncode();
        sender = new UDPSender();
//        receiver = new UDPReceiver();
        sender.startSender();
//        receiver.startRecivice();
        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] data = {1,2,3,4};
                        sender.testSendData(data,3);
                    }
                }).start();

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        manager = new CameraManager(surfaceView);
        manager.setOnFrameCallback(this);
    }

    @Override
    public void onFrame(byte[] data) {
        sender.addData(new Frame(mEncode.encodeFrame(data), mEncode.encodeFrame(data).length));
//        sender.sendData(mEncode.encodeFrame(data),mEncode.encodeFrame(data).length);
    }

}
