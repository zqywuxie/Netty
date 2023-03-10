package com.wuxie.netty.Demo6.client.Handler;

import com.wuxie.netty.Demo6.protocol.Request.MessageRequestPacket;
import com.wuxie.netty.Demo6.protocol.Response.MessageResponsePacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Date;

/**
 * @author wuxie
 * @date 2023/3/10 19:41
 * @description 该文件的描述 todo
 */
public class MessageResponseHandler extends SimpleChannelInboundHandler<MessageRequestPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageRequestPacket messageRequestPacket) throws Exception {


        System.out.println(new Date() + ": 收到客户端消息 :" + messageRequestPacket.getMessage());

        MessageResponsePacket messageResponsePacket = new MessageResponsePacket();
        messageResponsePacket.setMessage("服务端回复【" + messageRequestPacket.getMessage() + "】");

        ctx.channel().writeAndFlush(messageResponsePacket);
    }
}
