package com.wuxie.netty.Demo6.protocol;

import com.wuxie.netty.Demo6.protocol.Request.LoginRequestPacket;
import com.wuxie.netty.Demo6.protocol.Request.MessageRequestPacket;
import com.wuxie.netty.Demo6.protocol.Response.LoginResponsePacket;
import com.wuxie.netty.Demo6.protocol.Response.MessageResponsePacket;
import com.wuxie.netty.Demo6.serialize.Serializer;
import com.wuxie.netty.Demo6.serialize.impl.JSONSerializer;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

import static com.wuxie.netty.Demo4.protocol.command.*;


/**
 * @author wuxie
 * @date 2023/3/9 21:14
 * @description 该文件的描述 todo
 */
public class PacketCodeC {

    // 魔数
    public static final int MAGIC_NUMBER = 0x12345678;
    private static final Map<Byte,Class<? extends Packet>> packetTypeMap;
    private static final Map<Byte, Serializer>serializerMap;

    private static PacketCodeC INSTANCE;

    static {
        packetTypeMap = new HashMap<>();
        packetTypeMap.put(LOGIN_REQUEST, LoginRequestPacket.class);
        packetTypeMap.put(LOGIN_RESPONSE, LoginResponsePacket.class);
        packetTypeMap.put(MESSAGE_REQUEST, MessageRequestPacket.class);
        packetTypeMap.put(MESSAGE_RESPONSE, MessageResponsePacket.class);
        serializerMap = new HashMap<>();
        Serializer jsonSerializer = new JSONSerializer();
        serializerMap.put(jsonSerializer.getSerializerAlgorithm(),jsonSerializer);
    }
    public static synchronized PacketCodeC getInstance(){
        if (INSTANCE==null){
            INSTANCE = new PacketCodeC();
        }
        return INSTANCE;
    }



    /**
     * 编码
     */

    public ByteBuf encode(ByteBuf byteBuf,Packet packet){

        //1.序列化对象

        byte[] serialize = Serializer.DEFAULT.serialize(packet);

        //2.进行编码

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
