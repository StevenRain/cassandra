package com.cassandra.task;


import com.cassandra.service.BetService;
import com.cassandra.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class UpdateOpenCodeForWoodyTask {


    @Scheduled(cron = "5/10 * * * * *")
    public void updateOpenCodeWoody() {
        double bBalance= S118Utils.getBalance(WoodyUtils.getToken("163.com"));
        log.info("投注之前的金额：{}",bBalance);
//        BetService.betByHistory();
//        BetService.betOdd();
        //这位老司机稳
        BetService.betOnlyOdd();
        double eBalance= S118Utils.getBalance(WoodyUtils.getToken("163.com"));
        log.info("投注之后金额：{}",eBalance);
    }


    @Scheduled(cron = "0/30 * * * * *")
    public void balanceCheck() {
        String token = WoodyUtils.getToken("163.com");
        double balance = S118Utils.getBalance(token);
        if(balance > 1000.0) {
            EmailUtils.sendEmail("jiangzhuanximl@163.com", "余额提醒", "余额到1000了，快提款");
        }
    }
}
