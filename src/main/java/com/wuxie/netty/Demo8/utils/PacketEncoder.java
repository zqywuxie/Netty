package com.wuxie.netty.Demo8.utils;

import com.wuxie.netty.Demo8.protocol.Packet;
import com.wuxie.netty.Demo8.protocol.PacketCodeC;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author wuxie
 * @date 2023/3/10 19:34
 * @description 该文件的描述 todo
 */
public class PacketEncoder extends MessageToByteEncoder<Packet> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {
        PacketCodeC.getInstance().encode(out,msg);
    }
}
