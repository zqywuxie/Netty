package com.wuxie.netty.Demo8.protocol.Request;

import com.wuxie.netty.Demo8.protocol.Packet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.wuxie.netty.Demo4.protocol.command.MESSAGE_REQUEST;

/**
 * @author wuxie
 * @date 2023/3/9 20:59
 * @description 该文件的描述 todo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequestPacket extends Packet {

    private String message;

    private String toUserId;



    @Override
    public Byte getCommand() {
        return MESSAGE_REQUEST;
    }
}
