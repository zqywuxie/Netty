package com.wuxie.netty.Demo9.server;

import com.wuxie.netty.Demo9.codeC.PacketCodecHandler;
import com.wuxie.netty.Demo9.server.Handler.*;
import com.wuxie.netty.Demo9.codeC.Spliter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

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
                        channel.pipeline().addLast(new Spliter());
                        channel.pipeline().addLast(PacketCodecHandler.INSTANCE);
                        channel.pipeline().addLast(LoginRequestHandler.INSTANCE);
                        channel.pipeline().addLast(AuthHandler.INSTANCE);
                        channel.pipeline().addLast(IMServerHandler.INSTANCE);
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