package com.wuxie.netty.Demo2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.EventExecutorGroup;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author wuxie
 * @date 2023/3/9 19:06
 * @description 该文件的描述 todo
 */
public class FirstServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf= (ByteBuf) msg;

        System.out.println(new Date()+"：服务端读取到数据 ->"+byteBuf.toString(StandardCharsets.UTF_8));
    }
}
