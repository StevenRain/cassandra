package com.cassandra.task;


import com.cassandra.service.BetService;
import com.cassandra.utils.S118Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class UpdateOpenCodeForWoodyTask {

    private final String token="d2b30fca9a00f008cc8af877baeeef8c";
    private final String userName="guest0080233";
    private final String psw="123456qaz";


    @Scheduled(cron = "5/10 * * * * *")
    public void updateOpenCodeWoody() {
        log.info("投注之前的金额：{}",S118Utils.getBalance(token));
        BetService.betByHistory();
        log.info("投注之后金额：{}",S118Utils.getBalance(token));
    }
}
