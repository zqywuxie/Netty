package com.wuxie.netty.Demo9.protocol.Response;

import com.wuxie.netty.Demo9.protocol.Packet;
import lombok.Data;

import static com.wuxie.netty.Demo9.protocol.command.command.LOGIN_REQUEST;
import static com.wuxie.netty.Demo9.protocol.command.command.LOGOUT_RESPONSE;

/**
 * @author wuxie
 * @date 2023/3/12 9:16
 * @description 该文件的描述 todo
 */
@Data
public class LogoutResponsePacket extends Packet {

   private boolean success;

   private String reason;

    @Override
    public Byte getCommand() {
        return LOGOUT_RESPONSE;
    }
}
