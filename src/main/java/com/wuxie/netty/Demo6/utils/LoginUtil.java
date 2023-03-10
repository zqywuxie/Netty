package com.wuxie.netty.Demo6.utils;

import com.wuxie.netty.Demo6.Attributes.Attributes;
import io.netty.channel.Channel;
import io.netty.util.Attribute;

/**
 * 登录工具类,添加登录标记和判断是否登录r
 */
public class LoginUtil {
    public static void markAsLogin(Channel channel) {
        channel.attr(Attributes.LOGIN).set(true);
    }

    public static boolean hasLogin(Channel channel) {
        Attribute<Boolean> loginAttr = channel.attr(Attributes.LOGIN);

        return loginAttr.get() != null;
    }
}