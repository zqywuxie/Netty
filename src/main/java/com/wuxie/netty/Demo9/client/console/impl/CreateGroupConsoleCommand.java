package com.wuxie.netty.Demo9.client.console.impl;

import com.wuxie.netty.Demo9.client.console.ConsoleCommand;
import com.wuxie.netty.Demo9.protocol.Request.CreateGroupRequestPacket;
import io.netty.channel.Channel;


import java.util.Arrays;
import java.util.Scanner;

/**
 * @author wuxie
 * @date 2023/3/12 8:53
 * @description 该文件的描述 todo
 */
public class CreateGroupConsoleCommand implements ConsoleCommand {

    public static final String USER_ID_SPLITER = ",";

    @Override
    public void exec(Scanner scanner, Channel channel) {
        CreateGroupRequestPacket createGroupRequestPacket = new CreateGroupRequestPacket();

        System.out.println("【拉入群聊】输入用户id，id之间英文逗号隔开");
        String userIds = scanner.next();
        createGroupRequestPacket.setUserIds(Arrays.asList(userIds.split(USER_ID_SPLITER)));
        channel.writeAndFlush(createGroupRequestPacket);

    }
}
