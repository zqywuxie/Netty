package com.wuxie.netty.Demo8.protocol.Response;

import com.wuxie.netty.Demo8.protocol.Packet;
import lombok.Data;

import static com.wuxie.netty.Demo4.protocol.command.MESSAGE_RESPONSE;

/**
 * @author wuxie
 * @date 2023/3/9 20:59
 * @description 该文件的描述 todo
 */
@Data
public class MessageResponsePacket extends Packet {

    private String message;

    private String fromUserId;

    private String fromUserName;


    @Override
    public Byte getCommand() {
        return MESSAGE_RESPONSE;
    }
}
