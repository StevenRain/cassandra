package com.cassandra.task;

import com.cassandra.dto.entity.BettingDto;
import com.cassandra.dto.entity.OpenResult;
import com.cassandra.dto.entity.UserInfo;
import com.cassandra.utils.S118Utils;
import com.cassandra.utils.SleepUtils;
import com.cassandra.utils.SoundUtils;
import com.cassandra.utils.UserConfigUtils;
import com.google.common.collect.Maps;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class UpdateOpenCodeForStevenTask {

    private static Map<String, Object> cacheMap = Maps.newHashMap();
    private static final String KEY_FOR_BETTING_DTO = "keyForBettingDto";
    private static final String KEY_FOR_OPEN_RESULT = "openResultKey";

    private void printAnalyzeResult(OpenResult openResult) {
        OpenResult openResultInCache = (OpenResult)cacheMap.get(KEY_FOR_OPEN_RESULT);
        if(Objects.nonNull(openResultInCache) && openResult.equals(openResultInCache)) {
            log.info("等待开奖");
            return;
        }
        log.info("本次分析结果");
        openResult.getOpenResultDtoList().forEach(dto -> log.info("{}", dto));
        log.info("大 {} 小 {} 单 {} 双 {}", openResult.getBigRatio(), openResult.getSmallRatio(), openResult.getOddRatio(), openResult.getEvenRatio());
        String recommendBettingNumber = S118Utils.getRecommendBettingNumber(openResult);
        if(StringUtils.isEmpty(recommendBettingNumber)) {
            SoundUtils.shortBeep();
        }else {
            SoundUtils.longBeep();
        }
        cacheMap.put(KEY_FOR_OPEN_RESULT, openResult);
    }

    private double buildPrice(UserInfo userInfo) {
        double balance = S118Utils.getBalance(userInfo.getToken());
        log.info("{} 当前余额 {}", userInfo.getEmail(), balance);

        double price = BigDecimal.valueOf(balance / 40).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        if(price > 1.0) {
            price = BigDecimal.valueOf(price).setScale(0, BigDecimal.ROUND_DOWN).doubleValue();
        }
        return price;
    }

    private void betting(UserInfo userInfo, String recommendBettingNumber) {
        if(!StringUtils.isEmpty(recommendBettingNumber)) {
            String gameIssueNumber = S118Utils.getLatestGameIssueNumber();
            double price = buildPrice(userInfo);
            BettingDto bettingDto = BettingDto.builder().gameIssueNumber(gameIssueNumber).bettingNumber(recommendBettingNumber).price(price).token(userInfo.getToken()).build();
            BettingDto bettingDtoInCache = (BettingDto) cacheMap.get(KEY_FOR_BETTING_DTO);
            if(Objects.nonNull(bettingDtoInCache) && bettingDtoInCache.getGameIssueNumber().equals(bettingDto.getGameIssueNumber())) {
                return;
            }
            log.info("本次推荐投注 {}", recommendBettingNumber);
            boolean result = S118Utils.bet(bettingDto);
            if(result) {
                cacheMap.put(KEY_FOR_BETTING_DTO, bettingDto);
                log.info("{} 投注成功, 投注期号{}, 投注内容{}, 投注价格{}", userInfo.getEmail(), bettingDto.getGameIssueNumber(), bettingDto.getBettingNumber(), bettingDto.getPrice());
            }else {
                SleepUtils.sleepMilliseconds(5100);
                result = S118Utils.bet(bettingDto);
                if(result) {
                    cacheMap.put(KEY_FOR_BETTING_DTO, bettingDto);
                    log.info("{} 投注成功, 投注期号{}, 投注内容{}, 投注价格{}", userInfo.getEmail(), bettingDto.getGameIssueNumber(), bettingDto.getBettingNumber(), bettingDto.getPrice());
                }else {
                    log.error("投注失败");
                }
            }
        }else {
            log.info("本次没有推荐投注的内容");
        }
    }


    @Scheduled(fixedRate = 10000)
    public void updateOpenCodeSteven() {
        Optional<UserInfo> userInfoOptional= UserConfigUtils.getAllUserConfig().stream().filter(userInfo -> userInfo.getEmail().contains("gmail")).findAny();
        userInfoOptional.ifPresent(userInfo -> {
            OpenResult openResult = S118Utils.buildLatestOpenResult();
            printAnalyzeResult(openResult);

//            String recommendBettingNumber = S118Utils.getRecommendBettingNumber(openResult);
//            betting(userInfo, recommendBettingNumber);
        });
    }
}
