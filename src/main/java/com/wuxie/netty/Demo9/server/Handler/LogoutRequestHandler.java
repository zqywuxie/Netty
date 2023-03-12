package com.wuxie.netty.Demo9.server.Handler;

import com.wuxie.netty.Demo9.utils.SessionUtil;
import com.wuxie.netty.Demo9.protocol.Request.LogoutRequestPacket;
import com.wuxie.netty.Demo9.protocol.Response.LogoutResponsePacket;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author wuxie
 * @date 2023/3/12 9:16
 * @description 该文件的描述 todo
 */

@ChannelHandler.Sharable
public class LogoutRequestHandler extends SimpleChannelInboundHandler<LogoutRequestPacket> {

    public static final LogoutRequestHandler INSTANCE = new LogoutRequestHandler();

    public LogoutRequestHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LogoutRequestPacket logoutRequestPacket) throws Exception {
        String username = SessionUtil.getLogin(ctx.channel()).getUsername();
        SessionUtil.unBindSession(ctx.channel());
        LogoutResponsePacket logoutResponsePacket = new LogoutResponsePacket();
        logoutResponsePacket.setSuccess(true);
        System.out.println(username+" ：已退出系统");
        ctx.writeAndFlush(logoutResponsePacket);
    }
}
