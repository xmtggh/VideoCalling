package com.lkl.opengl

import android.opengl.GLES20
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

/**
 * 创建者     likunlun
 * 创建时间   2019/3/26 17:23
 * 描述	      desc
 */
class MyGLProgram {
    companion object {
        private const val TAG = "MyGLProgram"
        var squareVertices = floatArrayOf(-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f) // fullscreen
    }

    private var mProgram: Int

    private var mPlanarTextureHandles = IntBuffer.wrap(IntArray(3))
    private val mSampleHandle = IntArray(3)
    // handles
    private var mPositionHandle = -1
    private var mCoordHandle = -1
    private var mVPMatrixHandle: Int = -1

    // vertices buffer
    private var mVertexBuffer: FloatBuffer? = null
    private var mCoordBuffer: FloatBuffer? = null
    // whole-texture
    private val mCoordVertices = floatArrayOf(0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f)

    init {
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        Log.d(TAG, "vertexShader = $vertexShader \n fragmentShader = $fragmentShader")

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram().also {
            checkGlError("glCreateProgram")
            // add the vertex shader to program
            GLES20.glAttachShader(it, vertexShader)

            // add the fragment shader to program
            GLES20.glAttachShader(it, fragmentShader)

            // creates OpenGL ES program executables
            GLES20.glLinkProgram(it)
        }

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.w(TAG, "Could not link program: ${GLES20.glGetProgramInfoLog(mProgram)}")
            GLES20.glDeleteProgram(mProgram)
            mProgram = 0
        }

        Log.d(TAG, "mProgram = $mProgram")

        checkGlError("glCreateProgram")

        // 生成纹理句柄
        GLES20.glGenTextures(3, mPlanarTextureHandles)

        checkGlError("glGenTextures")
    }

    /**
     * 绘制纹理贴图
     * @param mvpMatrix 顶点坐标变换矩阵
     * @param type YUV数据格式类型
     */
    fun drawTexture(mvpMatrix: FloatArray, type: Int) {

        GLES20.glUseProgram(mProgram)
        checkGlError("glUseProgram")
        /*
         * get handle for "vPosition" and "a_texCoord"
         */
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {
            GLES20.glVertexAttribPointer(it, 2, GLES20.GL_FLOAT, false, 8, mVertexBuffer)
            GLES20.glEnableVertexAttribArray(it)
        }

        // 传纹理坐标给fragment shader
        mCoordHandle = GLES20.glGetAttribLocation(mProgram, "texCoord").also {
            GLES20.glVertexAttribPointer(it, 2, GLES20.GL_FLOAT, false, 8, mCoordBuffer)
            GLES20.glEnableVertexAttribArray(it)
        }

        // get handle to shape's transformation matrix
        mVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mVPMatrixHandle, 1, false, mvpMatrix, 0)

        //传纹理的像素格式给fragment shader
        val yuvType = GLES20.glGetUniformLocation(mProgram, "yuvType")
        checkGlError("glGetUniformLocation yuvType")
        GLES20.glUniform1i(yuvType, type)

        //type: 0是I420, 1是NV12
        var planarCount = 0
        if (type == 0) {
            //I420有3个平面
            planarCount = 3
            mSampleHandle[0] = GLES20.glGetUniformLocation(mProgram, "samplerY")
            mSampleHandle[1] = GLES20.glGetUniformLocation(mProgram, "samplerU")
            mSampleHandle[2] = GLES20.glGetUniformLocation(mProgram, "samplerV")
        } else {
            //NV12、NV21有两个平面
            planarCount = 2
            mSampleHandle[0] = GLES20.glGetUniformLocation(mProgram, "samplerY")
            mSampleHandle[1] = GLES20.glGetUniformLocation(mProgram, "samplerUV")
        }
        (0 until planarCount).forEach { i ->
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mPlanarTextureHandles[i])
            GLES20.glUniform1i(mSampleHandle[i], i)
        }

        // 调用这个函数后，vertex shader先在每个顶点执行一次，之后fragment shader在每个像素执行一次，
        // 绘制后的图像存储在render buffer中
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glFinish()

        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mCoordHandle)
    }

    /**
     * 将图片数据绑定到纹理目标，适用于UV分量分开存储的（I420）
     * @param yPlane YUV数据的Y分量
     * @param uPlane YUV数据的U分量
     * @param vPlane YUV数据的V分量
     * @param width YUV图片宽度
     * @param height YUV图片高度
     */
    fun feedTextureWithImageData(yPlane: ByteBuffer, uPlane: ByteBuffer, vPlane: ByteBuffer, width: Int, height: Int) {
        //根据YUV编码的特点，获得不同平面的基址
        textureYUV(yPlane, width, height, 0)
        textureYUV(uPlane, width / 2, height / 2, 1)
        textureYUV(vPlane, width / 2, height / 2, 2)
    }

    /**
     * 将图片数据绑定到纹理目标，适用于UV分量交叉存储的（NV12、NV21）
     * @param yPlane YUV数据的Y分量
     * @param uvPlane YUV数据的UV分量
     * @param width YUV图片宽度
     * @param height YUV图片高度
     */
    fun feedTextureWithImageData(yPlane: ByteBuffer, uvPlane: ByteBuffer, width: Int, height: Int) {
        //根据YUV编码的特点，获得不同平面的基址
        textureYUV(yPlane, width, height, 0)
        textureNV12(uvPlane, width / 2, height / 2, 1)
    }

    /**
     * 将图片数据绑定到纹理目标，适用于UV分量分开存储的（I420）
     * @param imageData YUV数据的Y/U/V分量
     * @param width YUV图片宽度
     * @param height YUV图片高度
     */
    private fun textureYUV(imageData: ByteBuffer, width: Int, height: Int, index: Int) {
        // 将纹理对象绑定到纹理目标
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mPlanarTextureHandles[index])
        // 设置放大和缩小时，纹理的过滤选项为：线性过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        // 设置纹理X,Y轴的纹理环绕选项为：边缘像素延伸
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        // 加载图像数据到纹理，GL_LUMINANCE指明了图像数据的像素格式为只有亮度，虽然第三个和第七个参数都使用了GL_LUMINANCE，
        // 但意义是不一样的，前者指明了纹理对象的颜色分量成分，后者指明了图像数据的像素格式
        // 获得纹理对象后，其每个像素的r,g,b,a值都为相同，为加载图像的像素亮度，在这里就是YUV某一平面的分量值
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0,
            GLES20.GL_LUMINANCE, width, height, 0,
            GLES20.GL_LUMINANCE,
            GLES20.GL_UNSIGNED_BYTE, imageData
        )
    }

    /**
     * 将图片数据绑定到纹理目标，适用于UV分量交叉存储的（NV12、NV21）
     * @param imageData YUV数据的UV分量
     * @param width YUV图片宽度
     * @param height YUV图片高度
     */
    private fun textureNV12(imageData: ByteBuffer, width: Int, height: Int, index: Int) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mPlanarTextureHandles[index])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, width, height, 0,
            GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, imageData
        )
    }

    /**
     * 创建两个缓冲区用于保存顶点 -> 屏幕顶点和纹理顶点
     * @param vert 屏幕顶点数据
     */
    fun createBuffers(vert: FloatArray) {
        mVertexBuffer = ByteBuffer.allocateDirect(vert.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(vert)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

        if (mCoordBuffer == null) {
            mCoordBuffer = ByteBuffer.allocateDirect(mCoordVertices.size * 4).run {
                // use the device hardware's native byte order
                order(ByteOrder.nativeOrder())

                // create a floating point buffer from the ByteBuffer
                asFloatBuffer().apply {
                    // add the coordinates to the FloatBuffer
                    put(mCoordVertices)
                    // set the buffer to read the first coordinate
                    position(0)
                }
            }
        }
        Log.d(TAG, "createBuffers vertice_buffer $mVertexBuffer  coord_buffer $mCoordBuffer")
    }

    /**
     * 检查GL操作是否有error
     * @param op 检查当前所做的操作
     */
    private fun checkGlError(op: String) {
        var error: Int = GLES20.glGetError()
        while (error != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "***** $op: glError $error")
            error = GLES20.glGetError()
        }
    }
}