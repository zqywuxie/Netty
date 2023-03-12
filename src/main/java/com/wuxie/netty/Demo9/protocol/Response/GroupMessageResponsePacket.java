package com.wuxie.netty.Demo9.protocol.Response;

import com.wuxie.netty.Demo9.entity.Session;
import com.wuxie.netty.Demo9.protocol.Packet;
import lombok.Data;

import static com.wuxie.netty.Demo9.protocol.command.command.GROUP_MESSAGE_RESPONSE;


@Data
public class GroupMessageResponsePacket extends Packet {

    private String fromGroupId;

    private Session fromUser;

    private String message;

    @Override
    public Byte getCommand() {

        return GROUP_MESSAGE_RESPONSE;
    }
}
