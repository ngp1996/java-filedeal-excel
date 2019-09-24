package cn.com.victorysoft.vseaf.ExcelImport.service;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelReader {
    //excel格式要求：本类中的方法并没有对单元格合并的情况进行处理，而且要求第一行必须为标题
    //这里使用xssf和hssf的父类来定义excel
    Workbook wb;
    Row row;
    Sheet sheet;

    public List<Map<Integer, String>> readExcelContentByList(InputStream is) {

        List<Map<Integer, String>> list = new ArrayList<Map<Integer,String>>();
        //这里同时处理2003和2007版本的excel
        try {
            wb  = new XSSFWorkbook(is); // 解析2007版本
        } catch (Exception ex) {
            //logger.info("importData------------------------>XSSFWorkbook Read InputStream Exception");
            System.out.println("importData------------------------>XSSFWorkbook Read InputStream Exception");
            try {
                POIFSFileSystem pfs = new POIFSFileSystem(is); // 解析2003版本
                wb = new HSSFWorkbook(pfs);
            } catch (IOException e) {
               // logger.info("importData------------------------>HSSFWorkbook Read InputStream Exception");
                System.out.println("importData------------------------>HSSFWorkbook Read InputStream Exception");
            }
        }

        sheet = wb.getSheetAt(0);
        // 得到总行数
        int rowNum = sheet.getLastRowNum();
         row = sheet.getRow(1);
        int colNum = row.getPhysicalNumberOfCells();
        // 正文内容应该从第二行开始,第一行为表头的标题
        for (int i = 2; i <= rowNum; i++) {
            row = sheet.getRow(i);
            int j = 0;
            Map<Integer,String> map = new HashMap<Integer, String>();
            while (j < colNum) {
                // 每个单元格的数据内容用"-"分割开，以后需要时用String类的replace()方法还原数据
                // 也可以将每个单元格的数据设置到一个javabean的属性中，此时需要新建一个javabean
                // str += getStringCellValue(row.getCell((short) j)).trim() +
                // "-";
                //map.put(j, getCellFormatValue(row.getCell((short) j)).trim().replaceAll("\t\r", ""));
                map.put(j, getCellFormatValue(row.getCell((short) j)));
                //str += getCellFormatValue(row.getCell((short) j)).trim() + "    ";
                j++;
            }
            list.add(map);
        }
        return list;
    }

    private String getCellFormatValue(Cell cell) {
        String cellvalue = "";
        if (cell != null) {
            // 判断当前Cell的Type
            switch (cell.getCellType()) {
                // 如果当前Cell的Type为NUMERIC
                case Cell.CELL_TYPE_NUMERIC: {
                    short format = cell.getCellStyle().getDataFormat();
                    if(format == 14 || format == 31 || format == 57 || format == 58){ 	//excel中的时间格式
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        double value = cell.getNumericCellValue();
                        Date date = DateUtil.getJavaDate(value);
                        cellvalue = sdf.format(date);
                    }
                    // 判断当前的cell是否为Date
                    else if (DateUtil.isCellDateFormatted(cell)) {  //先注释日期类型的转换，在实际测试中发现HSSFDateUtil.isCellDateFormatted(cell)只识别2014/02/02这种格式。
                        // 如果是Date类型则，取得该Cell的Date值           // 对2014-02-02格式识别不出是日期格式
                        Date date = cell.getDateCellValue();
                        DateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
                        cellvalue= formater.format(date);
                    } else { // 如果是纯数字
                        // 取得当前Cell的数值
                        cellvalue = NumberToTextConverter.toText(cell.getNumericCellValue());

                    }
                    break;
                }
                // 如果当前Cell的Type为string
                case Cell.CELL_TYPE_STRING:
                    // 取得当前的Cell字符串
                    cellvalue = cell.getStringCellValue().replaceAll("'", "''");
                    break;
                case Cell.CELL_TYPE_BLANK:
                    cellvalue = null;
                    break;
                // 默认的Cell值
                default:{
                    cellvalue = "default";
                }
            }
        } else {
            cellvalue = "";
        }
        return cellvalue;

    }

}

