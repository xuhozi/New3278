package cn.itcast.core.controller;

import cn.itcast.common.utils.PhoneFormatCheckUtils;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Reference
    private UserService userService;
    //发短信验证码
    @RequestMapping("sendCode")
    public Result sendCode(String phone){

        //判断手机号格式是否合法
        try {
            if (PhoneFormatCheckUtils.isPhoneLegal(phone)){
                //fa验证码
                userService.sendCode(phone);
                return new Result(true,"成功");
            }else {
                return new Result(false,"手机号不合法");
            }
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"失败");

        }

    }

    @RequestMapping("add")
    public Result add(@RequestBody User user, String smscode){


        try {
            userService.add(user,smscode);
            return new Result(true,"注册成功");

        }catch (RuntimeException e){
            return new Result(false,e.getMessage());
        } catch (Exception e){
            e.printStackTrace();
            return new Result(false,"注册失败");

        }

    }


}
