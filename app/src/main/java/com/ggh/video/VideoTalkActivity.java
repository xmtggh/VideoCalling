package com.ggh.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.apkfuns.logutils.LogUtils;
import com.ggh.video.binder.FrameProvider;
import com.ggh.video.decode.AndroidHradwareDecode;
import com.ggh.video.decode.AudioDecoder;
import com.ggh.video.device.AudioRecorder;
import com.ggh.video.device.CameraConfig;
import com.ggh.video.device.CameraManager;
import com.ggh.video.net.udp.AudioReceiver;
import com.ggh.video.net.udp.NettyReceiverHandler;
import com.vonchenchen.android_video_demos.codec.FFmpegDecodeFrame;

/**
 * Created by ZQZN on 2017/12/12.
 */

public class VideoTalkActivity extends Activity implements CameraManager.OnFrameCallback {
    private SurfaceHolder mHoder;
    SurfaceView surfaceView;
    SurfaceView textureView;
    CameraManager manager;
    private FrameProvider provider;
    //    private AndroidHradwareDecode mDecode; //硬遍
    private FFmpegDecodeFrame fFmpegDecodeFrame;//ffmpeg 软编
    private AudioRecorder audioRecorder;
    private boolean isSend = false;

    private String ip;
    private int port;
    private int localPort;


    public static void newInstance(Context context, String targetIp, String port, String localPort) {
        context.startActivity(new Intent(context, VideoTalkActivity.class)
                .putExtra("ip", targetIp)
                .putExtra("port", Integer.valueOf(port))
                .putExtra("localPort", Integer.valueOf(localPort)));

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);
        ip = getIntent().getStringExtra("ip");
        port = getIntent().getIntExtra("port", 7888);
        localPort = getIntent().getIntExtra("localPort", 7999);
        surfaceView = (SurfaceView) findViewById(R.id.surface);
        textureView = (SurfaceView) findViewById(R.id.texture);
        initSurface(textureView);
        fFmpegDecodeFrame = new FFmpegDecodeFrame(textureView.getHolder().getSurface());
        provider = new FrameProvider(ip, port, localPort, new NettyReceiverHandler.FrameResultedCallback() {
            @Override
            public void onVideoData(byte[] data) {
                fFmpegDecodeFrame.decodeStream(data, data.length);

            }

            @Override
            public void onAudioData(byte[] data) {
                AudioDecoder.getInstance().addData(data, data.length);
            }
        }, FrameProvider.ENCEDE_TYPE_ANDROIDHARDWARE);


        manager = new CameraManager(surfaceView);
        manager.setOnFrameCallback(VideoTalkActivity.this);

        //摄像头编码过后的数据
        provider.setEncodeFrameCallback(new FrameProvider.OnEncodeFrameCallback() {
            @Override
            public void onEncodeData(byte[] data) {
                LogUtils.d("编码过后的数据" + data.length);

//                mDecode.onDecodeData(data);
            }
        });
        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSend = !isSend;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    //摄像头回调yuv数据
    @Override
    public void onCameraFrame(byte[] data) {
        if (isSend)
        provider.sendVideoFrame(data);//发送去编码
//
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
//                mDecode = new AndroidHradwareDecode(surfaceHolder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            }
        });
    }

}
