package cn.itcast.core.controller;

import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojogroup.SpecificationVo;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("specification")
public class SpecificationController {
    @Reference
    private SpecificationService specificationService;

    @RequestMapping("findAll")
    public List<Specification> findAll() {
        return specificationService.findAll();
    }

    @RequestMapping("search")
    public PageResult search(Integer pageNum, Integer pageSize, @RequestBody Specification specification) {
        return specificationService.search(pageNum, pageSize, specification);
    }

    @RequestMapping("add")
    public Result add(@RequestBody SpecificationVo specificationVo) {

         try {
           specificationService.add(specificationVo);
            return new Result(true,"成功");
         }catch (Exception e){
            return new Result(false,"失败");
         }
    }
    /**
     * 根据id查找实体
     */
    @RequestMapping("findOne")
    public SpecificationVo findOne(Long id){
        SpecificationVo specificationVo=specificationService.findOne(id);
        return specificationVo;
    }

    /**
     * 修改规格表，及规格选项表
     */

    @RequestMapping("update")
    public Result update(@RequestBody SpecificationVo specificationVo){

        try {
            specificationService.update(specificationVo);
            return new Result(true,"成功");
        }catch (Exception e){
            return new Result(false,"失败");
        }
    }
    /**
     * 批量删除
     *
     */
   @RequestMapping("delete")
    public Result delete(Long[] ids){
       try {
           specificationService.delete(ids);
           return new Result(true,"成功");
       }catch (Exception e){
           return new Result(false,"失败");
       }
   }
   @RequestMapping("selectOptionList")
    public List<Map> selectOptionList(){
       return specificationService.selectOptionList();
   }
}
