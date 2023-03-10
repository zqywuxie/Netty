package com.wuxie.netty.Demo6.protocol.Request;

import com.wuxie.netty.Demo6.protocol.Packet;
import lombok.Data;

import static com.wuxie.netty.Demo4.protocol.command.LOGIN_REQUEST;

/**
 * @author wuxie
 * @date 2023/3/9 20:59
 * @description 该文件的描述 todo
 */
@Data
public class LoginRequestPacket extends Packet {

    private String userId;

    private String username;

    private String password;



    @Override
    public Byte getCommand() {
        return LOGIN_REQUEST;
    }
}
