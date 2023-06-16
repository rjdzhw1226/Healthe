package com.itheima.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyDateUtils {
    //计算当前月份有多少天
    public static String MonthJudge(String data){

        String year = data.split("-")[0];
        String month = data.split("-")[1];

        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(new String[]{"1","2","3","4","5","6","7","8","9","10","11","12"}));
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).equals(month)){
                if(i == 0 || i == 2 || i == 4 || i == 6 || i == 7 || i == 9 || i == 11){
                    return "31";
                }else if(i == 1){
                    int IntYear = Integer.parseInt(year);
                    if((0 == IntYear % 4 && IntYear % 100 != 0) || (0 == IntYear % 400)){
                        return "29";
                    }else{
                        return "28";
                    }
                }else{
                    return "30";
                }
            }
        }
        return "-1";

    }
}
