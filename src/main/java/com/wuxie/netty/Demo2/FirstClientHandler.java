package com.wuxie.netty.Demo2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.EventExecutorGroup;

import java.nio.charset.Charset;

/**
 * @author wuxie
 * @date 2023/3/9 17:11
 * @description 该文件的描述 todo
 */
public class FirstClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf byteBuf = getByteBuf(ctx);
        ctx.channel().writeAndFlush(byteBuf);
    }

    private ByteBuf getByteBuf(ChannelHandlerContext ctx) {
        ByteBuf buffer = ctx.alloc().buffer();
        byte[] bytes = "你好，我们在学习netty".getBytes(Charset.forName("utf-8"));
        buffer.writeBytes(bytes);
        return buffer;
    }

}
