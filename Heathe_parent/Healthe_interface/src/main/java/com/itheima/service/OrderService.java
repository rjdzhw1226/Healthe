package com.itheima.service;

import com.itheima.entity.Result;

import java.util.Map;

public interface OrderService {
    //体检预约
    public Result order(Map map);

    public Map findById(Integer id);

}
