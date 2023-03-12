package com.wuxie.netty.Demo9.server.Handler;


import com.wuxie.netty.Demo9.entity.Session;
import com.wuxie.netty.Demo9.protocol.Request.LoginRequestPacket;
import com.wuxie.netty.Demo9.protocol.Response.LoginResponsePacket;
import com.wuxie.netty.Demo9.utils.IDUtil;
import com.wuxie.netty.Demo9.utils.SessionUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Date;
import java.util.UUID;

/**
 * @author wuxie
 * @date 2023/3/10 19:41
 * @description 该文件的描述 todo
 */

@ChannelHandler.Sharable

public class LoginRequestHandler extends SimpleChannelInboundHandler<LoginRequestPacket> {

    public static final LoginRequestHandler INSTANCE = new LoginRequestHandler();

    public LoginRequestHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestPacket loginRequestPacket) throws Exception {

        System.out.println(new Date() + ": 收到客户端登录请求....");

        LoginResponsePacket loginResponsePacket = new LoginResponsePacket();
        loginResponsePacket.setVersion(loginRequestPacket.getVersion());
        if (valid(loginRequestPacket)) {

            String userId = IDUtil.randomId();
            String username = loginRequestPacket.getUsername();
            loginResponsePacket.setUserId(userId);
            loginResponsePacket.setUsername(username);

            SessionUtil.bindSession(new Session(userId, username), ctx.channel());
            System.out.println(new Date() + ":【" + username + "】登录成功");
            loginResponsePacket.setIsSuccess(true);
        } else {
            System.out.println(new Date() + ": 用户登录失败");
            loginResponsePacket.setReason("密码错误");
            loginResponsePacket.setIsSuccess(false);
        }

        ctx.writeAndFlush(loginResponsePacket);
    }

    /**
     * 鉴权逻辑
     */
    private boolean valid(LoginRequestPacket loginRequestPacket) {
        return true;
    }
}
