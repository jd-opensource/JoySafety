package com.jd.security.llmsec.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.base.Joiner;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;



public class FileUtils {
    public static <T> T loadContent(String file, TypeReference<T> type) {
        try {
            String content = loadContent(file);
            return JSON.parseObject(content, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String loadContent(String file) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(file));
        return Joiner.on("\n").join(lines);
    }

}
