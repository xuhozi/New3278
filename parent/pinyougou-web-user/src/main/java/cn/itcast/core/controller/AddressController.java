package cn.itcast.core.controller;


import cn.itcast.core.pojo.address.Address;
import cn.itcast.core.service.AddressService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {

    @Reference
    private AddressService addressService;

    @RequestMapping("/findListByLoginUser")
    public List<Address> findListByLoginUser(){
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return addressService.findListByLoginUser(userName);
    }

    @RequestMapping("/add")
    public Result add(@RequestBody Address address){
        try {
            address.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
            addressService.add(address);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }

    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            addressService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }
}
