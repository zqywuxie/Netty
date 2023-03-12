package com.wuxie.netty.Demo9.client.Handler;

import com.wuxie.netty.Demo9.protocol.Response.LogoutResponsePacket;
import com.wuxie.netty.Demo9.utils.SessionUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author wuxie
 * @date 2023/3/12 9:16
 * @description 该文件的描述 todo
 */
public class LogoutResponseHandler extends SimpleChannelInboundHandler<LogoutResponsePacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LogoutResponsePacket msg) throws Exception {
        SessionUtil.unBindSession(ctx.channel());
    }
}
