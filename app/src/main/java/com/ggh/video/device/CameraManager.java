package com.ggh.video.device;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.apkfuns.logutils.LogUtils;

import java.io.IOException;

/**
 * Created by ZQZN on 2017/12/12.
 */

public class CameraManager {
    private Camera mCamera;
    private SurfaceHolder mHoder;
    private OnFrameCallback onFrameCallback;

    /**
     * 初始化界面
     *
     * @param mSurfaceView
     */
    public CameraManager(SurfaceView mSurfaceView) {
        initSurface(mSurfaceView);
    }

    /**
     * 设置帧数据回调
     *
     * @param onFrameCallback
     */
    public void setOnFrameCallback(OnFrameCallback onFrameCallback) {
        this.onFrameCallback = onFrameCallback;
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
                initCamera();

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                Log.d("ggh","预览摄像头");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
//                destroy();
            }
        });
    }


    /**
     * 初始化摄像头
     */
    private void initCamera() {
        if (mCamera == null) {
            //摄像头设置，预览视频,实例化摄像头类对象  0为后置 1为前置
            mCamera = Camera.open(0);
            //视频旋转90度
            mCamera.setDisplayOrientation(90);
            //将摄像头参数传入p中
            Camera.Parameters p = mCamera.getParameters();
            p.setFlashMode("off");
            //设置预览视频的尺寸，CIF格式352×288
            p.setPreviewSize(CameraConfig.WIDTH, CameraConfig.HEIGHT);
            //设置预览的帧率，15帧/秒
            p.setPreviewFrameRate(CameraConfig.framerate);
            p.setPreviewFormat(ImageFormat.NV21);
            //设置参数
            mCamera.setParameters(p);
//            byte[] rawBuf = new byte[1400];
//            mCamera.addCallbackBuffer(rawBuf);
            try {
                //预览的视频显示到指定窗口
                mCamera.setPreviewDisplay(mHoder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.startPreview();
            //获取帧
            //预览的回调函数在开始预览的时候以中断方式被调用，每秒调用15次，回调函数在预览的同时调出正在播放的帧
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, Camera camera) {
                    onFrameCallback.onCameraFrame(bytes);
                }
            });
        }
    }

    public void startPreview(){
        //开始预览
        mCamera.startPreview();
    }

    /**
     * 销毁
     */
    public void destroy() {
        if (mCamera != null) {
            //停止回调函数
            mCamera.setPreviewCallback(null);
            //停止预览
            mCamera.stopPreview();
            //释放资源
            mCamera.release();
            //重新初始化
            mCamera = null;
        }
    }

    public interface OnFrameCallback {
        void onCameraFrame(byte[] data);
    }
}
