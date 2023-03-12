package com.wuxie.netty.Demo9.protocol.Response;

import com.wuxie.netty.Demo9.entity.Session;
import com.wuxie.netty.Demo9.protocol.Packet;
import lombok.Data;

import java.util.List;

import static com.wuxie.netty.Demo9.protocol.command.command.LIST_GROUP_MEMBERS_RESPONSE;

@Data
public class ListGroupMembersResponsePacket extends Packet {

    private String groupId;

    private List<Session> sessionList;

    @Override
    public Byte getCommand() {

        return LIST_GROUP_MEMBERS_RESPONSE;
    }
}
