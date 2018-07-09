package com.cassandra.utils;

public class GlobalParameter {

    /**
     * 连续出现次数小于30%
     * */
    public static final double THRESHOLD = 0.3;

    /**
     * 连续出现3期
     * */
    public static final int CONTINUE_ISSUES = 3;

    /**
     * 只用50%的余额投注，防止一次性亏光
     * */
    public static final double KEEP_BALANCE_RATIO = 0.5;

    /**
     * 最多跟n期
     * */
    public static final int MAX_FOLLOWING_ISSUES = 1;
}
