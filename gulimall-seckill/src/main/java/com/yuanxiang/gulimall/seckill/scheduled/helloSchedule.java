package com.yuanxiang.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j

public class helloSchedule {

    /**
     * spring中6位组成，不允许第七位的年
     * 定时任务不应该阻塞。默认是阻塞的
     *  1、可以让业务运行以异步的方式，自行提交到线程池
     *  2、支持定时任务线程池
     *  3、让定时任务异步执行
     *      异步任务：1、@EnableAsync
     *              2、给希望异步执行的方法加上标记
     *
     *   使用异步+定时任务完成定时任务不阻塞的功能。
     */
//    @Scheduled(cron = "* 0 * * * ?")
//    public void halo() {
//        log.info("hello....");
//    }
}
