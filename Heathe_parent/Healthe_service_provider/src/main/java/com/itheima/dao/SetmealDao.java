package com.itheima.dao;

import com.github.pagehelper.Page;
import com.itheima.pojo.Order;
import com.itheima.pojo.Setmeal;
import org.apache.ibatis.annotations.MapKey;

import java.util.List;
import java.util.Map;

public interface SetmealDao {
    public void add(Setmeal setmeal);

    public void setSetmealAndCheckGroup(Map<String, Integer> map);

    public Page<Setmeal> findByCondition(String queryString);

    public Setmeal findById(Integer id);

    public List<Integer> findCheckGroupIdBySetMealId(Integer id);

    public void deleteAssoication(Integer id);

    public void deleteById(Integer id);

    public void edit(Setmeal setmeal);

    public List<Setmeal> findAll();

    public Setmeal findByIdMobile(Integer id);

    public Page<Order> findByConditionList(Integer queryString);

    public Page<Order> findByConditionList1(Integer queryString);

    @MapKey("id")
    public List<Map<String, Object>> findSetmealCount();
    //查询套餐名称
    List<String> findSetmealNameById(Integer setmealId);

}
