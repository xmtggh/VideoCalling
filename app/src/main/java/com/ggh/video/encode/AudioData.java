package com.ggh.video.encode;

public class AudioData
{
    int size;
    short[] realData;
    byte[] receiverdata;

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    public short[] getRealData()
    {
        return realData;
    }

    public void setRealData(short[] realData)
    {
        this.realData = realData;
    }

    public byte[] getReceiverdata() {
        return receiverdata;
    }

    public void setReceiverdata(byte[] receiverdata) {
        this.receiverdata = receiverdata;
    }
}
