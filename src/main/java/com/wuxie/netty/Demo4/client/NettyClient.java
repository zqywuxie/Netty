package com.wuxie.netty.Demo4.client;

import com.wuxie.netty.Demo4.protocol.MessageRequestPacket;
import com.wuxie.netty.Demo4.protocol.MessageResponsePacket;
import com.wuxie.netty.Demo4.protocol.PacketCodeC;
import com.wuxie.netty.Demo4.utils.LoginUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * @author wuxie
 * @date 2023/3/9 16:37
 * @description 该文件的描述 todo
 */
public class NettyClient {
    private static final int MAX_RETRY = 5;

    public static void main(String[] args) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();

        bootstrap
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new FirstClientHandler());
                    }
                });

        connect(bootstrap, "127.0.0.1", 8000, MAX_RETRY);
    }

    //    private static void connect(Bootstrap bootstrap, String host, int port) {
//        bootstrap.connect(host, port).addListener(future -> {
//            if (future.isSuccess()) {
//                System.out.println("连接成功!");
//            } else {
//                System.err.println("连接失败，开始重连");
//                connect(bootstrap, host, port);
//            }
//        });
//    }
    private static void connect(Bootstrap bootstrap, String host, int port, int retry) {
        bootstrap.connect(host, port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("连接成功!");

                Channel channel = ((ChannelFuture) future).channel();
                startConsoleThread(channel);
            } else if (retry == 0) {
                System.err.println("重试次数已用完，放弃连接！");
            } else {
                // 第几次重连
                int order = (MAX_RETRY - retry) + 1;
                // 本次重连的间隔
                int delay = 1 << order;
                System.err.println(new Date() + ": 连接失败，第" + order + "次重连……");
                bootstrap.config().group().schedule(() -> connect(bootstrap, host, port, retry - 1), delay, TimeUnit.SECONDS);
            }
        });
    }

    public static void startConsoleThread(Channel channel) {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                if (LoginUtil.hasLogin(channel)) {
                    System.out.println("======请输入消息到服务端");
                    Scanner scanner = new Scanner(System.in);
                    String message = scanner.nextLine();

                    MessageRequestPacket messageRequestPacket = new MessageRequestPacket();
                    messageRequestPacket.setMessage(message);
                    ByteBuf byteBuf = PacketCodeC.getInstance().encode(messageRequestPacket);
                    channel.writeAndFlush(byteBuf);
                }
            }
        }).start();

    }
}
