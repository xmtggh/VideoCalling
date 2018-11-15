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
                public void onPreviewFrame(byte[] data, Camera camera) {
                    onFrameCallback.onCameraFrame(data);

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

    private byte[] rotateYUVDegree90(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    private byte[] rotateYUVDegree270AndMirror(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate and mirror the Y luma
        int i = 0;
        int maxY = 0;
        for (int x = imageWidth - 1; x >= 0; x--) {
            maxY = imageWidth * (imageHeight - 1) + x * 2;
            for (int y = 0; y < imageHeight; y++) {
                yuv[i] = data[maxY - (y * imageWidth + x)];
                i++;
            }
        }
        // Rotate and mirror the U and V color components
        int uvSize = imageWidth * imageHeight;
        i = uvSize;
        int maxUV = 0;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            maxUV = imageWidth * (imageHeight / 2 - 1) + x * 2 + uvSize;
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[maxUV - 2 - (y * imageWidth + x - 1)];
                i++;
                yuv[i] = data[maxUV - (y * imageWidth + x)];
                i++;
            }
        }
        return yuv;
    }



}
