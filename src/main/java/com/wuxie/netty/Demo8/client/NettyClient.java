package com.wuxie.netty.Demo8.client;

import com.wuxie.netty.Demo8.client.Handler.LoginResponseHandler;
import com.wuxie.netty.Demo8.client.Handler.MessageResponseHandler;
import com.wuxie.netty.Demo8.protocol.Request.LoginRequestPacket;
import com.wuxie.netty.Demo8.protocol.Request.MessageRequestPacket;
import com.wuxie.netty.Demo8.utils.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Date;
import java.util.Scanner;
import java.util.UUID;
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
                .handler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) {
                        nioSocketChannel.pipeline().addLast(new Spliter());
                        nioSocketChannel.pipeline().addLast(new PacketDecoder());
                        nioSocketChannel.pipeline().addLast(new LoginResponseHandler());
                        nioSocketChannel.pipeline().addLast(new MessageResponseHandler());
                        nioSocketChannel.pipeline().addLast(new PacketEncoder());
                    }
                });

        connect(bootstrap, "127.0.0.1", 8000, MAX_RETRY);
    }

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
        Scanner scanner = new Scanner(System.in);
        LoginRequestPacket loginRequestPacket = new LoginRequestPacket();
        new Thread(() -> {
            while (!Thread.interrupted()) {
                    if (!SessionUtil.hasLogin(channel)) {
                        System.out.println("输入用户名登录:");
                        String username = scanner.nextLine();
                        loginRequestPacket.setUsername(username);
                        // 默认密码
                        loginRequestPacket.setPassword("123");
                        channel.writeAndFlush(loginRequestPacket);
                        waitForLoginResponse();
                    } else {

                        System.out.println("====输入你要发送的对象ID");
                        String toUserId = scanner.next();
                        System.out.println("====输入你要发送的消息");
                        String message = scanner.next();
                        channel.writeAndFlush(new MessageRequestPacket(message,toUserId));
                    }
                }
        }).start();

    }

    public static void waitForLoginResponse(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }
    }
}
