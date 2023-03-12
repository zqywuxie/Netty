package com.wuxie.netty.Demo9.protocol.Request;

import com.wuxie.netty.Demo9.protocol.Packet;
import lombok.Data;

import static com.wuxie.netty.Demo9.protocol.command.command.LIST_GROUP_MEMBERS_REQUEST;

@Data
public class ListGroupMembersRequestPacket extends Packet {

    private String groupId;

    @Override
    public Byte getCommand() {

        return LIST_GROUP_MEMBERS_REQUEST;
    }
}
