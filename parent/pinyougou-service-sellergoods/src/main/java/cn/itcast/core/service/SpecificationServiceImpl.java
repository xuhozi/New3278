package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import pojogroup.SpecificationVo;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {
    @Autowired
    private SpecificationDao specificationDao;
    @Autowired
    private SpecificationOptionDao specificationOptionDao;

    @Override
    public List<Specification> findAll() {
        return specificationDao.selectByExample(null);
    }

    @Override
    public PageResult search(Integer pageNum, Integer pageSize, Specification specification) {
        PageHelper.startPage(pageNum, pageSize);
        //判断是否有条件需要查询
        SpecificationQuery specificationQuery = new SpecificationQuery();
        if (null != specification) {
            SpecificationQuery.Criteria criteria = specificationQuery.createCriteria();

            if (null != specification.getSpecName() && !"".equals(specification.getSpecName().trim())) {
                //根据名字进行模糊查询
                criteria.andSpecNameLike("%" + specification.getSpecName().trim() + "%");
            }

        }
        Page<Specification> p = (Page<Specification>) specificationDao.selectByExample(specificationQuery);

        return new PageResult(p.getTotal(), p.getResult());
    }

    @Override
    public void add(SpecificationVo specificationVo) {
        //规格表返回ID
        specificationDao.insertSelective(specificationVo.getSpecification());
        //规格选项表结果集
        List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();
        for (SpecificationOption specificationOption : specificationOptionList) {
            //外键
            specificationOption.setSpecId(specificationVo.getSpecification().getId());
            //保存规格选项
            specificationOptionDao.insertSelective(specificationOption);
        }
    }

    @Override
    public SpecificationVo findOne(Long id) {
        SpecificationVo specificationVo = new SpecificationVo();
        Specification specification = specificationDao.selectByPrimaryKey(id);
        specificationVo.setSpecification(specification);
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<SpecificationOption> specificationOptionList = specificationOptionDao.selectByExample(specificationOptionQuery);
        specificationVo.setSpecificationOptionList(specificationOptionList);
        return specificationVo;
    }

    @Override
    public void delete(Long[] ids) {
        SpecificationQuery specificationQuery = new SpecificationQuery();
        SpecificationQuery.Criteria criteria = specificationQuery.createCriteria();
        criteria.andIdIn(asList(ids));
        specificationDao.deleteByExample(specificationQuery);
    }

    @Override
    public void update(SpecificationVo specificationVo) {
        //更新规格表
        specificationDao.updateByPrimaryKeySelective(specificationVo.getSpecification());
        //先删除规格选项表-->根据规格id删除规格选项表
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
        criteria.andSpecIdEqualTo(specificationVo.getSpecification().getId());
        specificationOptionDao.deleteByExample(specificationOptionQuery);
        //再保存规格属性表
        List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();
        for (SpecificationOption specificationOption : specificationOptionList) {
            //外键
            specificationOption.setSpecId(specificationVo.getSpecification().getId());
            specificationOptionDao.insertSelective(specificationOption);
        }
    }

    @Override
    public List<Map> selectOptionList() {
        return specificationDao.selectOptionList();
    }

    /*@Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination topicPageAndSolrDestination;*/

    //运营商后台-规格审核
    @Override
    public void updateStatus(Long[] ids, String status) {
        Specification specification = new Specification();
        specification.setAuditStatus(status);
        //规格表ID
        for (Long id : ids) {
            specification.setId(id);
            //1:规格状态更改
            specificationDao.updateByPrimaryKeySelective(specification);
            /*//判断一定是通过
            if ("1".equals(status)) {
                //发消息
                jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage(String.valueOf(id));
                    }
                });
            }*/
        }
    }
}
