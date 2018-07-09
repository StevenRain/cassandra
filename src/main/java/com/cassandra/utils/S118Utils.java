package com.cassandra.utils;

import com.cassandra.dto.entity.BettingDto;
import com.cassandra.dto.entity.OpenResult;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import static com.cassandra.utils.GlobalParameter.CONTINUE_ISSUES;
import static com.cassandra.utils.GlobalParameter.THRESHOLD;

@Slf4j
public class S118Utils {


    private S118Utils() {
    }

    @Data
    private static class GameData {
        @SerializedName("issue")
        private String gameIssueNo;

        @SerializedName("winningNum")
        private String openCode;

        @SerializedName("kjnsdgjinidf")
        private List<String> openCodes;
    }

    private static Map<String, String> getHeaderMap() {
        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put("fr", "9");
        headerMap.put("Content-Type", "application/json");
        headerMap.put("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");
        return headerMap;
    }

    /**
     * 获取开奖数据
     * */
    private static List<GameData> getGameDataList() {
        String url = "https://cai33.net/apis/lotIssue/findPic";
        Map<String, String> headerMap = getHeaderMap();
        String jsonData = "{\"lotId\":55,\"nearly\":101}";

        String result = HttpUtils.sendPostByJsonData(url, headerMap, jsonData);
        List<GameData> gameDataList = Lists.newLinkedList();
        new JsonParser().parse(result).getAsJsonObject().get("data").getAsJsonArray().forEach(jsonElement -> {
            GameData gameData = new Gson().fromJson(jsonElement.toString(), GameData.class);
            gameData.setOpenCode(gameData.getOpenCode().replace("|", ","));
            gameData.setOpenCodes(Arrays.stream(gameData.getOpenCode().split(",")).collect(Collectors.toList()));
            gameDataList.add(gameData);
        });
        return gameDataList;
    }

    private static Predicate<Integer> isBig = x -> x >= 11;
    private static Predicate<Integer> isOdd = x -> x % 2 == 1;

    private static Comparator<GameData> comparator = (x1, x2) -> {
        Long v1 = Long.parseLong(x1.getGameIssueNo());
        Long v2 = Long.parseLong(x2.getGameIssueNo());
        return v1.compareTo(v2);
    };

    /**
     * 生成最近10期的开奖结果分析
     * */
    public static OpenResult buildLatestOpenResult() {
        List<GameData> gameDataList = getGameDataList();
        gameDataList = gameDataList.subList(0, 10);
        gameDataList.sort(comparator);
        int bigCount = 0;
        int smallCount = 0;
        int oddCount = 0;
        int evenCount = 0;
        List<OpenResult.OpenResultDto> openResultDtoList = Lists.newLinkedList();
        int total = gameDataList.size();
        for(GameData gameData : gameDataList) {
            int sum = gameData.getOpenCodes().stream().mapToInt(Integer::parseInt).sum();
            OpenResult.OpenResultDto openResultDto = new OpenResult.OpenResultDto();
            if(isBig.test(sum)) {
                bigCount = bigCount + 1;
                openResultDto.setBigOrSmall("大");
            }else {
                smallCount = smallCount + 1;
                openResultDto.setBigOrSmall("小");
            }
            if(isOdd.test(sum)) {
                oddCount = oddCount + 1;
                openResultDto.setOddOrEven("单");
            }else {
                evenCount = evenCount + 1;
                openResultDto.setOddOrEven("双");
            }
            openResultDto.setGameIssueNo(gameData.getGameIssueNo());
            openResultDto.setOpenCode(gameData.getOpenCode());
            int count = Sets.newHashSet(gameData.getOpenCodes()).size();
            openResultDto.setNumberProperty(count + "");
            openResultDtoList.add(openResultDto);
        }

        double bigRatio = BigDecimal.valueOf((double)bigCount / total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        double smallRatio = BigDecimal.valueOf((double)smallCount / total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        double oddRatio = BigDecimal.valueOf((double)oddCount / total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        double evenRatio = BigDecimal.valueOf((double)evenCount / total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        return OpenResult.builder()
                .bigRatio(bigRatio)
                .smallRatio(smallRatio)
                .oddRatio(oddRatio)
                .evenRatio(evenRatio)
                .openResultDtoList(openResultDtoList)
                .build();
    }


    /**
     * 获取用户余额
     * */
    public static double getBalance(String token) {
        Map<String, String> headerMap = getHeaderMap();
        headerMap.put("tk", token);

        String payload = "{}";
        String balanceUrl = "https://11c8.cc/apis/money/findBalanceApp";
        String result = HttpUtils.sendPostByJsonData(balanceUrl, headerMap, payload);
        if(result.contains("balance")) {
            String balanceString = new JsonParser().parse(result).getAsJsonObject().getAsJsonObject("data").get("balance").getAsString();
            return Double.parseDouble(balanceString);
        }
        return 0.0;
    }

    /**
     * 获取最新期号
     * */
    public static String getLatestGameIssueNumber() {
        String url = "https://11c8.cc/apis/lotIssue/findOpen?lotId=55";
        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put("fr", "9");
        String result = HttpUtils.sendGet(url, headerMap);
        return new JsonParser().parse(result).getAsJsonObject().getAsJsonObject("data").get("issue").getAsString();
    }


    /**
     * 投注
     * */
    public static boolean bet(BettingDto bettingDto) {
        String bettingUrl = "https://11c8.cc/apis/orderLot/addApp";
        Map<String, String> headerMap = getHeaderMap();
        headerMap.put("tk", bettingDto.getToken());

        String bettingPattern = "{\"lotId\":55,\"isChase\":0,\"chaseCount\":0,\"baseInfo\":[{\"key\":\"ffkshz\",\"betCode\":\"%s\",\"betNum\":1,\"thisReward\":0,\"odds\":\"{\\\"%s\\\":1.98}\",\"betType\":0,\"oneMoney\":\"%.2f\",\"money\":%.2f,\"position\":\"\",\"issue\":\"%s\"}]}";
        String payload = String.format(bettingPattern, bettingDto.getBettingNumber(), bettingDto.getBettingNumber(), bettingDto.getPrice(), bettingDto.getPrice(), bettingDto.getGameIssueNumber());

        String result = HttpUtils.sendPostByJsonData(bettingUrl, headerMap, payload);
        int returnCode = new JsonParser().parse(result).getAsJsonObject().get("code").getAsInt();
        if(returnCode == 200) {
            return true;
        }else {
            log.error("投注失败，投注dto{}，payload {}, 错误消息 {}", bettingDto, payload, result);
            return false;
        }
    }

    private static Predicate<List<OpenResult.OpenResultDto>> bigSmallPredicate = subOpenResultDtoList -> subOpenResultDtoList.stream().limit(CONTINUE_ISSUES).map(OpenResult.OpenResultDto::getBigOrSmall).distinct().count() == 1 &&
            subOpenResultDtoList.stream().limit(CONTINUE_ISSUES + 1L).map(OpenResult.OpenResultDto::getBigOrSmall).distinct().count() == 2;

    private static Predicate<List<OpenResult.OpenResultDto>> oddEvenPredicate = subOpenResultDtoList -> subOpenResultDtoList.stream().limit(CONTINUE_ISSUES).map(OpenResult.OpenResultDto::getOddOrEven).distinct().count() == 1 &&
            subOpenResultDtoList.stream().limit(CONTINUE_ISSUES + 1L).map(OpenResult.OpenResultDto::getOddOrEven).distinct().count() == 2;

    public static String getRecommendBettingNumber(OpenResult openResult) {
        List<OpenResult.OpenResultDto> openResultDtoList = openResult.getOpenResultDtoList();
        List<OpenResult.OpenResultDto> subOpenResultDtoList = openResultDtoList.stream().skip(openResultDtoList.size() - (long)(CONTINUE_ISSUES + 1)).collect(Collectors.toList());
        //前3个一样，最后一个不一样的
        boolean bigSmallMatch = bigSmallPredicate.test(subOpenResultDtoList);
        boolean oddEvenMatch = oddEvenPredicate.test(subOpenResultDtoList);

        String finalBettingNumber = "";
        double minRatio = 1.0;
        if(bigSmallMatch) {
            String bettingNumber = subOpenResultDtoList.stream().skip(CONTINUE_ISSUES).map(OpenResult.OpenResultDto::getBigOrSmall).findAny().orElse("");
            if(bettingNumber.equals("大") && openResult.getBigRatio() <= THRESHOLD) {
                minRatio = openResult.getBigRatio();
                finalBettingNumber = bettingNumber;
            }
            if(bettingNumber.equals("小") && openResult.getSmallRatio() <= THRESHOLD) {
                minRatio = openResult.getSmallRatio();
                finalBettingNumber = bettingNumber;
            }
        }

        if(oddEvenMatch) {
            String bettingNumber = subOpenResultDtoList.stream().skip(CONTINUE_ISSUES).map(OpenResult.OpenResultDto::getOddOrEven).findAny().orElse("");
            if(bettingNumber.equals("单") && openResult.getOddRatio() <= THRESHOLD && openResult.getOddRatio() < minRatio) {
                minRatio = openResult.getOddRatio();
                finalBettingNumber = bettingNumber;
            }
            if(bettingNumber.equals("双") && openResult.getEvenRatio() <= THRESHOLD && openResult.getEvenRatio() < minRatio) {
                finalBettingNumber = bettingNumber;
            }
        }
        return finalBettingNumber;
    }


    public static Optional<BettingDto> getLastLossBettingOrder(String token) {
        Map<String, String> headerMap = getHeaderMap();
        headerMap.put("tk", token);

        String url = "https://11c8.cc/apis/orderLot/findByConditionApp";
        String queryPattern = "{\"type\":null,\"createTimeStart\":%d,\"createTimeEnd\":%d,\"pageNo\":1,\"pageSize\":1}";
        long startTimestamp = LocalDateTime.now().withHour(0).withMinute(0).withSecond(1).toEpochSecond(ZoneOffset.ofHours(8)) * 1000;
        long endTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)) * 1000;
        String payload = String.format(queryPattern, startTimestamp, endTimestamp);

        String result = HttpUtils.sendPostByJsonData(url, headerMap, payload);
        if(!result.contains("winMoney")) {
            return Optional.empty();
        }
        JsonObject jsonObject = new JsonParser().parse(result).getAsJsonObject().getAsJsonObject("data").getAsJsonArray("resultList").get(0).getAsJsonObject();
        double winMoney = jsonObject.get("winMoney").getAsDouble();


        if(winMoney < 0.01) {
            long transactionId = jsonObject.get("id").getAsLong();
            String gameIssueNumber = jsonObject.get("issue").getAsString();
            double price = jsonObject.get("money").getAsDouble();
            url = "https://11c8.cc/apis/orderLot/findDetailApp";
            String pattern = "{\"id\":%d}";
            queryPattern = String.format(pattern, transactionId);
            result = HttpUtils.sendPostByJsonData(url, headerMap, queryPattern);
            String bettingNumber = new JsonParser().parse(result).getAsJsonObject().getAsJsonObject("data").get("betCode").getAsString();
            String winningNumber = new JsonParser().parse(result).getAsJsonObject().getAsJsonObject("data").get("winningNum").toString();
            BettingDto bettingDto = BettingDto.builder().price(price).bettingNumber(bettingNumber).gameIssueNumber(gameIssueNumber).build();

            if(StringUtils.isEmpty(winningNumber) || winningNumber.equals("null")) {
                return Optional.empty();
            }

            String latestGameIssueNumber = getLatestGameIssueNumber();
            long interval = Long.parseLong(latestGameIssueNumber) - Long.parseLong(bettingDto.getGameIssueNumber());
            if(interval > 1L) {
                return Optional.empty();
            }
            return Optional.of(bettingDto);
        }
        return Optional.empty();
    }
}
