package com.ggh.video.net.udp;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class NettyClient {
    private NettyReceiverHandler handler;

    private int localPort = 7778;//绑定本地端口
    private String targetIp = "127.0.0.1";//对方ip地址
    private int targetport = 7779; // 对方端口
    private NettyReceiverHandler.FrameResultedCallback frameResultedCallback;

    private NettyClient(Builder builder) {
        localPort = builder.localPort;
        targetIp = builder.targetIp;
        targetport = builder.targetport;
        frameResultedCallback =builder.frameResultedCallback;
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        handler = new NettyReceiverHandler();
        if (frameResultedCallback!=null){
            handler.setOnFrameCallback(frameResultedCallback);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bootstrap b = new Bootstrap();
                EventLoopGroup group = new NioEventLoopGroup();
                try {
                    b.group(group)
                            .channel(NioDatagramChannel.class)
                            .option(ChannelOption.SO_BROADCAST, true)
                            .option(ChannelOption.SO_RCVBUF, 1024 * 1024)//接收区2m缓存
                            .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535))//加上这个，里面是最大接收、发送的长度
                            .handler(handler);

                    b.bind(localPort).sync().channel().closeFuture().await();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    group.shutdownGracefully();
                }
            }
        }).start();
    }


    /**
     * 发送数据
     *
     * @param data
     */
    public void sendData(Object data, String msgType) {
        handler.sendData(targetIp, targetport, data, msgType);
    }


    public static final class Builder {
        private int localPort;
        private String targetIp;
        private int targetport;
        private NettyReceiverHandler.FrameResultedCallback frameResultedCallback;

        public Builder() {
        }

        public Builder localPort(int val) {
            localPort = val;
            return this;
        }

        public Builder targetIp(String val) {
            targetIp = val;
            return this;
        }

        public Builder targetport(int val) {
            targetport = val;
            return this;
        }

        public Builder frameResultedCallback(NettyReceiverHandler.FrameResultedCallback val) {
            frameResultedCallback = val;
            return this;
        }

        public NettyClient build() {
            return new NettyClient(this);
        }
    }
}
