package com.wuxie.netty.Demo6.server.Handler;


import com.wuxie.netty.Demo6.protocol.Request.LoginRequestPacket;
import com.wuxie.netty.Demo6.protocol.Response.LoginResponsePacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Date;

/**
 * @author wuxie
 * @date 2023/3/10 19:41
 * @description 该文件的描述 todo
 */
public class LoginRequestHandler extends SimpleChannelInboundHandler<LoginRequestPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestPacket loginRequestPacket) throws Exception {

        System.out.println(new Date()+": 收到客户端登录请求....");

        LoginResponsePacket loginResponsePacket = new LoginResponsePacket();
        loginResponsePacket.setVersion(loginRequestPacket.getVersion());
        if (valid(loginRequestPacket)) {
            System.out.println(new Date()+": 用户登录成功");
            loginResponsePacket.setIsSuccess(true);
        } else {
            System.out.println(new Date()+": 用户登录失败");
            loginResponsePacket.setReason("密码错误");
            loginResponsePacket.setIsSuccess(false);
        }


        ctx.channel().writeAndFlush(loginResponsePacket);
    }

    /**
     * 鉴权逻辑
     */
    private boolean valid(LoginRequestPacket loginRequestPacket) {
        return true;
    }
}
