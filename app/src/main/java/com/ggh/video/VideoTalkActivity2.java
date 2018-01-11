package com.ggh.video;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.ggh.video.decode.VideoDecodeManager;
import com.ggh.video.device.CameraManager;
import com.ggh.video.encode.AndroidHradwareEncode;
import com.ggh.video.encode.Encode;
import com.ggh.video.net.Frame;
import com.ggh.video.net.ReceiverCallback;
import com.ggh.video.net.other.JRtpSender;
import com.ggh.video.net.rtp.JlibRtpReceiver;
import com.ggh.video.net.rtp.JlibRtpSender;

/**
 * Created by ZQZN on 2017/12/12.
 */

public class VideoTalkActivity2 extends Activity implements CameraManager.OnFrameCallback, ReceiverCallback {
    private SurfaceHolder mHoder;
    SurfaceView surfaceView;
    SurfaceView textureView;
    CameraManager manager;
    private Encode mEncode;
    private VideoDecodeManager mDecode;
    private JlibRtpSender sender;
    private JlibRtpReceiver receiver;
    private Frame mFrame;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);
        surfaceView = (SurfaceView) findViewById(R.id.surface);
        textureView = (SurfaceView) findViewById(R.id.texture);
        initSurface(textureView);
        mFrame = new Frame();
        mEncode = new AndroidHradwareEncode();
        sender = new JlibRtpSender();
//        sender.setCallback(this);
        receiver = new JlibRtpReceiver();
        receiver.setCallback(this);
        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
                                11, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
                                21, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
                                31, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
                                41, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
                        mFrame.setData(data);
                        mFrame.setSize(data.length);
                        sender.addData(mFrame);
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
//        mDecode.onDecodeData(encode);

        Log.w("video", "发送数据 大小为" + encode.length);
        mFrame.setData(encode);
        mFrame.setSize(encode.length);
        sender.addData(mFrame);
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
                mDecode = new VideoDecodeManager(surfaceHolder);

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

    @Override
    public void callback(byte[] data) {
        if (mDecode != null && data.length > 0) {
            Log.w("video", "接收数据 大小为" + data.length);
            mDecode.onDecodeData(data);
        }
    }
}
