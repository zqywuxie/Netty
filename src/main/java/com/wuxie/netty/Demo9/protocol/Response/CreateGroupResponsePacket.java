package com.wuxie.netty.Demo9.protocol.Response;

import com.wuxie.netty.Demo9.protocol.Packet;
import lombok.Data;

import java.util.List;

import static com.wuxie.netty.Demo9.protocol.command.command.CREATE_GROUP_RESPONSE;

/**
 * @author wuxie
 * @date 2023/3/12 8:56
 * @description 该文件的描述 todo
 */
@Data
public class CreateGroupResponsePacket extends Packet {


    private List<String>userNames;
    boolean isSuccess;

    private String groupId;

    @Override
    public Byte getCommand() {
        return CREATE_GROUP_RESPONSE;
    }
}
