package com.wuxie.netty.Demo4.protocol;

import lombok.Data;

import static com.wuxie.netty.Demo4.protocol.command.LOGIN_REQUEST;

/**
 * @author wuxie
 * @date 2023/3/9 20:59
 * @description 该文件的描述 todo
 */
@Data
public class LoginRequestPacket extends Packet {

    private Integer userId;

    private String username;

    private String password;

    @Override
    public Byte getCommand() {
        return LOGIN_REQUEST;
    }
}
