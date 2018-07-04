package com.cassandra.task;


import com.cassandra.dto.entity.OpenResult;
import com.cassandra.dto.entity.UserInfo;
import com.cassandra.service.BetService;
import com.cassandra.utils.S118Utils;
import com.cassandra.utils.UserConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class UpdateOpenCodeForWoodyTask {
    private final String token="a9aa04fc4c3cebd32920fffc5a834c01";
    @Autowired
    @Scheduled(cron = "5/10 * * * * *")
    public void updateOpenCodeWoody() {
        log.info("投注之前的金额：{}",S118Utils.getBalance(token));
        BetService.betByHistory();
        log.info("投注之后金额：{}",S118Utils.getBalance(token));
    }
}
