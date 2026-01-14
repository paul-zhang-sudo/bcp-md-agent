package com.bsi.utils;

import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.utils.ExceptionUtils;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtil {
    private static final Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");
    private static final NumberFormat numberFormat;

    static {
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setGroupingUsed(false);
    }

    public static List<List<JSONObject>> readData(String filepath) {
        List<List<JSONObject>> sheetList = new ArrayList<>();
        try {
            // 创建一个文件对象，表示要读取的Excel文件
            File file = new File(filepath);

            // 创建一个文件输入流，将Excel文件读入内存
            FileInputStream inputStream = new FileInputStream(file);

            // 创建一个工作簿对象，表示整个Excel文件
            Workbook workbook = WorkbookFactory.create(inputStream);

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet == null) continue;
                List<JSONObject> dataList = new ArrayList<>();
                // 遍历工作表中的每一行
                for (int j = sheet.getFirstRowNum(); j <= sheet.getLastRowNum(); j++) {
                    Row row = sheet.getRow(j);
                    if (row == null) continue;
                    JSONObject map = new JSONObject();
                    for (int k = 0; k <= row.getLastCellNum(); k++) {
                        // 获取单元格的值
                        Cell cell = row.getCell(k);
                        Object value;
                        if (cell == null) {
                            value = null;
                        } else {
                            CellType cellType = cell.getCellTypeEnum();
                            switch (cellType) {
                                case STRING:
                                    value = cell.getStringCellValue();
                                    break;
                                case NUMERIC:
                                    if (DateUtil.isCellDateFormatted(cell)) {
                                        value = DateUtils.toString(cell.getDateCellValue(), "yyyy-MM-dd HH:mm:ss.SSS");
                                    } else {
                                        double number = cell.getNumericCellValue();
                                        value = numberFormat.format(number);
                                    }
                                    break;
                                case BOOLEAN:
                                    value = cell.getBooleanCellValue();
                                    break;
                                default:
                                    value = "";
                            }
                        }
                        map.put(k + 1 + "", value);
                    }
                    dataList.add(map);
                }
                sheetList.add(dataList);
            }
            // 关闭工作簿和文件输入流
            workbook.close();
            inputStream.close();
        } catch (Exception e) {
            info_log.error(ExceptionUtils.getFullStackTrace(e));
        }
        return sheetList;
    }
}

