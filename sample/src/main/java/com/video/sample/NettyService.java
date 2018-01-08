package com.video.sample;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * Created by Administrator on 2018/1/8 0008.
 */

public class NettyService {
    public NettyService() {
    }

    private void init(){
        Bootstrap b = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        b.group(group)
                .channel(NioDatagramChannel.class)
                .handler(new EchoSeverHandler());

        // 服务端监听在9999端口
        try {
            b.bind(9999).sync().channel().closeFuture().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
