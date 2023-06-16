package com.itheima.dao;

import com.github.pagehelper.Page;
import com.itheima.pojo.CheckItem;

import java.util.List;

public interface CheckItemDao {
    public void add(CheckItem checkItem);
    public Page<CheckItem> selectByCondition(String queryString);
    public long findCountByCheckItemId(Integer id);

    public void delectById(Integer id);

    public void edit(CheckItem checkItem);

    public CheckItem findById(Integer id);

    public List<CheckItem> findAll();

}
