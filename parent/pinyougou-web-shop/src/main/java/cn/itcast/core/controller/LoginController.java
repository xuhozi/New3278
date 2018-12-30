package cn.itcast.core.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("login")
public class LoginController {
    @RequestMapping("showName")
    public Map name(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        HashMap<Object, Object> map = new HashMap<>();
        map.put("username",name);
        map.put("curTime",new Date());
        return map;
    }
}
