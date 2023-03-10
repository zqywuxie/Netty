package com.wuxie.netty.Demo6.utils;

import com.wuxie.netty.Demo6.protocol.PacketCodeC;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author wuxie
 * @date 2023/3/10 21:21
 * @description 该文件的描述 todo
 */
public class Spliter extends LengthFieldBasedFrameDecoder {

    private static final int LENGTH_FIELD_OFFSET = 7;

    public static final int LENGTH_FIELD_LENGTH = 4;
    public Spliter (){
        super(Integer.MAX_VALUE,LENGTH_FIELD_OFFSET,LENGTH_FIELD_LENGTH);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        //in 缓存区的开头，获得读指针 getint读取一个整数(魔数)
        if (in.getInt(in.readerIndex()) != PacketCodeC.MAGIC_NUMBER){
            ctx.channel().close();
            return null;
        }

        return super.decode(ctx, in);
    }
}
