package com.itheima.entity;

/**
 * 封装密码条件
 */
public class PasswordBean {
    private String Password;//密码
    private String PasswordConfirm;//确认密码
    private String userName;//用户名

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getPasswordConfirm() {
        return PasswordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        PasswordConfirm = passwordConfirm;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
