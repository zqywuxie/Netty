package com.wuxie.netty.Demo8.server.Handler;


import com.wuxie.netty.Demo8.entity.Session;
import com.wuxie.netty.Demo8.protocol.Request.MessageRequestPacket;
import com.wuxie.netty.Demo8.protocol.Response.MessageResponsePacket;
import com.wuxie.netty.Demo8.utils.SessionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Date;

/**
 * @author wuxie
 * @date 2023/3/10 19:41
 * @description 该文件的描述 todo
 */
public class MessageRequestHandler extends SimpleChannelInboundHandler<MessageRequestPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageRequestPacket messageRequestPacket) throws Exception {


        //1.拿到session
        Session login = SessionUtil.getLogin(ctx.channel());

        // 2. 通过发起方的信息构造发送的消息
        MessageResponsePacket messageResponsePacket = new MessageResponsePacket();
        messageResponsePacket.setFromUserId(login.getUserId());
        messageResponsePacket.setFromUserName(login.getUsername());
        messageResponsePacket.setMessage(messageRequestPacket.getMessage());

        //3.拿到消息方的channel

        Channel toUserChannel =SessionUtil.getChannel(messageRequestPacket.getToUserId());

        //4.消息发送给接收方

        if (toUserChannel != null  && SessionUtil.hasLogin(toUserChannel))  {

            toUserChannel.writeAndFlush(messageResponsePacket);
        } else {

            System.err.println("[" + messageRequestPacket.getToUserId() +"] 不在线,发送失败");
        }

    }
}
