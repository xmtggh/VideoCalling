package sample.databinding.com.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * Created by ZQZN on 2018/1/9.
 */

public class DelimiterClientTime {
    private static final int PORT = 8080;
    private static final String IP = "127.0.0.1";

    public static void main(String[] args) throws InterruptedException {
        new DelimiterClientTime().connect(PORT, IP);
    }

    public void connect(int port, String host) throws InterruptedException {
//创建时间顺循环线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            //socket通道管道加入行解码器
                            ByteBuf delimiter = Unpooled.copiedBuffer("#".getBytes());
                            //传输遍历的数据长度和界定符“#_”
                            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));
                            //socket通道管道加入字符串解码器
                            ch.pipeline().addLast(new StringDecoder());
                            //socket通道管道加入客户端发送数据的适配器
                            ch.pipeline().addLast(new DelimiterClientHandler());
                        }

                    });
                    //发起异步连接,连接指定的主机，指定主机ip和port
            ChannelFuture f = bootstrap.connect(host, port).sync();
                    //等待客户端链路关闭
            f.channel().closeFuture().sync();
        } finally {
            //释放线程组
            group.shutdownGracefully();
        }
    }
}

class DelimiterClientHandler extends ChannelHandlerAdapter {
    /*private int counter;
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (int i = 0; i < 100; i++) {
            ctx.writeAndFlush(Unpooled.copiedBuffer(("HI Netty" + "#_").getBytes()).toString());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(" this is " + (++counter) + "receive server message" + msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("读取完成");
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        System.out.println("客户端异常退出");
    }*/
}