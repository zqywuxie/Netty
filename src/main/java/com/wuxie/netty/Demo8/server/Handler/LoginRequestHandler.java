package com.wuxie.netty.Demo8.server.Handler;


import com.wuxie.netty.Demo8.entity.Session;
import com.wuxie.netty.Demo8.protocol.Request.LoginRequestPacket;
import com.wuxie.netty.Demo8.protocol.Response.LoginResponsePacket;
import com.wuxie.netty.Demo8.utils.SessionUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Date;
import java.util.UUID;

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

            String userId =UUID.randomUUID().toString();

            loginResponsePacket.setUserId(userId);
            loginResponsePacket.setUsername(loginRequestPacket.getUsername());

            SessionUtil.bindSession(new Session(userId, loginResponsePacket.getUsername()),ctx.channel());
            System.out.println(new Date()+": " + userId +"用户登录成功");
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
