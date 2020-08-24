package com.tuyrk.util;

import redis.clients.jedis.Jedis;

public class JedisUtil {
    // Redis 操作客户端
    private static Jedis JEDIS = new Jedis("127.0.0.1", 6379);

    private JedisUtil() {
    }

    public static Jedis getJedis() {
        return JEDIS;
    }
}
