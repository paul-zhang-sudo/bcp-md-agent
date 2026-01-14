package com.bsi.utils;

import com.bsi.framework.core.utils.ExceptionUtils;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFRow;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class DbfUtils {
    private static final Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

    public static List<Map<String, Object>> readDbf(String filePath,String charSet,int skipRecords) {
        File dbfFile = new File(filePath);
        List<Map<String, Object>> resultList = new ArrayList<>();
        DBFReader reader = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(dbfFile);
            reader = new DBFReader(fis, Charset.forName(charSet));
            reader.skipRecords(skipRecords);
            DBFRow row;
            int rowNum = 0;

            while ((row = reader.nextRow()) != null) {
                Map<String, Object> rowMap = new LinkedHashMap<>();
                // 加上行号
                rowMap.put("DBF_ROW_NO", ++rowNum);

                for (int i = 0; i < reader.getFieldCount(); i++) {
                    DBFField field = reader.getField(i);
                    rowMap.put(field.getName(), row.getString(field.getName()));
                }
                resultList.add(rowMap);
            }
        }catch (Exception e){
            info_log.info("read DBF file error, msg:{}", ExceptionUtils.getFullStackTrace(e));
        }finally {
            if (reader!=null) {
                reader.close();
            }
            if( fis!=null ){
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
        return resultList;
    }
}
