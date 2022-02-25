package com.bsi.utils;

import com.bsi.framework.core.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 文件工具类
 */
@Slf4j
public class FileUtils {

    /**
     * 读取文件内容
     * @param path
     * @return
     */
    public static List<String> readFile(String path){
        List<String> resultLines = null;
        try{
            resultLines = org.apache.commons.io.FileUtils.readLines(new File(path),StandardCharsets.UTF_8);
        }catch (Exception e){
            log.error("读取文件{}报错,错误信息:", ExceptionUtils.getFullStackTrace(e));
        }
        return resultLines;
    }

    /**
     * 读取文件内容
     * @param path
     * @return
     */
    public static boolean writeFile(String path,String msg,boolean append){
        boolean flag = true;
        List<String> resultLines = null;
        try{
            org.apache.commons.io.FileUtils.write(new File(path),msg, StandardCharsets.UTF_8,append);
        }catch (Exception e){
            flag = false;
            log.error("写入文件{}报错,错误信息:", ExceptionUtils.getFullStackTrace(e));
        }
        return flag;
    }
}
