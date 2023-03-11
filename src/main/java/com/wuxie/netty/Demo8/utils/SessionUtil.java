package com.wuxie.netty.Demo8.utils;

import com.wuxie.netty.Demo8.Attributes.Attributes;
import com.wuxie.netty.Demo8.entity.Session;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wuxie
 * @date 2023/3/11 19:44
 * @description 该文件的描述 todo
 */
public class SessionUtil {

    // map 存储映射

    public static final Map<String , Channel> userIdChannelMap = new ConcurrentHashMap<>();

    /**
     * 绑定
     */

    public static void bindSession (Session session , Channel channel){
        userIdChannelMap.put(session.getUserId(),channel);
        channel.attr(Attributes.SESSION).set(session);
    }

    /**
     * 去除绑定
     */

    public static void unBindSession(Channel channel){
        if (hasLogin(channel)){
            userIdChannelMap.remove(getLogin(channel).getUserId());
            channel.attr(Attributes.SESSION).set(null);
        }
    }


    /**
     * 另一层面判断是否登录
     */

    public static boolean hasLogin (Channel channel){

        return  channel.hasAttr(Attributes.SESSION);
    }

    /**
     * 获得登录数据
     */

    public static Session getLogin (Channel channel) {
        return channel.attr(Attributes.SESSION).get();
    }

    /**
     *  获得连接
     */
    public static Channel  getChannel (String userId) {
        return userIdChannelMap.get(userId);
    }
}
