package com.ggh.video.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

/**
 * Created by Administrator on 2018/1/8 0008.
 */

public class EchoSeverHandler extends SimpleChannelInboundHandler<DatagramPacket>
{
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet)
            throws Exception
    {
        // 读取收到的数据
        ByteBuf buf = (ByteBuf) packet.copy().content();
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, CharsetUtil.UTF_8);
        System.out.println("【NOTE】>>>>>> 收到客户端的数据："+body);

        // 回复一条信息给客户端
        ctx.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer("Hello，我是Server，我的时间戳是"+System.currentTimeMillis()
                        , CharsetUtil.UTF_8)
                , packet.sender())).sync();
    }
}