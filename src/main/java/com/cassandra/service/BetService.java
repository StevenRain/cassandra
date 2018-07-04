package com.cassandra.service;

import com.cassandra.dto.entity.BettingDto;
import com.cassandra.dto.entity.OpenResult;
import com.cassandra.utils.PublicUtils;
import com.cassandra.utils.S118Utils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public class BetService {

    public static void  betByHistory(){
        List<OpenResult.OpenResultDto> openResultDtoList = S118Utils.buildLatestOpenResult().getOpenResultDtoList();
        openResultDtoList.stream().forEach(openResultDto -> System.out.println(openResultDto));
        //倒数第二
        OpenResult.OpenResultDto openResultTow = openResultDtoList.get(openResultDtoList.size() - 2);
        //倒数第一
        OpenResult.OpenResultDto lastResult = openResultDtoList.get(openResultDtoList.size() - 1);


        //单双连续
        if (lastResult.getOddOrEven().equals(openResultTow.getOddOrEven())){
            //单双都投注 调用投注的接口
             S118Utils.bet( S118Utils.buildBetByNumber(lastResult.getOddOrEven(),S118Utils.getToken(PublicUtils.EMAIL)));
        }else {
            //投注单跳
            if(lastResult.getOddOrEven().equals(PublicUtils.ODD)){
                 lastResult.setOddOrEven(PublicUtils.EVEN);
                 S118Utils.bet( S118Utils.buildBetByNumber(lastResult.getOddOrEven(),S118Utils.getToken(PublicUtils.EMAIL)));
            }else {
                 lastResult.setOddOrEven(PublicUtils.ODD);
                 S118Utils.bet( S118Utils.buildBetByNumber(lastResult.getOddOrEven(),S118Utils.getToken(PublicUtils.EMAIL)));
            }

        }
        //大小连续
        if (lastResult.getBigOrSmall().equals(openResultTow.getBigOrSmall())){
            S118Utils.bet( S118Utils.buildBetByNumber(lastResult.getBigOrSmall(),S118Utils.getToken(PublicUtils.EMAIL)));
        }else {
            //投注单跳
            if(lastResult.getBigOrSmall().equals(PublicUtils.BIG)){
                lastResult.setOddOrEven(PublicUtils.SMALL);
                 S118Utils.bet( S118Utils.buildBetByNumber(lastResult.getBigOrSmall(),S118Utils.getToken(PublicUtils.EMAIL)));
            }else {
                lastResult.setOddOrEven(PublicUtils.BIG);
                 S118Utils.bet( S118Utils.buildBetByNumber(lastResult.getOddOrEven(),S118Utils.getToken(PublicUtils.EMAIL)));
            }
        }


        log.info("本期{},没有投注,上一期的期号是{}",S118Utils.getLatestGameIssueNumber(),lastResult.getGameIssueNo());
    }

}
