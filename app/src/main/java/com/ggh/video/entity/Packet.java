package com.ggh.video.entity;

/**
 * Created by ZQZN on 2017/9/15.
 */

public class Packet {
    public byte[] buffer;
    public int packSize;
    public boolean islaskpack;
    public int sequence;
    public long time;

    public Packet() {
    }

    public Packet(byte[] buffer, int packSize, boolean islaskpack, int sequence, long time) {
        this.buffer = buffer;
        this.packSize = packSize;
        this.islaskpack = islaskpack;
        this.sequence = sequence;
        this.time = time;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public int getPackSize() {
        return packSize;
    }

    public void setPackSize(int packSize) {
        this.packSize = packSize;
    }

    public boolean islaskpack() {
        return islaskpack;
    }

    public void setIslaskpack(boolean islaskpack) {
        this.islaskpack = islaskpack;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
