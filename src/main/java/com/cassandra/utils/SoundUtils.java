package com.cassandra.utils;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import sun.audio.AudioPlayer;

@Slf4j
public class SoundUtils {

    private SoundUtils() {
    }

    private static void play(String fileName) {
        String currentPath = System.getProperty("user.dir");
        Path path = Paths.get(currentPath, fileName);
        try {
            AudioPlayer.player.start(new FileInputStream(path.toString()));
        }catch (Exception e) {
            log.error("{}", e);
        }
    }

    public static void shortBeep() {
        play("short.wav");
    }

    public static void longBeep() {
        play("long.wav");
    }
}
