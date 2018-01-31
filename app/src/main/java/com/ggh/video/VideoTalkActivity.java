package com.ggh.video;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.ggh.video.decode.AndroidHradwareDecode;
import com.ggh.video.device.CameraManager;
import com.ggh.video.encode.AndroidHradwareEncode;
import com.ggh.video.encode.Encode;
import com.ggh.video.net.ReceiverCallback;
import com.ggh.video.net.udp.UDPReceiver;
import com.ggh.video.net.udp.UDPSender;

/**
 * Created by ZQZN on 2017/12/12.
 */

public class VideoTalkActivity extends Activity implements CameraManager.OnFrameCallback,ReceiverCallback {
    private SurfaceHolder mHoder;
    SurfaceView surfaceView;
    SurfaceView textureView;
    CameraManager manager;
    private Encode mEncode;
    private AndroidHradwareDecode mDecode;
    private UDPSender sender;
    private UDPReceiver receiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);
        surfaceView = (SurfaceView) findViewById(R.id.surface);
        textureView = (SurfaceView) findViewById(R.id.texture);
        initSurface(textureView);
        mEncode = new AndroidHradwareEncode();
        sender = new UDPSender();
        receiver = new UDPReceiver();
        receiver.startRecivice();
        receiver.setCallback(this);
        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] data = {1, 2, 3};

                        sender.addData(data);
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
        byte[] encode = mEncode.encodeFrame(data);
        Log.w("video", "发送数据 大小为" + encode.length);
        sender.addData(encode);
    }

    @Override
    public void callback(final byte[] data) {
        if (mDecode != null) {
            Log.w("video", "接收数据 大小为" + data.length);
            mDecode.onDecodeData(data);

        }
    }


    /**
     * 初始化预览界面
     *
     * @param mSurfaceView
     */
    private void initSurface(SurfaceView mSurfaceView) {
        mHoder = mSurfaceView.getHolder();
        mHoder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHoder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                mDecode = new AndroidHradwareDecode(surfaceHolder);
            }
            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
//                destroy();
            }
        });
    }


}
