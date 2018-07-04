package com.cassandra.task;

import com.cassandra.dto.entity.BettingDto;
import com.cassandra.dto.entity.OpenResult;
import com.cassandra.dto.entity.UserInfo;
import com.cassandra.utils.S118Utils;
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

    private void printAnalyzeResult(OpenResult openResult) {
        log.info("本次分析结果");
        openResult.getOpenResultDtoList().forEach(dto -> log.info("{}", dto));
        log.info("大 {} 小 {} 单 {} 双 {}", openResult.getBigRatio(), openResult.getSmallRatio(), openResult.getOddRatio(), openResult.getEvenRatio());
    }

    private double buildPrice(String token) {
        double balance = S118Utils.getBalance(token);
        log.info("{} 当前余额", balance);

        double price = BigDecimal.valueOf(balance / 100).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        if(price > 1.0) {
            price = BigDecimal.valueOf(price).setScale(0, BigDecimal.ROUND_DOWN).doubleValue();
        }
        return price;
    }

    private void betting(UserInfo userInfoSteven, String recommandBettingNumber) {
        if(!StringUtils.isEmpty(recommandBettingNumber)) {
            String gameIssueNumber = S118Utils.getLatestGameIssueNumber();
            double price = buildPrice(userInfoSteven.getToken());
            BettingDto bettingDto = BettingDto.builder().gameIssueNumber(gameIssueNumber).bettingNumber(recommandBettingNumber).price(price).token(userInfoSteven.getToken()).build();
            String keyForBettingDto = "keyForBettingDto";
            BettingDto bettingDtoInCache = (BettingDto) cacheMap.get(keyForBettingDto);
            if(Objects.nonNull(bettingDtoInCache) && bettingDtoInCache.getGameIssueNumber().equals(bettingDto.getGameIssueNumber())) {
                return;
            }
            cacheMap.put(keyForBettingDto, bettingDto);
            log.info("本次推荐投注 {}", recommandBettingNumber);
            boolean result = S118Utils.bet(bettingDto);
            if(result) {
                log.info("{} 投注成功, 投注期号{}, 投注内容{}, 投注价格{}", userInfoSteven.getEmail(), bettingDto.getGameIssueNumber(), bettingDto.getBettingNumber(), bettingDto.getPrice());
            }else {
                log.error("投注失败");
            }
        }else {
            log.info("本次没有推荐投注的内容");
        }
    }

    @Scheduled(cron = "0/10 * * * * *")
    public void updateOpenCodeSteven() {
        Optional<UserInfo> userInfoOptional= UserConfigUtils.getAllUserConfig().stream().filter(userInfo -> userInfo.getEmail().contains("gmail")).findAny();
        userInfoOptional.ifPresent(userInfoSteven -> {
            String keyForOpenResult = "openResultKey";
            OpenResult openResult = S118Utils.buildLatestOpenResult();
            OpenResult openResultInCache = (OpenResult)cacheMap.get(keyForOpenResult);
            if(Objects.nonNull(openResultInCache) && openResult.equals(openResultInCache)) {
                log.info("等待开奖结果更新");
                return;
            }
            printAnalyzeResult(openResult);
            cacheMap.put(keyForOpenResult, openResult);
            //TODO 倍投优先

            String recommendBettingNumber = S118Utils.getRecommendBettingNumber(openResult);
            betting(userInfoSteven, recommendBettingNumber);
        });
    }
}
