package com.cassandra.task;


import com.cassandra.dto.entity.OpenResult;
import com.cassandra.utils.S118Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UpdateOpenCodeTask {


    @Scheduled(cron = "0/5 * * * * *")
    public void updateOpenCode() {
        OpenResult openResult = S118Utils.buildLatestOpenResult();
        log.info("{}", openResult);
    }
}
