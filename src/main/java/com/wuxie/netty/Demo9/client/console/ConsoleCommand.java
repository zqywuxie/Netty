package com.wuxie.netty.Demo9.client.console;

import io.netty.channel.Channel;

import java.util.Scanner;

/**
 * @author wuxie
 * @date 2023/3/12 8:47
 * @description 该文件的描述 todo
 */
public interface ConsoleCommand {

    void exec (Scanner scanner, Channel channel);
}
