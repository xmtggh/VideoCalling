package com.ggh.video.netty;

import android.util.Log;

import com.ggh.video.rtp.RtpPacket;

import java.net.DatagramSocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

/**
 * Created by Administrator on 2018/1/8 0008.
 */

public class EchoSeverHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    RtpPacket rtpPacket;

    public EchoSeverHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet)
            throws Exception {
        // 读取收到的数据
        byte[] buf = packet.copy().content().array();
        rtpPacket = new RtpPacket(buf,buf.length);
        rtpPacket.getSequenceNumber();
    }
}