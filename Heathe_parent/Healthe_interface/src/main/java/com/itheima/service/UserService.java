package com.itheima.service;

import com.itheima.pojo.User;

public interface UserService {
    public User findByUserName(String userName);

    public void save(User user);

    public void add(User userDTO);

}
