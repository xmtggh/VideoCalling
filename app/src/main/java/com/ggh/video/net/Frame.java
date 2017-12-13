package com.ggh.video.net;

/**
 * Created by ZQZN on 2017/12/12.
 */

public class Frame {
    private byte[] data;
    private int size;

    public Frame() {
    }

    public Frame(byte[] data, int size) {
        this.data = data;
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
