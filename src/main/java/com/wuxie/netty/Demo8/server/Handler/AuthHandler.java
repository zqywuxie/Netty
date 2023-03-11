package com.wuxie.netty.Demo8.server.Handler;

import com.wuxie.netty.Demo8.utils.LoginUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author wuxie
 * @date 2023/3/11 18:41
 * @description 该文件的描述 todo
 */
public class AuthHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!LoginUtil.hasLogin(ctx.channel())){
            ctx.channel().close();
        } else {
            // 移除逻辑
            ctx.pipeline().remove(this);
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        if (LoginUtil.hasLogin(ctx.channel())) {
            System.out.println("当前连接登录校验完毕，无需再次验证，AuthHandler移除");
        } else {
            System.out.println("无登录验证，强制关闭连接");
        }
    }
}
