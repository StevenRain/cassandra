package com.cassandra.task;


import com.cassandra.dto.entity.OpenResult;
import com.cassandra.dto.entity.UserInfo;
import com.cassandra.utils.S118Utils;
import com.cassandra.utils.UserConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class UpdateOpenCodeTask {

    //TODO
    @Scheduled(cron = "0/10 * * * * *")
    public void updateOpenCodeSteven() {
        OpenResult openResult = S118Utils.buildLatestOpenResult();
        Optional<UserInfo> userInfoOptional= UserConfigUtils.getAllUserConfig().stream().filter(userInfo -> userInfo.getEmail().contains("gmail")).findAny();
        userInfoOptional.ifPresent(userInfoSteven -> {
            //TODO Steven
        });
    }


    @Scheduled(cron = "5/10 * * * * *")
    public void updateOpenCodeWoody() {
        OpenResult openResult = S118Utils.buildLatestOpenResult();
        Optional<UserInfo> userInfoOptional= UserConfigUtils.getAllUserConfig().stream().filter(userInfo -> userInfo.getEmail().contains("163.com")).findAny();
        userInfoOptional.ifPresent(userInfoWoody -> {
            //TODO Woody


        });
    }
}
