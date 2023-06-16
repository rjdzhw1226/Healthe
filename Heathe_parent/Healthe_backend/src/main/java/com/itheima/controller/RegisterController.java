package com.itheima.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.itheima.entity.Result;
import com.itheima.pojo.User;
import com.itheima.pojo.UserRegister;
import com.itheima.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/register")
public class RegisterController {
    @Reference
    private UserService userService;

    @PostMapping("/member")
    public Result register(@RequestBody UserRegister userRegister){
        try{
            if(!userRegister.getPassword().equals(userRegister.getPasswordConfirm())){
                return new Result(false,"密码不一致，注册失败");
            }
            String password = userRegister.getPassword();
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            final String passHash = encoder.encode(password);

            String s = JSON.toJSONString(userRegister);
            User userDTO = JSON.parseObject(s, User.class);
            userDTO.setPassword(passHash);

            String username = userRegister.getUsername();
            User user = userService.findByUserName(username);
            if(user!=null){
                userService.save(userDTO);
            }else{
                userService.add(userDTO);
            }
            return new Result(true,"注册成功！");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false, "服务异常，请稍后！");
        }
    }
}
