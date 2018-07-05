package com.cassandra.service;

import com.cassandra.dto.entity.OpenResult;
import com.cassandra.utils.PublicUtils;
import com.cassandra.utils.S118Utils;
import com.cassandra.utils.WoodyUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class BetService {
    public static void  betByHistory(){
        String curentGameIssueNo = S118Utils.getLatestGameIssueNumber();
        List<OpenResult.OpenResultDto> openResultDtoList = S118Utils.buildLatestOpenResult().getOpenResultDtoList();
        openResultDtoList.forEach(openResultDto -> log.info("{}", openResultDto));
        //倒数第二
        OpenResult.OpenResultDto openResultTow = openResultDtoList.get(openResultDtoList.size() - 2);
        //倒数第一
        OpenResult.OpenResultDto lastResult = openResultDtoList.get(openResultDtoList.size() - 1);

        if (Long.valueOf(curentGameIssueNo)  - Long.valueOf(lastResult.getGameIssueNo())==1){
            log.info("投注的期号相同不投注");
            return;
        }
        log.info("投注开始.....");

        //单双连续
        if (lastResult.getOddOrEven().equals(openResultTow.getOddOrEven())){
            //单双都投注 调用投注的接口
             S118Utils.bet( WoodyUtils.buildBetByNumber(lastResult.getOddOrEven(),PublicUtils.PRICE,WoodyUtils.getToken(PublicUtils.EMAIL)));
        }else {
            //投注单跳
            if(lastResult.getOddOrEven().equals(PublicUtils.ODD)){
                 lastResult.setOddOrEven(PublicUtils.EVEN);
                 S118Utils.bet( WoodyUtils.buildBetByNumber(lastResult.getOddOrEven(),PublicUtils.PRICE,WoodyUtils.getToken(PublicUtils.EMAIL)));
            }else {
                 lastResult.setOddOrEven(PublicUtils.ODD);
                 S118Utils.bet( WoodyUtils.buildBetByNumber(lastResult.getOddOrEven(),PublicUtils.PRICE,WoodyUtils.getToken(PublicUtils.EMAIL)));
            }

        }
        //大小连续
        if (lastResult.getBigOrSmall().equals(openResultTow.getBigOrSmall())){
            S118Utils.bet( WoodyUtils.buildBetByNumber(lastResult.getBigOrSmall(),PublicUtils.PRICE,WoodyUtils.getToken(PublicUtils.EMAIL)));
        }else {
            //投注单跳
            if(lastResult.getBigOrSmall().equals(PublicUtils.BIG)){
                lastResult.setBigOrSmall(PublicUtils.SMALL);
                 S118Utils.bet( WoodyUtils.buildBetByNumber(lastResult.getBigOrSmall(),PublicUtils.PRICE,WoodyUtils.getToken(PublicUtils.EMAIL)));
            }else {
                lastResult.setBigOrSmall(PublicUtils.BIG);
                 S118Utils.bet( WoodyUtils.buildBetByNumber(lastResult.getOddOrEven(),PublicUtils.PRICE,WoodyUtils.getToken(PublicUtils.EMAIL)));
            }
        }


        log.info("本期{},上一期的期号是{}",S118Utils.getLatestGameIssueNumber(),lastResult.getGameIssueNo());
    }
    public static void betOdd(){
        String curentGameIssueNo = S118Utils.getLatestGameIssueNumber();
        List<OpenResult.OpenResultDto> openResultDtoList = S118Utils.buildLatestOpenResult().getOpenResultDtoList();
        openResultDtoList.forEach(openResultDto -> log.info("{}", openResultDto));
        //倒数第二
        OpenResult.OpenResultDto openResultTow = openResultDtoList.get(openResultDtoList.size() - 2);
        //倒数第一
        OpenResult.OpenResultDto lastResult = openResultDtoList.get(openResultDtoList.size() - 1);

        if (Long.valueOf(curentGameIssueNo)  - Long.valueOf(lastResult.getGameIssueNo())==1){
            log.info("投注的期号相同不投注");
            return;
        }
        //单双连续
        if (lastResult.getOddOrEven().equals(openResultTow.getOddOrEven())){
            //单双都投注 调用投注的接口
            S118Utils.bet( WoodyUtils.buildBetByNumber(lastResult.getOddOrEven(),PublicUtils.PRICE,WoodyUtils.getToken(PublicUtils.EMAIL)));
        }else {
            log.info("");
        }
    }
    //单双  大小  连续
    public static void betOnlyOdd(){
        String curentGameIssueNo = S118Utils.getLatestGameIssueNumber();
        List<OpenResult.OpenResultDto> openResultDtoList = S118Utils.buildLatestOpenResult().getOpenResultDtoList();
        openResultDtoList.forEach(openResultDto -> log.info("{}", openResultDto));
        //倒数第二
        OpenResult.OpenResultDto openResultTow = openResultDtoList.get(openResultDtoList.size() - 2);
        //倒数第一
        OpenResult.OpenResultDto lastResult = openResultDtoList.get(openResultDtoList.size() - 1);

        if (Long.valueOf(curentGameIssueNo)  - Long.valueOf(lastResult.getGameIssueNo())==1){
            log.info("投注的期号相同不投注");
            return;
        }
        S118Utils.bet( WoodyUtils.buildBetByNumber(lastResult.getOddOrEven(),PublicUtils.PRICE2,WoodyUtils.getToken(PublicUtils.EMAIL)));
        S118Utils.bet( WoodyUtils.buildBetByNumber(lastResult.getBigOrSmall(),PublicUtils.PRICE,WoodyUtils.getToken(PublicUtils.EMAIL)));
    }

}
