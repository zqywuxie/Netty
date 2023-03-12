package com.wuxie.netty.Demo9.protocol;

import lombok.Data;

/**
 * @author wuxie
 * @date 2023/3/9 20:52
 * @description 该文件的描述 todo
 */
@Data
public abstract class Packet {
    /**
     * 协议版本
     */
    private Byte version = 1;

    /**
     * 指令
     */
    public abstract Byte getCommand();
}
