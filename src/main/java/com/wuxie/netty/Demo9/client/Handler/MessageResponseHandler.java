package com.wuxie.netty.Demo9.client.Handler;

import com.wuxie.netty.Demo9.protocol.Response.MessageResponsePacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author wuxie
 * @date 2023/3/10 19:41
 * @description 该文件的描述 todo
 */
public class MessageResponseHandler extends SimpleChannelInboundHandler<MessageResponsePacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageResponsePacket messageResponsePacket) throws Exception {


        String fromUserId = messageResponsePacket.getFromUserId();
        String fromUserName = messageResponsePacket.getFromUserName();
        String message = messageResponsePacket.getMessage();

        System.out.println(fromUserId + ":" + fromUserName + "->" +message);
    }
}
