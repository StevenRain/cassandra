package com.cassandra.service;

import com.cassandra.dto.entity.BettingDto;
import com.cassandra.dto.entity.OpenResult;
import com.cassandra.dto.entity.UserInfo;
import com.cassandra.utils.S118Utils;
import com.cassandra.utils.SoundUtils;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.cassandra.utils.GlobalParameter.KEEP_BALANCE_RATIO;
import static com.cassandra.utils.GlobalParameter.MAX_FOLLOWING_ISSUES;

@Slf4j
@Service
public class BettingMethodServiceForSteven {

    private static Map<String, Object> cacheMap = Maps.newHashMap();
    private static final String KEY_FOR_LAST_ISSUE_NUMBER = "keyForLastGameIssueNumber";

    private double buildPrice(UserInfo userInfo) {
        double balance = S118Utils.getBalance(userInfo.getToken());
        balance = balance * KEEP_BALANCE_RATIO;
        log.info("{} 当前余额 {}", userInfo.getEmail(), balance);

//        double price = balance / ((2 << MAX_FOLLOWING_ISSUES) - 1);
        double price = balance / 40;
        price = BigDecimal.valueOf(price).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
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
    public void bet1(OpenResult openResult, UserInfo userInfo) {
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
    public void bet2(OpenResult openResult, UserInfo userInfo) {
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


    /**
     * 投注推荐号码
     * */
    private void betRecommendBettingNumber(OpenResult openResult, UserInfo userInfo) {
        String recommendBettingNumber = S118Utils.getRecommendBettingNumber(openResult);
        if(!StringUtils.isEmpty(recommendBettingNumber)) {
            log.info("本期推荐投注 {}", recommendBettingNumber);
            String latestGameIssueNumber = S118Utils.getLatestGameIssueNumber();
            double price = buildPrice(userInfo);
            BettingDto bettingDto = BettingDto.builder().gameIssueNumber(latestGameIssueNumber).bettingNumber(recommendBettingNumber).price(price).token(userInfo.getToken()).build();
            boolean result = S118Utils.bet(bettingDto);
            if(result) {
                SoundUtils.shortBeep();
                log.info("投注成功，期号 {}, 投注号码 {}, 投注金额 {}", bettingDto.getGameIssueNumber(), bettingDto.getBettingNumber(), bettingDto.getPrice());
            }
        }
    }


    /**
     * 投注方案3， 连续n期一样，第n+1期不一样时下注，输了2倍投
     * */
    public void bet3(OpenResult openResult, UserInfo userInfo) {
        List<OpenResult.OpenResultDto> openResultDtoList = openResult.getOpenResultDtoList();
        List<OpenResult.OpenResultDto> subOpenResultDtoList = openResultDtoList.stream().skip(openResultDtoList.size() - 2L).collect(Collectors.toList());

        String gameIssueNumber = subOpenResultDtoList.get(1).getGameIssueNo();
        String gameIssueNumberInCache = (String)cacheMap.get(KEY_FOR_LAST_ISSUE_NUMBER);
        if(!StringUtils.isEmpty(gameIssueNumberInCache) && gameIssueNumber.equals(gameIssueNumberInCache)) {
            return;
        }
        cacheMap.put(KEY_FOR_LAST_ISSUE_NUMBER, gameIssueNumber);

        //处理上一期的情况
        Optional<BettingDto> bettingDtoOptional = S118Utils.getLastLossBettingOrder(userInfo.getToken());
        if(bettingDtoOptional.isPresent()) {
            BettingDto bettingDto = bettingDtoOptional.get();
            log.info("上期投注 {} 输了 {} 本期倍投", bettingDto.getBettingNumber(), bettingDto.getPrice());
            String latestGameIssueNumber = S118Utils.getLatestGameIssueNumber();
            double price = bettingDto.getPrice() * 2;
            bettingDto.setGameIssueNumber(latestGameIssueNumber);
            bettingDto.setPrice(price);
            bettingDto.setToken(userInfo.getToken());
            boolean result = S118Utils.bet(bettingDto);
            if(result) {
                SoundUtils.shortBeep();
                log.info("倍投成功，期号 {}, 投注号码 {}, 投注金额 {}", bettingDto.getGameIssueNumber(), bettingDto.getBettingNumber(), bettingDto.getPrice());
            }
            return;
        }

        betRecommendBettingNumber(openResult, userInfo);
    }

    /**
     * 投注方案4， 连续n期一样，第n+1期不一样时下注，输了不倍投
     * */
    public void bet4(OpenResult openResult, UserInfo userInfo) {
        List<OpenResult.OpenResultDto> openResultDtoList = openResult.getOpenResultDtoList();
        List<OpenResult.OpenResultDto> subOpenResultDtoList = openResultDtoList.stream().skip(openResultDtoList.size() - 2L).collect(Collectors.toList());

        String gameIssueNumber = subOpenResultDtoList.get(1).getGameIssueNo();
        String gameIssueNumberInCache = (String)cacheMap.get(KEY_FOR_LAST_ISSUE_NUMBER);
        if(!StringUtils.isEmpty(gameIssueNumberInCache) && gameIssueNumber.equals(gameIssueNumberInCache)) {
            return;
        }
        cacheMap.put(KEY_FOR_LAST_ISSUE_NUMBER, gameIssueNumber);

        betRecommendBettingNumber(openResult, userInfo);
    }
}
