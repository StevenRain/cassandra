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

    public static BettingDto buildBetByNumber(String betNumber, String token) {
        log.info("下注号码为：" + betNumber);
        if ("单".equals(betNumber)) {

            return BettingDto.builder()
                    .gameIssueNumber(S118Utils.getLatestGameIssueNumber())
                    .price(2)
                    .bettingNumber("单")
                    .token(token)
                    .build();

        } else if ("双".equals(betNumber)) {

            return BettingDto.builder()
                    .gameIssueNumber(S118Utils.getLatestGameIssueNumber())
                    .price(2)
                    .bettingNumber("双")
                    .token(token)
                    .build();

        } else if ("大".equals(betNumber)) {

            return BettingDto.builder()
                    .gameIssueNumber(S118Utils.getLatestGameIssueNumber())
                    .price(2)
                    .bettingNumber("大")
                    .token(token)
                    .build();

        } else if ("小".equals(betNumber)) {

            return BettingDto.builder()
                    .gameIssueNumber(S118Utils.getLatestGameIssueNumber())
                    .price(2)
                    .bettingNumber("小")
                    .token(token)
                    .build();

        }
        return null;
    }
}
