package com.cassandra.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SleepUtils {

    private SleepUtils() {
    }

    public static void sleepMilliseconds(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        }catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
    }
}
