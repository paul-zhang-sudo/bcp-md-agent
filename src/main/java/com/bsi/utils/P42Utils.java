package com.bsi.utils;

import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.utils.ExceptionUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 42号文工具类
 */
public class P42Utils {
    private static final Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");
    private static final String LINE_DELIMITER = "~";
    private static final String FIELD_DELIMITER = ";";
    private static final String EOF_MARKER = "||";

    public static List<JSONObject> readData(String filePath) {
        return readData(filePath,"UTF-8");
    }

    public static List<JSONObject> readData(String filePath,String encoding) {

        List<JSONObject> dataList = new ArrayList<>();
        try {
            List<String> lines= IOUtils.readLines(Files.newInputStream(Paths.get(filePath)),encoding);
            for(String line : lines) {
                String[] ls = line.split(LINE_DELIMITER);
                for (String l : ls) {
                    if (l.trim().isEmpty() || l.equals(EOF_MARKER)) {
                        continue;
                    }
                    String[] fields = l.split(FIELD_DELIMITER);
                    JSONObject data = new JSONObject(true);
                    int i = 0;
                    for (String f : fields) {
                        data.put(++i + "", f);
                    }
                    dataList.add(data);
                }
            }
        } catch (IOException e) {
            info_log.error(ExceptionUtils.getFullStackTrace(e));
        }
        return dataList;
    }
}
