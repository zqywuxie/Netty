package com.wuxie.netty.Demo4.protocol;

import lombok.Data;

import static com.wuxie.netty.Demo4.protocol.command.MESSAGE_REQUEST;

/**
 * @author wuxie
 * @date 2023/3/9 20:59
 * @description 该文件的描述 todo
 */
@Data
public class MessageRequestPacket extends Packet {

    private String message;



    @Override
    public Byte getCommand() {
        return MESSAGE_REQUEST;
    }
}
