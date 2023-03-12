package com.wuxie.netty.Demo9.codeC;

import com.wuxie.netty.Demo9.protocol.Packet;
import com.wuxie.netty.Demo9.protocol.PacketCodeC;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.internal.ObjectUtil;

import java.util.List;

/**
 * @author wuxie
 * @date 2023/3/10 19:34
 * @description 该文件的描述 todo
 */
public class PacketEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
        PacketCodeC.getInstance().encode(out,packet);
    }
}
