package com.itheima.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.constant.MessageConstant;
import com.itheima.entity.Result;
import com.itheima.pojo.OrderSetting;
import com.itheima.service.OrderSettingService;
import com.itheima.utils.POIUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 预约设置
 */

@RestController
@RequestMapping("/ordersetting")
public class OrderSettingController {

    @Reference
    private OrderSettingService orderSettingService;

    @RequestMapping("/upload")
    public Result upload(@RequestParam("excelFile")MultipartFile excelFile) {
        try {
            List<String[]> list = POIUtils.readExcel(excelFile);
            List<OrderSetting> data = new ArrayList<>();
            for (String[] strings : list) {
                String date = strings[0];
                String number = strings[1];
                OrderSetting orderSetting = new OrderSetting(new Date(date), Integer.parseInt(number));
                data.add(orderSetting);
            }
            //远程调用服务写入数据库
            orderSettingService.add(data);
            return new Result(true, MessageConstant.IMPORT_ORDERSETTING_SUCCESS);
        } catch (IOException e) {
            e.printStackTrace();
            //文件解析异常
            return new Result(false, MessageConstant.IMPORT_ORDERSETTING_FAIL);
        }
    }

    //根据月份查询返回数据
    @PostMapping("/getOrderSettingByMonth")
    public Result getOrderSettingByMonth(String data){//格式为yyyy-MM
        try{
            List<Map> list = orderSettingService.getOrderSettingByMonth(data);
            return new Result(true,MessageConstant.GET_ORDERSETTING_SUCCESS,list);
        }catch(Exception e){
            e.printStackTrace();
            return new Result(false,MessageConstant.GET_ORDERSETTING_FAIL);
        }
    }

    //根据月份设置预约数据
    @PostMapping("/editNumberByDate")
    public Result editNumberByDate(@RequestBody OrderSetting orderSetting){
        try{
            orderSettingService.editNumberByDate(orderSetting);
            return new Result(true,MessageConstant.ORDERSETTING_SUCCESS);
        }catch(Exception e){
            e.printStackTrace();
            return new Result(false,MessageConstant.ORDERSETTING_FAIL);
        }

    }

}
