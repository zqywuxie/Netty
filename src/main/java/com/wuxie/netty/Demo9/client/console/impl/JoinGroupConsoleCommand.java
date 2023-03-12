package com.wuxie.netty.Demo9.client.console.impl;

import com.wuxie.netty.Demo9.client.console.ConsoleCommand;
import com.wuxie.netty.Demo9.protocol.Request.JoinGroupRequestPacket;
import io.netty.channel.Channel;

import java.util.Scanner;

/**
 * @author wuxie
 * @date 2023/3/12 11:58
 * @description 该文件的描述 todo
 */
public class JoinGroupConsoleCommand implements ConsoleCommand {
    @Override
    public void exec(Scanner scanner, Channel channel) {
        JoinGroupRequestPacket joinGroupRequestPacket = new JoinGroupRequestPacket();
        System.out.println("输入加入的群聊id:");
        String groupId = scanner.next();
        joinGroupRequestPacket.setGroupId(groupId);
        channel.writeAndFlush(joinGroupRequestPacket);
    }
}
