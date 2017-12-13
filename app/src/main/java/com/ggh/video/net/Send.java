package com.ggh.video.net;

/**
 * Created by ZQZN on 2017/12/12.
 */

public interface Send {

    /**
     * 添加数据到发送队列
     * @param frame
     */
    void addData(Frame frame);

    /**
     * 开始发送数据
     */
    void startSender();

    /**
     * 暂停发送数据
     */
    void stopSender();

    /**
     * 销毁发送器
     */
    void destroy();
}
