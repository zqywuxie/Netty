package com.wuxie.netty.Demo8.client.Handler;


import com.wuxie.netty.Demo8.entity.Session;
import com.wuxie.netty.Demo8.protocol.Request.LoginRequestPacket;
import com.wuxie.netty.Demo8.protocol.Response.LoginResponsePacket;
import com.wuxie.netty.Demo8.utils.LoginUtil;
import com.wuxie.netty.Demo8.utils.SessionUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Date;
import java.util.UUID;

/**
 * @author wuxie
 * @date 2023/3/10 19:48
 * @description 该文件的描述 todo
 */
public class LoginResponseHandler extends SimpleChannelInboundHandler<LoginResponsePacket> {
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        super.channelActive(ctx);
//
//        LoginRequestPacket loginRequestPacket = new LoginRequestPacket();
//
//        String userId = UUID.randomUUID().toString();
//        System.out.println(new Date() +": 客户端开始登录");
//        loginRequestPacket.setUserId(userId);
////        SessionUtil
//        SessionUtil.bindSession(new Session(userId,loginRequestPacket.getUsername()),ctx.channel());
//        ctx.channel().writeAndFlush(loginRequestPacket);
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginResponsePacket loginResponsePacket) throws Exception {
        String username = loginResponsePacket.getUsername();
        String userId = loginResponsePacket.getUserId();
        if (loginResponsePacket.getIsSuccess()) {
            SessionUtil.bindSession(new Session(userId,username),ctx.channel());
            System.out.println(new Date() + ": 登录成功,用户ID"+userId);
        } else {
            System.out.println(new Date() + ": 客户端登录失败，原因:" + loginResponsePacket.getReason());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SessionUtil.unBindSession(ctx.channel());
        super.channelInactive(ctx);
    }
}
