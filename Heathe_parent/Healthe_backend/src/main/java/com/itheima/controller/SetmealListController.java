package com.itheima.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.entity.QueryPageBeanDto;
import com.itheima.service.SetmealService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 体检预约列表
 */
@RestController
@RequestMapping("/setmealList")
public class SetmealListController {

    @Reference
    private SetmealService setmealService;

    //查已经预约的全部数据
    @PostMapping("/findPage")
    public PageResult findPage(@RequestBody QueryPageBeanDto queryPageBean){
        return setmealService.pageQueryList(queryPageBean);
    }
}
