package com.cassandra.task;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UpdateOpenCodeTask {


    @Scheduled(cron = "0/5 * * * * *")
    public void updateOpenCode() {
        log.info("正在执行定时任务");
    }
}
