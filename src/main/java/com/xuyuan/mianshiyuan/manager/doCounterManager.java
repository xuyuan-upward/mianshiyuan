package com.xuyuan.mianshiyuan.manager;


import java.util.concurrent.TimeUnit;
public class doCounterManager {
    private doCounter doCounter;
    public doCounterManager(doCounter doCounter){
        this.doCounter = doCounter;
    }
    public int doCounter(String userId, int timeInterval, TimeUnit timeUnit, int expirationTimeInSeconds){
       return doCounter.doCounter(userId,timeInterval, timeUnit, expirationTimeInSeconds );
    }
}
