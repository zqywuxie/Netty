package com.wuxie.netty.Demo4.protocol;

/**
 * @author wuxie
 * @date 2023/3/9 20:58
 * @description 该文件的描述 todo
 */
public interface command {
    /**
     * 登录指令
     */
    Byte LOGIN_REQUEST = 1;

    /**
     * 登录响应指令
     */

    Byte LOGIN_RESPONSE = 2;

    /**
     * 消息发送
     */

    Byte MESSAGE_REQUEST = 3;

    /**
     * 消息响应指令
     */

    Byte MESSAGE_RESPONSE = 4;
}
