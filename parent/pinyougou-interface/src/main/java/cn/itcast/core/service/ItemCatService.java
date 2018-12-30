package cn.itcast.core.service;

import cn.itcast.core.pojo.item.ItemCat;
import entity.PageResult;

import java.util.List;

public interface ItemCatService {
    List<ItemCat> ItemCatService(Long parentId);

    PageResult search(Integer page, Integer rows, ItemCat itemCat);

    void add(ItemCat itemCat);

    ItemCat findOne(Long id);

    List<ItemCat> findAll();
}
