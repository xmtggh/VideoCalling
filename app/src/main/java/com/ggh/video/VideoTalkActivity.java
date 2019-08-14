package com.ggh.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.ggh.video.base.DecodeManager;
import com.ggh.video.base.EncodeManager;
import com.ggh.video.decode.AudioDecoder;
import com.ggh.video.decode.HardwareDecoder;
import com.ggh.video.encode.IEncoderCallback;
import com.ggh.video.encode.X264Encoder;
import com.ggh.video.net.udp.Message;
import com.ggh.video.net.udp.NettyClient;
import com.ggh.video.device.CameraManager;
import com.ggh.video.net.udp.NettyReceiverHandler;

/**
 * Created by ZQZN on 2017/12/12.
 */

public class VideoTalkActivity extends Activity implements CameraManager.OnFrameCallback, NettyReceiverHandler.FrameResultedCallback{
    private SurfaceHolder mHoder;
    //播放端
    SurfaceView playerView;
    //预览端
    SurfaceView previewView;
    CameraManager manager;
    //编码
    private EncodeManager mEncodeManager;
    //解码
    private DecodeManager mDecodeManager;
    //netty传输
    private NettyClient mNettyClient;
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
        playerView = (SurfaceView) findViewById(R.id.surface);
        previewView = (SurfaceView) findViewById(R.id.texture);
        initSurface(previewView);
        //初始化编码器
        mEncodeManager = new X264Encoder();
        //编码后数据
        mEncodeManager.setEncodeCallback(new IEncoderCallback() {
            @Override
            public void onEncodeCallback(byte[] data) {
                //传输
                mNettyClient.sendData(data, Message.MES_TYPE_VIDEO);
                //不传输直接渲染
//                mDecodeManager.onDecodeData(data);
            }
        });
        //使用netty传输接收
        mNettyClient = new NettyClient.
                Builder().targetIp(ip)
                .targetPort(port)
                .localPort(localPort)
                .frameResultedCallback(this)
                .build();


        manager = new CameraManager(playerView);
        manager.setOnFrameCallback(VideoTalkActivity.this);

        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSend = !isSend;
            }
        });

    }

    /**
     * 摄像头回调数据
     * @param data
     */
    @Override
    public void onCameraFrame(byte[] data) {
        if (isSend) {
            //发送去编码
            mEncodeManager.onEncodeData(data);
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
                mDecodeManager = new HardwareDecoder(surfaceHolder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            }
        });
    }

    /**
     * 接收视频回调
     * @param data
     */
    @Override
    public void onVideoData(byte[] data) {
        mDecodeManager.onDecodeData(data);

    }

    /**
     * 接收音频回调
     * @param data
     */
    @Override
    public void onAudioData(byte[] data) {
        AudioDecoder.getInstance().addData(data, data.length);

    }
}
