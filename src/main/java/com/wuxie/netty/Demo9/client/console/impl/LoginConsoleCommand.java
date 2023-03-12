package com.wuxie.netty.Demo9.client.console.impl;

import com.wuxie.netty.Demo9.client.console.ConsoleCommand;
import com.wuxie.netty.Demo9.protocol.Request.LoginRequestPacket;
import io.netty.channel.Channel;

import java.util.Scanner;

/**
 * @author wuxie
 * @date 2023/3/12 8:59
 * @description 该文件的描述 todo
 */
public class LoginConsoleCommand implements ConsoleCommand {
    @Override
    public void exec(Scanner scanner, Channel channel) {
        LoginRequestPacket loginRequestPacket = new LoginRequestPacket();

        System.out.println("输入用户名登录:");
        String username = scanner.nextLine();
        loginRequestPacket.setUsername(username);
        // 默认密码
        loginRequestPacket.setPassword("123");
        channel.writeAndFlush(loginRequestPacket);
        waitForLoginResponse();
    }

    public static void waitForLoginResponse(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }
    }
}
