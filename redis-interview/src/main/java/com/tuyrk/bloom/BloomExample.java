package com.tuyrk.bloom;

import com.tuyrk.util.JedisUtil;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.Collections;

/**
 * Jedis 操作 Lua 脚本的方式来实现布隆过滤器
 */
public class BloomExample {
    private static final String USER_MAP = "user";
    private static final String USERLIST_MAP = "userlist";

    @Test
    public void notExists() {
        Jedis jedis = JedisUtil.getJedis();
        for (int i = 1; i < 10_001; i++) {
            bfAdd(jedis, USER_MAP, "user_" + i);
            boolean exists = bfExists(jedis, USER_MAP, "user_" + i);
            if (!exists) {
                System.out.println("未找到数据 i=" + i);
                break;
            }
        }
        System.out.println("执行完成");
    }

    @Test
    public void exists() {
        Jedis jedis = JedisUtil.getJedis();
        for (int i = 1; i < 10_001; i++) {
            bfAdd(jedis, USERLIST_MAP, "user_" + i);
            boolean exists = bfExists(jedis, USERLIST_MAP, "user_" + (i + 1));
            if (exists) {
                System.out.println("找到了" + i);
                break;
            }
        }
        System.out.println("执行完成");
    }


    // 添加元素
    public static boolean bfAdd(Jedis jedis, String key, String value) {
        String luaStr = "return redis.call('bf.add', KEYS[1], KEYS[2])";
        Object result = jedis.eval(luaStr, Arrays.asList(key, value), Collections.emptyList());
        return result.equals(1L);
    }

    // 查询元素是否存在
    public static boolean bfExists(Jedis jedis, String key, String value) {
        String luaStr = "return redis.call('bf.exists', KEYS[1], KEYS[2])";
        Object result = jedis.eval(luaStr, Arrays.asList(key, value), Collections.emptyList());
        return result.equals(1L);
    }
}
