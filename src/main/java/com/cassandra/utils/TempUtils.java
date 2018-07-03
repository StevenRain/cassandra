package com.cassandra.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TempUtils {

    @Data
    private static class GameData {
        @SerializedName("issue")
        private String gameIssueNo;

        @SerializedName("winningNum")
        private String openCode;

        @SerializedName("kjnsdgjinidf")
        private List<String> openCodes;
    }

    private static List<GameData> getGameDataList() {
        String url = "https://cai33.net/apis/lotIssue/findPic";
        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put("fr", "9");
        headerMap.put("Content-Type", "application/json");
        headerMap.put("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");

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

//    public static void main(String[] args) {
//        List<GameData> gameDataList = getGameDataList();
//        gameDataList = gameDataList.subList(0, 10);
//        gameDataList.sort(comparator);
//        int bigCount = 0;
//        int smallCount = 0;
//        int oddCount = 0;
//        int evenCount = 0;
//        List<String> finalList = Lists.newLinkedList();
//        int total = gameDataList.size();
//        for(GameData gameData : gameDataList) {
//            int sum = gameData.getOpenCodes().stream().mapToInt(Integer::parseInt).sum();
//            String sign = "";
//            if(isBig.test(sum)) {
//                bigCount = bigCount + 1;
//                sign = sign + "大  ";
//            }else {
//                smallCount = smallCount + 1;
//                sign = sign + "小  ";
//            }
//            if(isOdd.test(sum)) {
//                oddCount = oddCount + 1;
//                sign = sign + "单  ";
//            }else {
//                evenCount = evenCount + 1;
//                sign = sign + "双  ";
//            }
//            sign = sign + " " + gameData.getGameIssueNo() + " " + gameData.getOpenCode();
//            finalList.add(sign);
//        }
//        String pattern = "大 %.2f%%, 小 %.2f%%, 单 %.2f%%, 双%.2f%%";
//        double bigPercent = BigDecimal.valueOf((double)bigCount / total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
//        double smallPercent = BigDecimal.valueOf((double)smallCount / total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
//        double oddPercent = BigDecimal.valueOf((double)oddCount / total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
//        double evenPercent = BigDecimal.valueOf((double)evenCount / total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
//        String report1 = String.format(pattern, bigPercent, smallPercent, oddPercent, evenPercent);
//        System.out.println("**************************************************************************");
//        finalList.forEach(System.out::println);
//        System.out.println("**************************************************************************");
//        System.out.println(report1);
//    }
}
