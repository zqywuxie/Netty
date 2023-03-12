package com.wuxie.netty.Demo9.utils;

import com.wuxie.netty.Demo9.Attributes.Attributes;
import com.wuxie.netty.Demo9.entity.Session;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

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

    private static final Map<String, ChannelGroup> groupIdChannelGroupMap = new ConcurrentHashMap<>();


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
            Session login = getLogin(channel);
            userIdChannelMap.remove(login.getUserId());
            channel.attr(Attributes.SESSION).set(null);
            System.out.println(login.getUsername() + ": 退出登录");
            System.exit(1);
        }
    }


    /**
     * 另一层面判断是否登录 ,注意可能为null，更改原来的逻辑
     */

    public static boolean hasLogin (Channel channel){
        return  getLogin(channel)!=null;
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

    public static void bindChannelGroup(String groupId, ChannelGroup channelGroup) {
        groupIdChannelGroupMap.put(groupId, channelGroup);
    }

    public static ChannelGroup getChannelGroup(String groupId) {
        return groupIdChannelGroupMap.get(groupId);
    }
}
