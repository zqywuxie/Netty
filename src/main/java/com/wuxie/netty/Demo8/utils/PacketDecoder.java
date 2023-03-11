package com.wuxie.netty.Demo8.utils;

import com.wuxie.netty.Demo8.protocol.PacketCodeC;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author wuxie
 * @date 2023/3/10 19:34
 * @description 该文件的描述 todo
 */
public class PacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        out.add(PacketCodeC.getInstance().decode(in));
    }
}
