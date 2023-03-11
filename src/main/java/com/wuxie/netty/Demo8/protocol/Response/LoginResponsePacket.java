package com.wuxie.netty.Demo8.protocol.Response;

import com.wuxie.netty.Demo8.protocol.Packet;
import lombok.Data;

import static com.wuxie.netty.Demo4.protocol.command.LOGIN_RESPONSE;

/**
 * @author wuxie
 * @date 2023/3/9 23:23
 * @description 该文件的描述 todo
 */
@Data
public class LoginResponsePacket extends Packet {

    private String userId;

    private String username;
    private String reason;

    private Boolean isSuccess;
    @Override
    public Byte getCommand() {
        return LOGIN_RESPONSE;
    }
}
