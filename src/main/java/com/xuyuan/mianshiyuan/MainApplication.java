package com.xuyuan.mianshiyuan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 主类（项目启动入口）
 *
 * @author <a href="https://github.com/xuyuan-upward">许苑向上</a>
 */
// todo 如需开启 Redis，须移除 exclude 中的内容
//@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@SpringBootApplication
@MapperScan("com.xuyuan.mianshiyuan.mapper")
@EnableScheduling
// 开启拦截器扫描
@ServletComponentScan
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

}
