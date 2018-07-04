package com.cassandra.task;

import com.cassandra.dto.entity.BettingDto;
import com.cassandra.dto.entity.OpenResult;
import com.cassandra.dto.entity.UserInfo;
import com.cassandra.utils.S118Utils;
import com.cassandra.utils.SleepUtils;
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
        log.info("本次分析结果");
        openResult.getOpenResultDtoList().forEach(dto -> log.info("{}", dto));
        log.info("大 {} 小 {} 单 {} 双 {}", openResult.getBigRatio(), openResult.getSmallRatio(), openResult.getOddRatio(), openResult.getEvenRatio());
    }

    private double buildPrice(UserInfo userInfo) {
        double balance = S118Utils.getBalance(userInfo.getToken());
        log.info("{} 当前余额 {}", userInfo.getEmail(), balance);

        double price = BigDecimal.valueOf(balance / 20).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
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

//    private boolean bettingOneMore(UserInfo userInfo) {
//        Optional<BettingDto> bettingDtoOptional = S118Utils.getLastBettingOrder(userInfo.getToken());
//        bettingDtoOptional.ifPresent(bettingDto -> {
//            double price = bettingDto.getPrice() * 2;
//            log.info("用户 {} 上期未中奖，本次加倍投注, 投注金额 {}", userInfo.getEmail(), price);
//            String gameIssueNumber = S118Utils.getLatestGameIssueNumber();
//            BettingDto bettingDtoNew = BettingDto.builder().bettingNumber(bettingDto.getBettingNumber()).price(price).token(bettingDto.getToken()).gameIssueNumber(gameIssueNumber).token(userInfo.getToken()).build();
//
//            BettingDto bettingDtoInCache = (BettingDto) cacheMap.get(KEY_FOR_BETTING_DTO);
//            if(Objects.nonNull(bettingDtoInCache) && bettingDtoInCache.getGameIssueNumber().equals(gameIssueNumber)) {
//                return;
//            }
//
//            boolean result = S118Utils.bet(bettingDtoNew);
//            if(result) {
//                cacheMap.put(KEY_FOR_BETTING_DTO, bettingDtoNew);
//                log.info("{} 投注成功, 投注期号{}, 投注内容{}, 投注价格{}", userInfo.getEmail(), bettingDtoNew.getGameIssueNumber(), bettingDtoNew.getBettingNumber(), bettingDtoNew.getPrice());
//            }else {
//                SleepUtils.sleepMilliseconds(5000);
//                result = S118Utils.bet(bettingDtoNew);
//                if(result) {
//                    cacheMap.put(KEY_FOR_BETTING_DTO, bettingDtoNew);
//                    log.info("{} 投注成功, 投注期号{}, 投注内容{}, 投注价格{}", userInfo.getEmail(), bettingDtoNew.getGameIssueNumber(), bettingDtoNew.getBettingNumber(), bettingDtoNew.getPrice());
//                }else {
//                    log.error("投注失败");
//                }
//            }
//        });
//        return bettingDtoOptional.isPresent();
//    }


    @Scheduled(fixedRate = 10000)
    public void updateOpenCodeSteven() {
        Optional<UserInfo> userInfoOptional= UserConfigUtils.getAllUserConfig().stream().filter(userInfo -> userInfo.getEmail().contains("gmail")).findAny();
        userInfoOptional.ifPresent(userInfo -> {
            OpenResult openResult = S118Utils.buildLatestOpenResult();
            printAnalyzeResult(openResult);

//            boolean bettingOneMore = bettingOneMore(userInfo);
//            if(!bettingOneMore) {
//                String recommendBettingNumber = S118Utils.getRecommendBettingNumber(openResult);
//                betting(userInfo, recommendBettingNumber);
//            }
            String recommendBettingNumber = S118Utils.getRecommendBettingNumber(openResult);
            betting(userInfo, recommendBettingNumber);
        });
    }
}
