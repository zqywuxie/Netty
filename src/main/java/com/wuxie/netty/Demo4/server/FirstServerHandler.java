package com.wuxie.netty.Demo4.server;

import com.wuxie.netty.Demo4.protocol.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.wuxie.netty.Demo4.protocol.command.LOGIN_REQUEST;
import static com.wuxie.netty.Demo4.protocol.command.MESSAGE_REQUEST;

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


        ByteBuf requestByteBuf = (ByteBuf) msg;
        /**
         * 解码
         */

        Packet packet = PacketCodeC.getInstance().decode(requestByteBuf);

        Byte command = packet.getCommand();
        /**
         * 判断是否为请求数据包
         */
        if (LOGIN_REQUEST.equals(command)) {
            LoginRequestPacket loginRequestPacket = (LoginRequestPacket) packet;

            LoginResponsePacket loginResponsePacket = new LoginResponsePacket();
            if (valid(loginRequestPacket)) {
                System.out.println(new Date()+": 用户鉴权成功");
                loginResponsePacket.setIsSuccess(true);
            } else {
                System.out.println(new Date()+": 用户鉴权失败");
                loginResponsePacket.setReason("密码错误");
                loginResponsePacket.setIsSuccess(false);
            }

            ByteBuf responseByteBuf = PacketCodeC.getInstance().encode(loginResponsePacket);

            ctx.channel().writeAndFlush(responseByteBuf);
        } else if (MESSAGE_REQUEST.equals(command)) {
            MessageRequestPacket messageRequestPacket = (MessageRequestPacket) packet;
            System.out.println(new Date() + ": 收到客户端消息 :" + messageRequestPacket.getMessage());

            MessageResponsePacket messageResponsePacket = new MessageResponsePacket();
            messageResponsePacket.setMessage("服务端回复【" + messageRequestPacket.getMessage() + "】");

            ByteBuf responseByteBuf = PacketCodeC.getInstance().encode(messageResponsePacket);

            ctx.channel().writeAndFlush(responseByteBuf);

        }

        requestByteBuf.release();
    }

    /**
     * 鉴权逻辑
     */
    private boolean valid(LoginRequestPacket loginRequestPacket) {
        return true;
    }
}
