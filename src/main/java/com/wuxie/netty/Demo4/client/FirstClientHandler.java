package com.wuxie.netty.Demo4.client;

import com.wuxie.netty.Demo4.protocol.LoginRequestPacket;
import com.wuxie.netty.Demo4.protocol.LoginResponsePacket;
import com.wuxie.netty.Demo4.protocol.Packet;
import com.wuxie.netty.Demo4.protocol.PacketCodeC;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.UUID;

import static com.wuxie.netty.Demo4.protocol.command.LOGIN_REQUEST;
import static com.wuxie.netty.Demo4.protocol.command.LOGIN_RESPONSE;

/**
 * @author wuxie
 * @date 2023/3/9 17:11
 * @description 该文件的描述 todo
 */
public class FirstClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(new Date()+": 客户端开始登录");

        LoginRequestPacket loginRequestPacket = new LoginRequestPacket();

        loginRequestPacket.setUserId(UUID.randomUUID().toString());
        loginRequestPacket.setUsername("wuxie");
        loginRequestPacket.setPassword("123");

        //使用到单例模式创建
        ByteBuf byteBuf = PacketCodeC.getInstance().encode(loginRequestPacket);
        ctx.channel().writeAndFlush(byteBuf);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;

        Packet packet = PacketCodeC.getInstance().decode(byteBuf);

        if(LOGIN_RESPONSE.equals(packet.getCommand())){
            LoginResponsePacket loginResponsePacket = (LoginResponsePacket) packet;

            if (loginResponsePacket.getIsSuccess()){
                System.out.println(new Date()+": 客户端登录成功");
            } else {
                System.out.println(new Date()+": 客户端登录失败，原因:"+loginResponsePacket.getReason());
            }
        }


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Channel channel = ctx.channel();
        if (channel.isActive()){
            ctx.close();
        }
    }

    private ByteBuf getByteBuf(ChannelHandlerContext ctx) {
        ByteBuf buffer = ctx.alloc().buffer();
        byte[] bytes = "你好，我们在学习netty".getBytes(Charset.forName("utf-8"));
        buffer.writeBytes(bytes);
        return buffer;
    }

}
