package com.wuxie.netty.Demo9.server.Handler;

import com.wuxie.netty.Demo9.entity.Session;
import com.wuxie.netty.Demo9.protocol.Request.CreateGroupRequestPacket;
import com.wuxie.netty.Demo9.protocol.Response.CreateGroupResponsePacket;
import com.wuxie.netty.Demo9.utils.IDUtil;
import com.wuxie.netty.Demo9.utils.SessionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.DefaultChannelGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wuxie
 * @date 2023/3/12 8:56
 * @description 该文件的描述 todo
 */
@ChannelHandler.Sharable
public class CreateGroupRequestHandler extends SimpleChannelInboundHandler<CreateGroupRequestPacket> {

    public static final CreateGroupRequestHandler INSTANCE = new CreateGroupRequestHandler();

    private CreateGroupRequestHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CreateGroupRequestPacket createGroupRequestPacket) throws Exception {
        //1. 获得用户ids
        List<String> userIds = createGroupRequestPacket.getUserIds();
        Session login = SessionUtil.getLogin(ctx.channel());
        userIds.add(login.getUserId());
        //2. 创建用户名字集合
        ArrayList<String> userNames = new ArrayList<>();

        // 3. 创建channel 组
        DefaultChannelGroup channels = new DefaultChannelGroup(ctx.executor());
        for (String userId : userIds) {
            Channel channel = SessionUtil.getChannel(userId);
            if (channel != null) {
                channels.add(channel);
                userNames.add(SessionUtil.getLogin(channel).getUsername());
            }
        }

        // 4.创建响应包
        String groupId = IDUtil.randomId();
        CreateGroupResponsePacket createGroupResponsePacket = new CreateGroupResponsePacket();
        createGroupResponsePacket.setGroupId(groupId);
        createGroupResponsePacket.setSuccess(true);
        createGroupResponsePacket.setUserNames(userNames);

        // 5. 每个客户端发送消息
        channels.writeAndFlush(createGroupResponsePacket);

        System.out.println("群创建成功 id:" + groupId);
        System.out.println("成员有: " + userNames);


        SessionUtil.bindChannelGroup(groupId, channels);
    }
}
