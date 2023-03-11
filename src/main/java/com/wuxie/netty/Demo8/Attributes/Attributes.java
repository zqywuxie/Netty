package com.wuxie.netty.Demo8.Attributes;

import com.wuxie.netty.Demo8.entity.Session;
import io.netty.util.AttributeKey;

/**
 * 使用attr方法判断用户是否登录
 */
public interface Attributes {
    AttributeKey<Boolean> LOGIN = AttributeKey.newInstance("login");
    AttributeKey<Session> SESSION = AttributeKey.newInstance("session");
}