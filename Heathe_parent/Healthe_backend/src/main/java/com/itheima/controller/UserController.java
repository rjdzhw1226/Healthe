package com.itheima.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.itheima.constant.MessageConstant;
import com.itheima.entity.PasswordBean;
import com.itheima.entity.Result;
import com.itheima.pojo.MemberRegister;
import com.itheima.pojo.User;
import com.itheima.pojo.UserRegister;
import com.itheima.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户相关操作
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Reference
    private UserService userService;

    //获取当前登录用户的用户名
    @RequestMapping("/getUsername")
    public Result getUsername()throws Exception{
        try{
            org.springframework.security.core.userdetails.User user =
                    (org.springframework.security.core.userdetails.User)
                            SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return new Result(true, MessageConstant.GET_USERNAME_SUCCESS,user.getUsername());
        }catch (Exception e){
            return new Result(false, MessageConstant.GET_USERNAME_FAIL);
        }
    }
    //修改密码
    @PreAuthorize("hasAuthority('USER_EDIT')")//权限校验
    @RequestMapping("/changePassword")
    //$2a$10$LPbhiutR34wKvjv3Qb8zBu7piw5hG3.IlQMAI3e/D1Y0DJ/mMSkYa --admin
    //$2a$10$3xW2nBjwBM3rx1LoYprVsemNri5bvxeOd/QfmO7UDFQhW2HRHLi.C --xiaoming
    public Result changePassword(@RequestBody PasswordBean passwordBean)throws Exception{
        try{
            if(passwordBean.getPassword().equals(passwordBean.getPasswordConfirm())){
                User user = userService.findByUserName(passwordBean.getUserName());
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                final String passHash = encoder.encode(passwordBean.getPassword());
                user.setPassword(passHash);
                if("".equals(user.getRemark())||user.getRemark()==null){
                    int i = 1;
                    String sign = String.valueOf(i);
                    user.setRemark(sign);
                }else{
                    user.setRemark(String.valueOf(Integer.valueOf(user.getRemark())+1));
                }
                userService.save(user);
            }else{
                return new Result(false,"修改失败，请重新输入");
            }
            return new Result(true,"修改成功");
        }catch (Exception e){
            return new Result(false, "服务异常，请稍后！");
        }
    }
}
