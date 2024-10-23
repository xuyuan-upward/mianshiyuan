package com.xuyuan.mianshiyuan.manager;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.IntegerCodec;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

    @Slf4j
    @Service
    public class CounterManager implements doCounter{

        @Resource
        private RedissonClient redissonClient;

        /**
         * 增加并返回计数，默认统计一分钟内的计数结果
         *
         * @param key 缓存键
         * @return
         */
        public int incrAndGetCounter(String key) {
            return incrAndGetCounter(key, 1, TimeUnit.MINUTES);
        }

        /**
         * 增加并返回计数
         *
         * @param key          缓存键
         * @param timeInterval 时间间隔
         * @param timeUnit     时间间隔单位
         * @return
         */
        public int incrAndGetCounter(String key, int timeInterval, TimeUnit timeUnit) {
            int expirationTimeInSeconds;
            switch (timeUnit) {
                case SECONDS:
                    expirationTimeInSeconds = timeInterval;
                    break;
                case MINUTES:
                    expirationTimeInSeconds = timeInterval * 60;
                    break;
                case HOURS:
                    expirationTimeInSeconds = timeInterval * 60 * 60;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported TimeUnit. Use SECONDS, MINUTES, or HOURS.");
            }

            return incrAndGetCounter(key, timeInterval, timeUnit, expirationTimeInSeconds);
        }

        /**
         * 增加并返回计数
         *
         * @param loginUserId              用户的唯一标识符
         * @param timeInterval            时间间隔
         * @param timeUnit                时间间隔单位
         * @param expirationTimeInSeconds 计数器缓存过期时间
         * @return
         */
        public int incrAndGetCounter(String loginUserId, int timeInterval, TimeUnit timeUnit, int expirationTimeInSeconds) {
            if (StrUtil.isBlank(loginUserId)) {
                return 0;
            }
            // 根据时间粒度生成 redisKey  timeInterval实现每timeInterval个数后key都是不同的从而实现
            // 每个timeInterval时间间隔实现不同的计数
            long timeFactor;
            switch (timeUnit) {
                case SECONDS:
                    // Instant.now()：该方法获取当前的时间点，表示为 Instant 对象，精确到纳秒。
                    // getEpochSecond()获取当前时刻距离 Unix 时间纪元（1970年1月1日00:00:00 UTC）的秒数。
                    timeFactor = Instant.now().getEpochSecond() / timeInterval;
                    break;
                case MINUTES:
                    timeFactor = Instant.now().getEpochSecond() / 60 / timeInterval;
                    break;
                case HOURS:
                    timeFactor = Instant.now().getEpochSecond() / 3600 / timeInterval;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported TimeUnit. Use SECONDS, MINUTES, or HOURS.");
            }
            String redisKey = String.format("user:access:%s", loginUserId) + ":" + timeFactor;

            // Lua 脚本
            String luaScript =
                    "if redis.call('exists', KEYS[1]) == 1 then " +
                            "  return redis.call('incr', KEYS[1]); " +
                            "else " +
                            "  redis.call('set', KEYS[1], 1); " +
                            "  redis.call('expire', KEYS[1], ARGV[1]); " +
                            "  return 1; " +
                            "end";

            // 执行 Lua 脚本
            RScript script = redissonClient.getScript(IntegerCodec.INSTANCE);
            Long countObj = script.eval(
                    RScript.Mode.READ_WRITE,
                    luaScript,
                    RScript.ReturnType.INTEGER,
                    Collections.singletonList(redisKey), expirationTimeInSeconds);
            return countObj.intValue();
        }


        /**
         * 隔一段时间进行redis请求增加并返回计数
         *
         * @param loginUserId                     缓存键
         * @param timeInterval            时间间隔
         * @param timeUnit                时间间隔单位
         * @param expirationTimeInSeconds 计数器缓存过期时间
         * @return
         */
        public int incrAndGetCounter(String loginUserId, int timeInterval, TimeUnit timeUnit, int expirationTimeInSeconds,long count) {
            if (StrUtil.isBlank(loginUserId)) {
                return 0;
            }
            // 根据时间粒度生成 redisKey  timeInterval实现每timeInterval个数后key都是不同的从而实现
            // 每个timeInterval时间间隔实现不同的计数
            long timeFactor;
            switch (timeUnit) {
                case SECONDS:
                    // Instant.now()：该方法获取当前的时间点，表示为 Instant 对象，精确到纳秒。
                    // getEpochSecond()获取当前时刻距离 Unix 时间纪元（1970年1月1日00:00:00 UTC）的秒数。
                    timeFactor = Instant.now().getEpochSecond() / timeInterval;
                    break;
                case MINUTES:
                    timeFactor = Instant.now().getEpochSecond() / 60 / timeInterval;
                    break;
                case HOURS:
                    timeFactor = Instant.now().getEpochSecond() / 3600 / timeInterval;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported TimeUnit. Use SECONDS, MINUTES, or HOURS.");
            }

            String redisKey = String.format("user:access:%s", loginUserId) + ":" + timeFactor;


            // Lua 脚本
            String luaScript =
                    "if redis.call('exists', KEYS[1]) == 1 then " +
                            "    local currentValue = tonumber(redis.call('get', KEYS[1])) or 0; " +
                            "    local newValue = currentValue + tonumber(ARGV[2]); " +
                            // set命令会从新赋过期时间
                            "    redis.call('set', KEYS[1], newValue); " +
                             "   redis.call('expire', KEYS[1], ARGV[1]);                               "+
                            "    return newValue; " +
                            "else " +
                            "    redis.call('set', KEYS[1], 1); " +
                            "    redis.call('expire', KEYS[1], ARGV[1]); " +
                            "    return 1; " +
                            "end";

            // 执行 Lua 脚本
            RScript script = redissonClient.getScript(IntegerCodec.INSTANCE);
            Object countObj = script.eval(
                    RScript.Mode.READ_WRITE,
                    luaScript,
                    RScript.ReturnType.INTEGER,
                    Collections.singletonList(redisKey), expirationTimeInSeconds,count);
            return (int) countObj;
        }

        @Override
        public int doCounter(String userId, int timeInterval, TimeUnit timeUnit, int expirationTimeInSeconds) {
            return this.incrAndGetCounter(userId, timeInterval, timeUnit, expirationTimeInSeconds);
        }
    }
