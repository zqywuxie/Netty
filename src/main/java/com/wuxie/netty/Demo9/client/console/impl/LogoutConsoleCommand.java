package com.wuxie.netty.Demo9.client.console.impl;

import com.wuxie.netty.Demo9.client.console.ConsoleCommand;
import com.wuxie.netty.Demo9.protocol.Request.LogoutRequestPacket;
import io.netty.channel.Channel;

import java.util.Scanner;

/**
 * @author wuxie
 * @date 2023/3/12 9:06
 * @description 该文件的描述 todo
 */
public class LogoutConsoleCommand implements ConsoleCommand {

    @Override
    public void exec(Scanner scanner, Channel channel) {
        System.out.println("LogoutConsoleCommand");
        LogoutRequestPacket logoutRequestPacket = new LogoutRequestPacket();
        channel.writeAndFlush(logoutRequestPacket);
    }
}
