package com.lynn.demo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局锁，包括锁的名称
 */
@Getter
@AllArgsConstructor
public class Lock {
    private String name;
    private String value;
}
