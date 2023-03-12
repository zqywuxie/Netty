package com.wuxie.netty.Demo9.protocol.Request;

import com.wuxie.netty.Demo9.protocol.Packet;
import lombok.Data;

import static com.wuxie.netty.Demo9.protocol.command.command.LOGOUT_REQUEST;

/**
 * @author wuxie
 * @date 2023/3/12 9:16
 * @description 该文件的描述 todo
 */
@Data
public class LogoutRequestPacket extends Packet {

    @Override
    public Byte getCommand() {
        return LOGOUT_REQUEST;
    }
}
