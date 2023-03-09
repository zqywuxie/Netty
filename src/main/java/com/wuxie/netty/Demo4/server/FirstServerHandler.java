package com.wuxie.netty.Demo4.server;

import com.wuxie.netty.Demo4.protocol.LoginRequestPacket;
import com.wuxie.netty.Demo4.protocol.LoginResponsePacket;
import com.wuxie.netty.Demo4.protocol.Packet;
import com.wuxie.netty.Demo4.protocol.PacketCodeC;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.wuxie.netty.Demo4.protocol.command.LOGIN_REQUEST;

/**
 * @author wuxie
 * @date 2023/3/9 19:06
 * @description 该文件的描述 todo
 */
public class FirstServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Channel channel = ctx.channel();
        if (channel.isActive()) {
            ctx.close();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


        ByteBuf requestByteBuf= (ByteBuf) msg;
        /**
         * 解码
         */

        Packet packet = PacketCodeC.getInstance().decode(requestByteBuf);

        /**
         * 判断是否为请求数据包
         */
        if (LOGIN_REQUEST.equals(packet.getCommand())) {
            LoginRequestPacket loginRequestPacket = (LoginRequestPacket) packet;

            LoginResponsePacket loginResponsePacket = new LoginResponsePacket();
            if (valid(loginRequestPacket)){
                System.out.println("鉴权成功");
                loginResponsePacket.setIsSuccess(true);
            } else {
                System.out.println("鉴权失败");
                loginResponsePacket.setReason("密码错误");
                loginResponsePacket.setIsSuccess(false);
            }

            ByteBuf responseByteBuf = PacketCodeC.getInstance().encode(loginResponsePacket);

            ctx.channel().writeAndFlush(responseByteBuf);
        }

    }

    /**
     * 鉴权逻辑
     */
    private boolean valid(LoginRequestPacket loginRequestPacket){
        return true;
    }
}
