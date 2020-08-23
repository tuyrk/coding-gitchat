package com.tuyrk;

import redis.clients.jedis.Jedis;

public class JedisUtils {
    // Redis 操作客户端
    private static Jedis JEDIS = new Jedis("127.0.0.1", 6379);

    private JedisUtils() {
    }

    public static Jedis getJedis() {
        return JEDIS;
    }
}
