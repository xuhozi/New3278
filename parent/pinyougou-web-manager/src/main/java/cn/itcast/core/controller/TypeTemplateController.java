package cn.itcast.core.controller;

import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("typeTemplate")
public class TypeTemplateController {
    @Reference
    private TypeTemplateService typeTemplateService;
    @RequestMapping("search")
    public PageResult search(Integer page, Integer rows, @RequestBody TypeTemplate typeTemplate){
       return typeTemplateService.search(page,rows,typeTemplate);
    }
    @RequestMapping("add")
    public Result add(@RequestBody TypeTemplate typeTemplate){
        try {
            typeTemplateService.add(typeTemplate);
            return new Result(true,"成功");
        }catch (Exception e){
           return new Result(false,"失败");
        }
    }
    /**
     * 根据id查找一个模板管理
     */
    @RequestMapping("findOne")
    public TypeTemplate findOne(Long id){
        return typeTemplateService.findOne(id);
    }
    @RequestMapping("update")
    public Result update(@RequestBody TypeTemplate typeTemplate){
        try {
            typeTemplateService.update(typeTemplate);
            return new Result(true,"成功");
        }catch (Exception e){
            return new Result(false,"失败");
        }
    }
    /**
     * 批量删除
     */
    @RequestMapping("delete")
    public Result delete(Long[] ids){
        try {
            typeTemplateService.delete(ids);
            return new Result(true,"成功");
        }catch (Exception e){
            return new Result(false,"失败");
        }
    }

    //运营商后台-模板审核
    @RequestMapping("updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            typeTemplateService.updateStatus(ids, status);
            return new Result(true, "审核通过");
        } catch (Exception e) {
            return new Result(false, "审核失败");
        }
    }
}
