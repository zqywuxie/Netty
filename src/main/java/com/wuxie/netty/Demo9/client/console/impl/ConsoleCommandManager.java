package com.wuxie.netty.Demo9.client.console.impl;

import com.wuxie.netty.Demo9.client.console.ConsoleCommand;
import com.wuxie.netty.Demo9.entity.Session;
import com.wuxie.netty.Demo9.utils.SessionUtil;
import io.netty.channel.Channel;

;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @author wuxie
 * @date 2023/3/12 8:48
 * @description 该文件的描述 todo
 */
public class ConsoleCommandManager implements ConsoleCommand {

    private Map<String, ConsoleCommand> consoleCommandMap;

    public ConsoleCommandManager() {
        consoleCommandMap = new HashMap<>();
        consoleCommandMap.put("createGroup" , new CreateGroupConsoleCommand());
        consoleCommandMap.put("sendToUser" , new SendToUserConsoleCommand());
        consoleCommandMap.put("logout" , new LogoutConsoleCommand());
        consoleCommandMap.put("listMembers" , new ListGroupMembersConsoleCommand());
        consoleCommandMap.put("joinGroup" , new JoinGroupConsoleCommand());
        consoleCommandMap.put("quitGroup" , new QuitGroupConsoleCommand());
        consoleCommandMap.put("sendToGroup" , new SendToGroupConsoleCommand());
    }

    @Override
    public void exec(Scanner scanner, Channel channel) {

        if (!SessionUtil.hasLogin(channel)) {
            return;
        }

        System.out.println("输入指令:");
        //1.输出命令
        String command = scanner.next();

        ConsoleCommand consoleCommand = consoleCommandMap.get(command);

        if (consoleCommand != null) {
            consoleCommand.exec(scanner,channel);
        } else {
            System.out.println("无法识别[" + command + "]指令,请重新输入!");
        }
    }
}
