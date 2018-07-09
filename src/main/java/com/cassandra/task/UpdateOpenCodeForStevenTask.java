package com.cassandra.task;

import com.cassandra.dto.entity.OpenResult;
import com.cassandra.dto.entity.UserInfo;
import com.cassandra.service.BettingMethodServiceForSteven;
import com.cassandra.utils.EmailUtils;
import com.cassandra.utils.S118Utils;
import com.cassandra.utils.SoundUtils;
import com.cassandra.utils.UserConfigUtils;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class UpdateOpenCodeForStevenTask {

    private static Map<String, Object> cacheMap = Maps.newHashMap();
    private static final String KEY_FOR_OPEN_RESULT = "openResultKey";

    @Resource
    private BettingMethodServiceForSteven bettingMethodService;

    private void printAnalyzeResult(OpenResult openResult) {
        OpenResult openResultInCache = (OpenResult)cacheMap.get(KEY_FOR_OPEN_RESULT);
        if(Objects.nonNull(openResultInCache) && openResult.equals(openResultInCache)) {
            log.info("等待开奖");
            return;
        }
        SoundUtils.shortBeep();
        log.info("本次分析结果");
        openResult.getOpenResultDtoList().forEach(dto -> log.info("{}, {}, {}, {}, {}", dto.getGameIssueNo(), dto.getOpenCode(), dto.getBigOrSmall(), dto.getOddOrEven(), dto.getNumberProperty()));
        log.info("大 {} 小 {} 单 {} 双 {}", openResult.getBigRatio(), openResult.getSmallRatio(), openResult.getOddRatio(), openResult.getEvenRatio());
        cacheMap.put(KEY_FOR_OPEN_RESULT, openResult);
    }

    @Scheduled(cron = "0 0/30 * * * *")
    public void balanceNotify() {
        Optional<UserInfo> userInfoOptional= UserConfigUtils.getAllUserConfig().stream().filter(userInfo -> userInfo.getEmail().contains("gmail")).findAny();
        userInfoOptional.ifPresent(userInfo -> {
            double balance = S118Utils.getBalance(userInfo.getToken());
            String to = "renjie373270@gmail.com";
            String subject = "余额提醒";
            String content = "最新余额为 " + balance;
            EmailUtils.sendEmail(to, subject, content);
        });
    }


    @Scheduled(fixedRate = 10000)
    public void updateOpenCodeSteven() {
        Optional<UserInfo> userInfoOptional= UserConfigUtils.getAllUserConfig().stream().filter(userInfo -> userInfo.getEmail().contains("gmail")).findAny();
        userInfoOptional.ifPresent(userInfo -> {
            OpenResult openResult = S118Utils.buildLatestOpenResult();
            printAnalyzeResult(openResult);
            bettingMethodService.bet4(openResult, userInfo);
        });
    }
}
