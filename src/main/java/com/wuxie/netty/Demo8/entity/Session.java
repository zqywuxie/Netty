package com.wuxie.netty.Demo8.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wuxie
 * @date 2023/3/11 19:42
 * @description 该文件的描述 todo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Session {

    // 用户唯一标识
    private String userId;

    private String username;
}
