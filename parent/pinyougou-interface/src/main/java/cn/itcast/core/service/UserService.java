package cn.itcast.core.service;

import cn.itcast.core.pojo.user.User;
import entity.PageResult;

public interface UserService {
    void sendCode (String phone);

    void add (User user, String smscode);

    PageResult findPage (Integer page, Integer rows, String userName);

    User findOneByUserName (String userName);

    void updateUserDetails (User user);

    User showInfo (String userName);
}
