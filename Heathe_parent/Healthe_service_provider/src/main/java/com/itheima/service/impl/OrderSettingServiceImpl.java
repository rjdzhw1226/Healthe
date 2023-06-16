package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.dao.OrderSettingDao;
import com.itheima.pojo.OrderSetting;
import com.itheima.service.OrderSettingService;
import com.itheima.utils.MyDateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 预约设置服务
 */
@Service(interfaceClass = OrderSettingService.class)
@Transactional
public class OrderSettingServiceImpl implements OrderSettingService {

    @Autowired
    private OrderSettingDao orderSettingDao;

    //批量导入预约数据
    @Override
    public void add(List<OrderSetting> list) {
        //判断日期有没有被预约
        if(list != null && list.size() > 0){
            for (OrderSetting orderSetting : list) {
                long countByOrderDate = orderSettingDao.findCountByOrderDate(orderSetting.getOrderDate());
                if(countByOrderDate > 0){
                    //当前日期已经预约
                    orderSettingDao.editNumberByOrderDate(orderSetting);
                }else{
                    //当前日期未预约
                    orderSettingDao.add(orderSetting);
                }
            }
        }
    }

    @Override
    public List<Map> getOrderSettingByMonth(String data) {
        String Begin = data + "-" + "1"; //eg: 2019-3-1
        String End = "";
        String DataY = MyDateUtils.MonthJudge(data);//拿日期
        if(!DataY.equals("-1")){
            End = data + "-" + DataY;
        }else{
            return null;
        }
        Map<String , String> map = new HashMap<>();
        map.put("begin",Begin);
        map.put("end",End);
        //调用Dao层接口查询
        List<OrderSetting> list = orderSettingDao.getOrderSettingByMonth(map);
        List<Map> result = new ArrayList<>();
        if(list != null && list.size() > 0){
            for (OrderSetting orderSetting : list) {
                Map<String , Object> m = new HashMap<>();
                m.put("date", orderSetting.getOrderDate().getDate());
                m.put("number",orderSetting.getNumber());
                m.put("reservations",orderSetting.getReservations());
                result.add(m);
            }
        }
        return result;
    }

    @Override
    public void editNumberByDate(OrderSetting orderSetting) {
        //查询是否日期已经进行了预约设置
        Date orderDate = orderSetting.getOrderDate();
        long count = orderSettingDao.findCountByOrderDate(orderDate);
        if(count > 0){
            //更新操作
            orderSettingDao.editNumberByOrderDate(orderSetting);
        }else{
            //插入操作
            orderSettingDao.add(orderSetting);
        }
    }
}
