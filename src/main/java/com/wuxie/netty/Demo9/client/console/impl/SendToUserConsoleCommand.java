package com.wuxie.netty.Demo9.client.console.impl;

import com.wuxie.netty.Demo9.client.console.ConsoleCommand;
import com.wuxie.netty.Demo9.protocol.Request.MessageRequestPacket;

import io.netty.channel.Channel;

import java.util.Scanner;

/**
 * @author wuxie
 * @date 2023/3/12 9:03
 * @description 该文件的描述 todo
 */
public class SendToUserConsoleCommand implements ConsoleCommand {
    @Override
    public void exec(Scanner scanner, Channel channel) {
        System.out.println("====输入你要发送的对象ID");
        String toUserId = scanner.next();
        System.out.println("====输入你要发送的消息");
        String message = scanner.next();
        channel.writeAndFlush(new MessageRequestPacket(message,toUserId));
    }
}
