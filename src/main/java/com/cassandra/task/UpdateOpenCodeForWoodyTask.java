package com.cassandra.task;


import com.cassandra.service.BetService;
import com.cassandra.utils.S118Utils;
import com.cassandra.utils.WoodyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class UpdateOpenCodeForWoodyTask {

//    @Scheduled(cron = "5/10 * * * * *")
    public void updateOpenCodeWoody() {
        log.info("投注之前的金额：{}",S118Utils.getBalance(WoodyUtils.getToken("163.com")));
        //BetService.betByHistory();
        //BetService.betOdd();
        //这位老司机稳
        BetService.betOnlyOdd();
        log.info("投注之后金额：{}",S118Utils.getBalance(WoodyUtils.getToken("163.com")));
    }
}
