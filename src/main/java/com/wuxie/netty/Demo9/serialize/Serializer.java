package com.wuxie.netty.Demo9.serialize;

import com.wuxie.netty.Demo9.serialize.impl.JSONSerializer;

public interface Serializer {

    /**
     * json 序列化
     */

    Serializer DEFAULT = new JSONSerializer();

    /**
     * 获得具体的序列化算法标识
     */
    byte getSerializerAlgorithm();
    
    /**
     * java 对象转换成二进制
     */
    byte[] serialize(Object object);

    /**
     * 二进制转换成 java 对象
     */
    <T> T deserialize(Class<T> clazz, byte[] bytes);
}