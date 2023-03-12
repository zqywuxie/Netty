package com.wuxie.netty.Demo9.client.console.impl;

import com.wuxie.netty.Demo9.client.console.ConsoleCommand;
import com.wuxie.netty.Demo9.protocol.Request.GroupMessageRequestPacket;
import io.netty.channel.Channel;

import java.util.Scanner;

/**
 * @author wuxie
 * @date 2023/3/12 11:58
 * @description 该文件的描述 todo
 */
public class SendToGroupConsoleCommand implements ConsoleCommand {
    @Override
    public void exec(Scanner scanner, Channel channel) {
        System.out.print("发送消息给某个某个群组：");

        String toGroupId = scanner.next();
        String message = scanner.next();
        channel.writeAndFlush(new GroupMessageRequestPacket(toGroupId, message));
    }
}
