package com.itheima.dao;

import com.github.pagehelper.Page;
import com.itheima.pojo.Syslog;

public interface ISysLogDao {

    void insert(Syslog syslog);

    Page<Syslog> selectAll(String queryString);

    void deleteById(String id);
}
