package com.cassandra.utils;

import com.cassandra.dto.entity.UserInfo;
import com.google.common.collect.Lists;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

@Slf4j
public class UserConfigUtils {

    private UserConfigUtils() {
    }

    public static List<UserInfo> getAllUserConfig() {
        String currentPath = System.getProperty("user.dir");
        String fileName = "config.txt";
        Path path = Paths.get(currentPath, fileName);
        List<String> allLines = Lists.newArrayList();
        try {
            allLines = Files.readAllLines(path);
        }catch (Exception e) {
            log.error("{}", e);
        }
        allLines.forEach(line -> log.info("获取到用户配置 {}", line));
        if(!CollectionUtils.isEmpty(allLines)) {
            return allLines.stream().map(line -> {
                String[] splits = line.split(",");
                return UserInfo.builder().email(splits[0]).token(splits[1]).build();
            }).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }
}
