package com.ggh.video.net;

/**
 * Created by ZQZN on 2017/12/12.
 */

public abstract class Send {

    /**
     * 添加数据到发送队列
     * @param frame
     */
   protected abstract void addData(byte[] frame);

    /**
     * 开始发送数据
     */
    protected abstract void startSender();

    /**
     * 暂停发送数据
     */
    protected abstract void stopSender();

    /**
     * 销毁发送器
     */
    protected abstract void destroy();
}
