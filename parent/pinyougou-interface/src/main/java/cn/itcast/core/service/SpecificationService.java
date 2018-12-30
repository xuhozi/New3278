package cn.itcast.core.service;

import cn.itcast.core.pojo.specification.Specification;
import entity.PageResult;
import pojogroup.SpecificationVo;

import java.util.List;
import java.util.Map;

public interface SpecificationService {
    List<Specification> findAll();

    PageResult search(Integer pageNum, Integer pageSize, Specification specification);

    void add(SpecificationVo specificationVo);

    SpecificationVo findOne(Long id);

    void delete(Long[] ids);

    void update(SpecificationVo specificationVo);

    List<Map> selectOptionList();

    //运营商后台-规格审核
    void updateStatus(Long[] ids, String status);
}

