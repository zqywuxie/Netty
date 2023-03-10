package com.wuxie.netty.Demo5;

import com.wuxie.netty.Demo4.server.FirstServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * @author wuxie
 * @date 2023/3/7 21:00
 * @description 该文件的描述 todo
 */
public class NettyServer {
    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel channel) {
                        channel.pipeline().addLast(new FirstServerHandler());
                        channel.pipeline().addLast(new InBoundHandlerA());
                        channel.pipeline().addLast(new InBoundHandlerB());
                        channel.pipeline().addLast(new InBoundHandlerC());

                        // outBound，处理写数据的逻辑链
                        channel.pipeline().addLast(new OutBoundHandlerA());
                        channel.pipeline().addLast(new OutBoundHandlerB());
                        channel.pipeline().addLast(new OutBoundHandlerC());
                    }
                });
        bind(serverBootstrap, 8000);
    }

    private static void bind(ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("端口[" + port + "]绑定成功");
            } else {
                System.out.println("端口[" + port + "]绑定失败");
                bind(serverBootstrap, port + 1);
            }
        });
    }
}
class OutBoundHandlerA extends ChannelOutboundHandlerAdapter{

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("OutBoundHandlerA:"+msg);
        super.write(ctx, msg, promise);
    }
}
class OutBoundHandlerB extends ChannelOutboundHandlerAdapter{

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("OutBoundHandlerB:"+msg);
        super.write(ctx, msg, promise);
    }
}
class OutBoundHandlerC extends ChannelOutboundHandlerAdapter{

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("OutBoundHandlerC:"+msg);
        super.write(ctx, msg, promise);
    }
}
class InBoundHandlerA extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("InBoundHandlerA:" + msg);
        super.channelRead(ctx, msg);
    }
}

class InBoundHandlerB extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("InBoundHandlerB:" + msg);
        super.channelRead(ctx, msg);
    }
}

class InBoundHandlerC extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("InBoundHandlerC:" + msg);
        super.channelRead(ctx, msg);
    }
}