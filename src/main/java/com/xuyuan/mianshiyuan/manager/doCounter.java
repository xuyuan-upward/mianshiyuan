package com.xuyuan.mianshiyuan.manager;

import java.util.concurrent.TimeUnit;

public interface doCounter {
    int doCounter(String userId, int timeInterval, TimeUnit timeUnit, int expirationTimeInSeconds);
}
