package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("itemCat")
public class ItemCatController {
    @Reference
    private ItemCatService itemCatService;

    @RequestMapping("findByParentId")
    public List<ItemCat> ItemCatService(Long parentId) {
        return itemCatService.ItemCatService(parentId);
    }
    @RequestMapping("search")
    public PageResult search(Integer page, Integer rows, @RequestBody ItemCat itemCat){

        return itemCatService.search(page,rows,itemCat);
    }
    /**
     * 添加
     */
    @RequestMapping("add")
    public Result add(@RequestBody ItemCat itemCat){
        try {
            itemCatService.add(itemCat);
return new Result(true,"成功");
        }catch (Exception e){
            return new Result(false,"失败");
        }
    }
    /**
     * 查询所有
     */
    @RequestMapping("findAll")
    public List<ItemCat> findAll(){
        return itemCatService.findAll();

    }
}
