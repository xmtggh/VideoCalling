package com.ggh.video.net.udp;

import android.os.Handler;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.apkfuns.logutils.LogUtils;


import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class NettyReceiverHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private int delayTime = 3000;
    public static String TAG = NettyReceiverHandler.class.getSimpleName();
    private ChannelHandlerContext channelHandlerContext;
    private ConcurrentHashMap<String, Integer> sendMessages = new ConcurrentHashMap<String, Integer>();
    private List<String> deleteLists = new ArrayList<>();
    private Handler handler = new Handler();

    //视频帧回掉
    private FrameResultedCallback frameCallback;


    public void setOnFrameCallback(FrameResultedCallback callback) {
        this.frameCallback = callback;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet)
            throws Exception {
        //服务器推送对方IP和PORT
        ByteBuf buf = (ByteBuf) packet.copy().content();
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String str = new String(req, "UTF-8");
        Message message = JSON.parseObject(str,Message.class);
        if (message.equals(Message.MES_TYPE_NOMAL)){
            LogUtils.d("接受普通消息" + message.getMsgBody());
        }else if (message.getMsgtype().equals(Message.MES_TYPE_VIDEO)){
            LogUtils.d("接受视频消息"+message.getFrame().length);
            if (frameCallback !=null){
                frameCallback.onVideoData(message.getFrame());
            }
        }else if (message.getMsgtype().equals(Message.MES_TYPE_AUDIO)){
            LogUtils.d("接收音频消息"+message.getFrame().length);
            if (frameCallback !=null){
                frameCallback.onAudioData(message.getFrame());
            }
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.channelHandlerContext = ctx;
        LogUtils.d("netty 启动");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
//        Channel channel = ctx.channel();
//        //……
//        if (channel.isActive()) ctx.close();
        Log.d("ggh", "同道异常关闭");
    }



    public void sendData(String ip, int port, Object data, String type) {
        Message message = null;
        if (data instanceof byte[]) {
            message = new Message();
            message.setFrame((byte[]) data);
            message.setMsgtype(type);
            message.setTimestamp(System.currentTimeMillis());
        }else if (data instanceof String){
            message = new Message();
            message.setMsgBody((String) data);
            message.setMsgtype(type);
            message.setTimestamp(System.currentTimeMillis());
        }


        if (channelHandlerContext != null) {
            channelHandlerContext.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer(JSON.toJSONString(message).getBytes()),
                    new InetSocketAddress(ip, port)));
        }

    }

    public interface FrameResultedCallback {
        void onVideoData(byte[] data);

        void onAudioData(byte[] data);
    }
}