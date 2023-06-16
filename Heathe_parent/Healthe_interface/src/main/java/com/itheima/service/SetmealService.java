package com.itheima.service;

import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.entity.QueryPageBeanDto;
import com.itheima.entity.Result;
import com.itheima.pojo.Setmeal;

import java.util.List;
import java.util.Map;

public interface SetmealService {
    public void add(Setmeal setmeal, Integer[] checkgroupIds);

    public PageResult pageQuery(QueryPageBean queryPageBean);

    public Setmeal findById(Integer id);
    public Setmeal findByIdMobile(Integer id);

    public List<Integer> findCheckGroupIdBySetMealId(Integer id);

    public void delete(Integer id);

    public void edit(Setmeal setmeal, Integer[] checkgroupIds);

    public List<Setmeal> findAll();

    /*
    预约列表
     */
    public PageResult pageQueryList(QueryPageBeanDto queryPageBean);

    public List<Map<String, Object>> findSetmealCount();

}
