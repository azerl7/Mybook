package com.geo.mybook.user.biz.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/*
creator：AZERL7
createTime：15:07
*/
@Configuration
public class ThreadPoolConfig {

    @Bean(name="taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor(){
        ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
        //核心线程数
        executor.setCorePoolSize(8);
        //最大线程数
        executor.setMaxPoolSize(32);
        //队列容量
        executor.setQueueCapacity(128);
        //线程活跃时间
        executor.setKeepAliveSeconds(32);
        //线程前缀名
        executor.setThreadNamePrefix("UserExecutor-");

        //拒绝策略：由调用线程处理（一般为主线程）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        //等待所有任务结束之后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        //设置等待时间，超过这个时间之后强制关闭（单位S秒）
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();//差点搞忘了
        return executor;
    }

}
