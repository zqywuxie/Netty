package com.wuxie.netty.Demo4.protocol;

import io.netty.util.AttributeKey;

/**
 * 使用attr方法判断用户是否登录
 */
public interface Attributes {
    AttributeKey<Boolean> LOGIN = AttributeKey.newInstance("login");
}