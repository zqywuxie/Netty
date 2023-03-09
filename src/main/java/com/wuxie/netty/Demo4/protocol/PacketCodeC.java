package com.wuxie.netty.Demo4.protocol;

import com.wuxie.netty.Demo4.serialize.impl.JSONSerializer;
import com.wuxie.netty.Demo4.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.HashMap;
import java.util.Map;

import static com.wuxie.netty.Demo4.protocol.command.LOGIN_REQUEST;


/**
 * @author wuxie
 * @date 2023/3/9 21:14
 * @description 该文件的描述 todo
 */
public class PacketCodeC {

    // 魔数
    private static final int MAGIC_NUMBER = 0x12345678;
    private static final Map<Byte,Class<? extends Packet>> packetTypeMap;
    private static final Map<Byte, Serializer>serializerMap;

    static {
        packetTypeMap = new HashMap<>();
        packetTypeMap.put(LOGIN_REQUEST, LoginRequestPacket.class);
        serializerMap = new HashMap<>();
        Serializer jsonSerializer = new JSONSerializer();
        serializerMap.put(jsonSerializer.getSerializerAlgorithm(),jsonSerializer);

    }

    /**
     * 编码
     */

    public ByteBuf encode(Packet packet){
        //1.获得ByteBuf 对象
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.ioBuffer();

        //2.序列化对象

        byte[] serialize = Serializer.DEFAULT.serialize(packet);

        //3.进行编码

        //魔数
        byteBuf.writeInt(MAGIC_NUMBER);
        //版本
        byteBuf.writeByte(packet.getVersion());
        //序列化算法
        byteBuf.writeByte(Serializer.DEFAULT.getSerializerAlgorithm());
        //指令
        byteBuf.writeByte(packet.getCommand());
        // 数据长度
        byteBuf.writeInt(serialize.length) ;
        //数据
        byteBuf.writeBytes(serialize);
        return byteBuf;
    }

    /**
     * 解码
     */

    public Packet decode(ByteBuf byteBuf){

        //跳过魔数，实际应该取出来然后进行比较，此处省略
        byteBuf.skipBytes(4);

        //跳过版本号,同上
        byteBuf.skipBytes(1);

        byte serializerAlgorithm = byteBuf.readByte();

        byte command = byteBuf.readByte();

        int length = byteBuf.readInt();

        byte[] bytes = new byte[length];

        byteBuf.readBytes(bytes);

        Class<? extends Packet> requestType = getRequestType(command);
        Serializer serializer = getSerializer(serializerAlgorithm);
        if (requestType!=null&&serializer!=null){
            return serializer.deserialize(requestType, bytes);
        }
        return null;
    }

    private Serializer getSerializer(byte serializerAlgorithm) {
        return serializerMap.get(serializerAlgorithm);
    }

    private Class<? extends Packet> getRequestType(byte command) {
        return packetTypeMap.get(command);
    }
}
