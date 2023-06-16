package com.itheima.dao;

import com.itheima.pojo.User;

public interface UserDao {

    public User findByUserName(String userName);

    public void save(User user);

    public void add(User userDTO);

}
