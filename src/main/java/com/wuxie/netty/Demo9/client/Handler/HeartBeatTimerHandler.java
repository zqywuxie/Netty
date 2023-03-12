package com.wuxie.netty.Demo9.client.Handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.omg.CORBA.PRIVATE_MEMBER;

/**
 * @author wuxie
 * @date 2023/3/12 17:26
 * @description 该文件的描述 todo
 */
public class HeartBeatTimerHandler extends ChannelInboundHandlerAdapter {

    private static final int HEART_INTERVAL = 5;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        super.channelActive(ctx);
    }


}
