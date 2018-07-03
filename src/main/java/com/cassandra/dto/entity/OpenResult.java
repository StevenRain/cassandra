package com.cassandra.dto.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenResult {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OpenResultDto {
        /**
         * 期号
         * */
        private String gameIssueNo;

        /**
         * 开奖号码
         * */
        private String openCode;

        /**
         * 大小属性
         * */
        private String bigOrSmall;

        /**
         *  单双属性
         * */
        private String oddOrEven;
    }

    /**
     * 大占比
     * */
    private double bigRatio;

    /**
     * 小占比
     * */
    private double smallRatio;

    /**
     * 单占比
     * */
    private double oddRatio;

    /**
     * 双占比
     * */
    private double evenRatio;

    private List<OpenResultDto> openResultDtoList;
}
