package com.wuxie.netty.Demo6.client.Handler;


import com.wuxie.netty.Demo6.protocol.Request.LoginRequestPacket;
import com.wuxie.netty.Demo6.protocol.Response.LoginResponsePacket;
import com.wuxie.netty.Demo6.utils.LoginUtil;
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
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println(new Date() + ": 客户端开始登录");

        LoginRequestPacket loginRequestPacket = new LoginRequestPacket();

        loginRequestPacket.setUserId(UUID.randomUUID().toString());
        loginRequestPacket.setUsername("wuxie");
        loginRequestPacket.setPassword("123");

        //使用到单例模式创建
        ctx.channel().writeAndFlush(loginRequestPacket);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginResponsePacket loginResponsePacket) throws Exception {

        if (loginResponsePacket.getIsSuccess()) {
            //登录标记
            LoginUtil.markAsLogin(ctx.channel());
            System.out.println(new Date() + ": 客户端登录成功");
        } else {
            System.out.println(new Date() + ": 客户端登录失败，原因:" + loginResponsePacket.getReason());
        }
    }
}
