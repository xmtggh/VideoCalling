package com.lkl.opengl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 创建者     likunlun
 * 创建时间   2019/3/26 17:17
 * 描述	      GLSurfaceView.Renderer 渲染类
 */
class MyGLRenderer : GLSurfaceView.Renderer {
    companion object {
        private const val TAG = "MyGLRenderer"
    }

    private lateinit var mProgram: MyGLProgram
    // GLSurfaceView宽度
    private var mScreenWidth: Int = 0
    // GLSurfaceView高度
    private var mScreenHeight: Int = 0
    // 预览YUV数据宽度
    private var mVideoWidth: Int = 0
    // 预览YUV数据高度
    private var mVideoHeight: Int = 0

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    // y分量数据
    private var y: ByteBuffer = ByteBuffer.allocate(0)
    // u分量数据
    private var u: ByteBuffer = ByteBuffer.allocate(0)
    // v分量数据
    private var v: ByteBuffer = ByteBuffer.allocate(0)
    // uv分量数据
    private var uv: ByteBuffer = ByteBuffer.allocate(0)

    // YUV数据格式 0 -> I420  1 -> NV12  2 -> NV21
    private var type: Int = 0
    // 标识GLSurfaceView是否准备好
    private var hasVisibility = false

    //  Called once to set up the view's OpenGL ES environment.
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // 配置OpenGL ES 环境
        mProgram = MyGLProgram()
    }

    //  Called if the geometry of the view changes, for example when the device's screen orientation changes.
    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        mScreenWidth = width
        mScreenHeight = height

        mScreenWidth = width
        mScreenHeight = height
        val ratio: Float = width.toFloat() / height.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 1.0f, 0.0f, 0.0f)

        if (mVideoWidth > 0 && mVideoHeight > 0) {
            createBuffers(mVideoWidth, mVideoHeight)
        }
        hasVisibility = true
        Log.d(TAG, "onSurfaceChanged width:$width * height:$height")
    }

    //  Called for each redraw of the view.
    override fun onDrawFrame(unused: GL10) {
        synchronized(this) {
            if (y.capacity() > 0) {
                y.position(0)
                if (type == 0) {
                    u.position(0)
                    v.position(0)
                    mProgram.feedTextureWithImageData(y, u, v, mVideoWidth, mVideoHeight)
                } else {
                    uv.position(0)
                    mProgram.feedTextureWithImageData(y, uv, mVideoWidth, mVideoHeight)
                }
                // Redraw background color
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

                // Calculate the projection and view transformation
                Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

                try {
                    mProgram.drawTexture(vPMatrix, type)
                } catch (e: Exception) {
                    Log.w(TAG, e.message)
                }
            }
        }
    }

    /**
     * 设置显示方向
     * @param degrees 显示旋转角度（逆时针），有效值是（0, 90, 180, and 270.）
     */
    fun setDisplayOrientation(degrees: Int) {
        // Set the camera position (View matrix)
        if (degrees == 0) {
            Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 1.0f, 0.0f, 0.0f)
        } else if (degrees == 90) {
            Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0.0f, 1.0f, 0.0f)
        } else if (degrees == 180) {
            Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, -1.0f, 0.0f, 0.0f)
        } else if (degrees == 270) {
            Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0.0f, -1.0f, 0.0f)
        } else {
            Log.e(TAG, "degrees pram must be in (0, 90, 180, 270) ")
        }
    }

    /**
     * 设置渲染的YUV数据的宽高
     * @param width 宽度
     * @param height 高度
     */
    fun setYuvDataSize(width: Int, height: Int) {
        if (width > 0 && height > 0) {
            // 调整比例
            createBuffers(width, height)

            // 初始化容器
            if (width != mVideoWidth && height != mVideoHeight) {
                this.mVideoWidth = width
                this.mVideoHeight = height
                val yarraySize = width * height
                val uvarraySize = yarraySize / 4
                synchronized(this) {
                    y = ByteBuffer.allocate(yarraySize)
                    u = ByteBuffer.allocate(uvarraySize)
                    v = ByteBuffer.allocate(uvarraySize)
                    uv = ByteBuffer.allocate(uvarraySize * 2)
                }
            }
        }
    }

    /**
     * 调整渲染纹理的缩放比例
     * @param width YUV数据宽度
     * @param height YUV数据高度
     */
    private fun createBuffers(width: Int, height: Int) {
        if (mScreenWidth > 0 && mScreenHeight > 0) {
            val f1 = mScreenHeight.toFloat() / mScreenWidth.toFloat()
            val f2 = height.toFloat() / width.toFloat()
            if (f1 == f2) {
                mProgram.createBuffers(MyGLProgram.squareVertices)
            } else if (f1 < f2) {
                val widthScale = f1 / f2
                mProgram.createBuffers(
                    floatArrayOf(
                        -widthScale,
                        -1.0f,
                        widthScale,
                        -1.0f,
                        -widthScale,
                        1.0f,
                        widthScale,
                        1.0f
                    )
                )
            } else {
                val heightScale = f2 / f1
                mProgram.createBuffers(
                    floatArrayOf(
                        -1.0f,
                        -heightScale,
                        1.0f,
                        -heightScale,
                        -1.0f,
                        heightScale,
                        1.0f,
                        heightScale
                    )
                )
            }
        }
    }

    /**
     * 预览YUV格式数据
     * @param yuvdata yuv格式的数据
     * @param type YUV数据的格式 0 -> I420  1 -> NV12  2 -> NV21
     */
    fun feedData(yuvdata: ByteArray, type: Int = 0) {
        synchronized(this) {
            if (hasVisibility) {
                this.type = type
                if (type == 0) {
                    y.clear()
                    u.clear()
                    v.clear()
                    y.put(yuvdata, 0, mVideoWidth * mVideoHeight)
                    u.put(yuvdata, mVideoWidth * mVideoHeight, mVideoWidth * mVideoHeight / 4)
                    v.put(yuvdata, mVideoWidth * mVideoHeight * 5 / 4, mVideoWidth * mVideoHeight / 4)
                } else {
                    y.clear()
                    uv.clear()
                    y.put(yuvdata, 0, mVideoWidth * mVideoHeight)
                    uv.put(yuvdata, mVideoWidth * mVideoHeight, mVideoWidth * mVideoHeight / 2)
                }
            }
        }
    }
}