package com.tuyrk.optimize;

import com.tuyrk.util.JedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.List;

public class PipelineExample {
    public static void main(String[] args) {
        Jedis jedis = JedisUtil.getJedis();
        long beginTime = System.currentTimeMillis(); // 记录执行开始时间

        // 获取 Pipeline 对象
        Pipeline pipe = jedis.pipelined();
        // 设置多个 Redis 命令
        for (int i = 0; i < 100; i++) {
            pipe.set("key" + i, "val" + i);
            pipe.del("key" + i);
        }
        // 执行命令
        // pipe.sync();
        List<Object> result = pipe.syncAndReturnAll();


        long endTime = System.currentTimeMillis(); // 记录执行结束时间
        System.out.println("执行耗时：" + (endTime - beginTime) + "毫秒");
        System.out.println("result = " + result);
    }
}
