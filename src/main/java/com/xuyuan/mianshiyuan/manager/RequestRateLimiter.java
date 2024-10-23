package com.xuyuan.mianshiyuan.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

//@Component
@Slf4j
public final class RequestRateLimiter implements doCounter{

    // 用于存储每个用户的访问次数计数器
    private ConcurrentHashMap<String, LongAdder> userRequestCounts = new ConcurrentHashMap<>();
    // 每个用户redis中一分钟内的总计数
    private ConcurrentHashMap<String, Long> userRequestCountsSum = new ConcurrentHashMap<>();

    // 统计的时间间隔（比如每分钟重置一次计数）
    private final int interval =  1;
    private  int timeInterval ;
    private  int expirationTimeInSeconds;
    private TimeUnit timeUnit;

    /**
     * 每次用户访问时调用此方法
     * @param userId 用户的唯一标识符
     */
    public void recordRequest(String userId, int timeInterval,TimeUnit timeUnit,int expirationTimeInSeconds) {
        // 获取或者初始化用户的访问计数器
        // Function<T, R>是Java 中的一个函数式接口，代表一个接受一个输入参数（类型为 T）并返回一个结果（类型为 R）的函数。
        // 它通常用于将某种类型的输入转换为另一种类型的输出。
        //  userId 不在 userRequestCounts 中，则创建一个新的 LongAdder 对象并将其关联到 userId，
        //  然后对该计数器进行自增
        this.timeInterval = timeInterval;
        this.expirationTimeInSeconds = expirationTimeInSeconds;
        this.timeUnit = timeUnit;
        userRequestCounts.computeIfAbsent(userId, adder -> new LongAdder()).increment();
    }

//    /**
//     * 获取用户的当前访问次数
//     * @param userId 用户的唯一标识符
//     * @return 用户的访问次数
//     */
//    public long getRequestCount(String userId) {
//        return userRequestCounts.getOrDefault(userId, new LongAdder()).sum();
//    }

    /**
     * 定期重置每个用户的访问计数器
     */
    private void startResetTask() {
        // 定时任务，每隔指定的时间间隔重置计数
        new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(interval);  // 等待指定的时间间隔
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                reportToRedis(); // 上报当前统计到 Redis
                // 重置每个用户的计数器,连同userRequestCounts的内部的全部清理，包括键值对
                userRequestCounts.clear();
                userRequestCountsSum.clear();
            }
        }).start();
    }
    @Resource
    private CounterManager counterManager;

    private void reportToRedis() {

        for (String userId : userRequestCounts.keySet()) {
            long count = userRequestCounts.get(userId).sum();
            // 将统计结果上报到 Redis  并每5s获取请求的次数
            long counter = counterManager.incrAndGetCounter(userId, timeInterval, timeUnit, expirationTimeInSeconds, count);
            log.info("userCounter:{},{}",userId,counter);
            // todo 会出现内存泄露问题 不是很好的方法
            userRequestCountsSum.put(userId,  counter);
        }

        log.info("reportToRedis");
    }
    // 获取对应5s后的userId值
    /**
     * 获取用户的当前访问次数
     * @param userId 用户的唯一标识符
     * @return 用户的访问次数
     */
    public int getRequestCount(String userId) {
        if (userRequestCountsSum == null) {
            return 0;
        }
        return Math.toIntExact(userRequestCountsSum.getOrDefault(userId, 0L));
    }

    @Override
    public int doCounter(String userId, int timeInterval, TimeUnit timeUnit, int expirationTimeInSeconds) {
        startResetTask();
        this.recordRequest(userId, timeInterval, timeUnit, expirationTimeInSeconds);
        return   this.getRequestCount(userId);
    }
}
