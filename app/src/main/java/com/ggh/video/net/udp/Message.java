package com.ggh.video.net.udp;

public class Message {
    public static final String MES_TYPE_VIDEO = "MES_TYPE_VIDEO";
    public static final String MES_TYPE_AUDIO = "MES_TYPE_AUDIO";
    public static final String MES_TYPE_NOMAL = "MES_TYPE_NOMAL";
    private String msgtype;
    private String msgBody;
    private long timestamp;
    private byte[] frame;
    private int sort;



    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getFrame() {
        return frame;
    }

    public void setFrame(byte[] frame) {
        this.frame = frame;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }


}
