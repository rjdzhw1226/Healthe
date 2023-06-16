package com.itheima.test;




import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class POITest {
//    @Test
//    public void test1(){
//        try {
//            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(new FileInputStream(new File("D:\\")));
//            XSSFSheet sheetAt = xssfWorkbook.getSheetAt(0);
//            //1
//            int lastRowNum = sheetAt.getLastRowNum();
//            for (int i = 0; i <= lastRowNum; i++) {
//                XSSFRow row = sheetAt.getRow(i);
//                short lastCellNum = row.getLastCellNum();
//                for (int j = 0; j < lastCellNum; j++) {
//                    XSSFCell cell = row.getCell(i);
//                    System.out.println(cell);
//                }
//            }
//            //2
//            for (Row row : sheetAt) {
//                for (Cell cell : row) {
//                    //StringUtils.isNumber(String.valueOf(cell))
//                    System.out.println(cell.getStringCellValue());
//                }
//            }
//            xssfWorkbook.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
