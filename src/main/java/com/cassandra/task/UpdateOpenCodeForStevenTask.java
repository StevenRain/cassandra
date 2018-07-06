package com.cassandra.task;

import com.cassandra.dto.entity.BettingDto;
import com.cassandra.dto.entity.OpenResult;
import com.cassandra.dto.entity.UserInfo;
import com.cassandra.utils.S118Utils;
import com.cassandra.utils.SoundUtils;
import com.cassandra.utils.UserConfigUtils;
import com.google.common.collect.Maps;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class UpdateOpenCodeForStevenTask {

    private static Map<String, Object> cacheMap = Maps.newHashMap();
    private static final String KEY_FOR_LAST_ISSUE_NUMBER = "keyForLastGameIssueNumber";
    private static final String KEY_FOR_OPEN_RESULT = "openResultKey";

    private void printAnalyzeResult(OpenResult openResult) {
        OpenResult openResultInCache = (OpenResult)cacheMap.get(KEY_FOR_OPEN_RESULT);
        if(Objects.nonNull(openResultInCache) && openResult.equals(openResultInCache)) {
            log.info("等待开奖");
            return;
        }
        SoundUtils.shortBeep();
        log.info("本次分析结果");
        openResult.getOpenResultDtoList().forEach(dto -> log.info("{}", dto));
        log.info("大 {} 小 {} 单 {} 双 {}", openResult.getBigRatio(), openResult.getSmallRatio(), openResult.getOddRatio(), openResult.getEvenRatio());
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


    private void betting(UserInfo userInfo, String bettingNumber) {
        if(StringUtils.isEmpty(bettingNumber)) {
            return;
        }

        double price = buildPrice(userInfo);
        String gameIssueNumber = S118Utils.getLatestGameIssueNumber();
        BettingDto bettingDto = BettingDto.builder().gameIssueNumber(gameIssueNumber).bettingNumber(bettingNumber).price(price).token(userInfo.getToken()).build();
        boolean result = S118Utils.bet(bettingDto);
        if(result) {
            log.info("投注成功，期号 {}, 投注号码 {}, 投注金额 {}", bettingDto.getGameIssueNumber(), bettingDto.getBettingNumber(), bettingDto.getPrice());
        }
    }

    /**
     * 投注方案1，上次出什么就跟什么
     * */
    private void bet1(OpenResult openResult, UserInfo userInfo) {
        List<OpenResult.OpenResultDto> openResultDtoList = openResult.getOpenResultDtoList();
        OpenResult.OpenResultDto openResultDto = openResultDtoList.get(openResultDtoList.size() - 1);
        String bettingNumber1 = openResultDto.getBigOrSmall();
        String bettingNumber2 = openResultDto.getOddOrEven();
        String gameIssueNumber = openResultDto.getGameIssueNo();

        String gameIssueNumberInCache = (String)cacheMap.get(KEY_FOR_LAST_ISSUE_NUMBER);
        if(!StringUtils.isEmpty(gameIssueNumberInCache) && gameIssueNumber.equals(gameIssueNumberInCache)) {
            return;
        }
        cacheMap.put(KEY_FOR_LAST_ISSUE_NUMBER, gameIssueNumber);

        String recommendBettingNumber = S118Utils.getRecommendBettingNumber(openResult);
        if(!StringUtils.isEmpty(recommendBettingNumber)) {
            SoundUtils.longBeep();
        }

        betting(userInfo, bettingNumber1);
        betting(userInfo, bettingNumber2);
        SoundUtils.shortBeep();
    }

    /**
     * 投注方案2，连续两次出什么就跟什么
     * */
    private void bet2(OpenResult openResult, UserInfo userInfo) {
        List<OpenResult.OpenResultDto> openResultDtoList = openResult.getOpenResultDtoList();
        List<OpenResult.OpenResultDto> subOpenResultDtoList = openResultDtoList.stream().skip(openResultDtoList.size() - 2L).collect(Collectors.toList());

        String gameIssueNumber = subOpenResultDtoList.get(1).getGameIssueNo();
        String gameIssueNumberInCache = (String)cacheMap.get(KEY_FOR_LAST_ISSUE_NUMBER);
        if(!StringUtils.isEmpty(gameIssueNumberInCache) && gameIssueNumber.equals(gameIssueNumberInCache)) {
            return;
        }
        cacheMap.put(KEY_FOR_LAST_ISSUE_NUMBER, gameIssueNumber);

        String recommendBettingNumber = "";
        boolean bigSmallMatch = subOpenResultDtoList.stream().map(OpenResult.OpenResultDto::getBigOrSmall).distinct().count() == 1;
        boolean oddEvenMatch = subOpenResultDtoList.stream().map(OpenResult.OpenResultDto::getOddOrEven).distinct().count() == 1;
        recommendBettingNumber = bigSmallMatch ? subOpenResultDtoList.get(0).getBigOrSmall() : recommendBettingNumber;
        betting(userInfo, recommendBettingNumber);

        recommendBettingNumber = oddEvenMatch  ? subOpenResultDtoList.get(0).getOddOrEven() : recommendBettingNumber;
        betting(userInfo, recommendBettingNumber);
        SoundUtils.shortBeep();
    }

    @Scheduled(fixedRate = 10000)
    public void updateOpenCodeSteven() {
        Optional<UserInfo> userInfoOptional= UserConfigUtils.getAllUserConfig().stream().filter(userInfo -> userInfo.getEmail().contains("gmail")).findAny();
        userInfoOptional.ifPresent(userInfo -> {
            OpenResult openResult = S118Utils.buildLatestOpenResult();
            printAnalyzeResult(openResult);
//            bet2(openResult, userInfo);

            String recommendNumber = S118Utils.getRecommendBettingNumber(openResult);
            if(!StringUtils.isEmpty(recommendNumber)) {
                double balance = S118Utils.getBalance(userInfo.getToken());
                log.info("当前余额 {}", balance);
                SoundUtils.longBeep();
            }
        });
    }
}
