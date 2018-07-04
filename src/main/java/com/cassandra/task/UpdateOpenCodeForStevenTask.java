package com.cassandra.task;

import com.cassandra.dto.entity.OpenResult;
import com.cassandra.dto.entity.UserInfo;
import com.cassandra.utils.S118Utils;
import com.cassandra.utils.UserConfigUtils;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UpdateOpenCodeForStevenTask {

    @Scheduled(cron = "0/10 * * * * *")
    public void updateOpenCodeSteven() {
        OpenResult openResult = S118Utils.buildLatestOpenResult();
        Optional<UserInfo> userInfoOptional= UserConfigUtils.getAllUserConfig().stream().filter(userInfo -> userInfo.getEmail().contains("gmail")).findAny();
        userInfoOptional.ifPresent(userInfoSteven -> {
            //TODO Steven
        });
    }
}
