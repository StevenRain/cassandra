package com.cassandra.utils;

import com.cassandra.dto.entity.BettingDto;
import com.cassandra.dto.entity.UserInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class WoodyUtils {

    private WoodyUtils() {
    }

    public static String getToken(String email){
        Optional<UserInfo> userInfoOptional= UserConfigUtils.getAllUserConfig().stream().filter(userWoody -> userWoody.getEmail().contains(email)).findAny();
        if(userInfoOptional.isPresent()) {
            return userInfoOptional.get().getToken();
        }
        return "";
    }

    public static BettingDto buildBetByNumber(String betNumber,double price, String token) {
        log.info("下注号码为：" + betNumber);
        log.info("下注金额为：" + price);
        if ("单".equals(betNumber)) {

            return BettingDto.builder()
                    .gameIssueNumber(S118Utils.getLatestGameIssueNumber())
                    .price(price)
                    .bettingNumber("单")
                    .token(token)
                    .build();

        } else if ("双".equals(betNumber)) {

            return BettingDto.builder()
                    .gameIssueNumber(S118Utils.getLatestGameIssueNumber())
                    .price(price)
                    .bettingNumber("双")
                    .token(token)
                    .build();

        } else if ("大".equals(betNumber)) {

            return BettingDto.builder()
                    .gameIssueNumber(S118Utils.getLatestGameIssueNumber())
                    .price(price)
                    .bettingNumber("大")
                    .token(token)
                    .build();

        } else if ("小".equals(betNumber)) {

            return BettingDto.builder()
                    .gameIssueNumber(S118Utils.getLatestGameIssueNumber())
                    .price(price)
                    .bettingNumber("小")
                    .token(token)
                    .build();

        }
        return null;
    }
}
