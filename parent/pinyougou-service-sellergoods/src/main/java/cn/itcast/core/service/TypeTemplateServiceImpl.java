package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

@Service
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {
    @Autowired
    private SpecificationOptionDao specificationOptionDao;
    @Autowired
    private TypeTemplateDao typeTemplateDao;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate) {
        //去数据库中查询所有的模板对象
        List<TypeTemplate> typeTemplateList = typeTemplateDao.selectByExample(null);
        for (TypeTemplate template : typeTemplateList) {
            //将上面查询出来的结果放到缓存（hash）
            String brandIds = template.getBrandIds();
            List<Map> brandList = JSON.parseArray(brandIds, Map.class);
            redisTemplate.boundHashOps("brandList").put(template.getId(), brandList);
            List<Map> specList = findBySpecList(template.getId());
            redisTemplate.boundHashOps("specList").put(template.getId(), specList);
        }


        PageHelper.startPage(page, rows);
        PageHelper.orderBy("id desc");
        //根据条件进行搜索
        TypeTemplateQuery typeTemplateQuery = new TypeTemplateQuery();
        if (null != typeTemplate) {
            TypeTemplateQuery.Criteria criteria = typeTemplateQuery.createCriteria();
            if (null != typeTemplate.getName() && !"".equals(typeTemplate.getName().trim())) {
                criteria.andNameLike("%" + typeTemplate.getName().trim() + "%");
            }
        }
        Page<TypeTemplate> p = (Page<TypeTemplate>) typeTemplateDao.selectByExample(typeTemplateQuery);
        return new PageResult(p.getTotal(), p.getResult());
    }

    @Override
    public void add(TypeTemplate typeTemplate) {
        typeTemplateDao.insertSelective(typeTemplate);
    }

    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(TypeTemplate typeTemplate) {
        typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);
    }

    @Override
    public void delete(Long[] ids) {
        //创建条件对象
        TypeTemplateQuery typeTemplateQuery = new TypeTemplateQuery();
        TypeTemplateQuery.Criteria criteria = typeTemplateQuery.createCriteria();
        criteria.andIdIn(asList(ids));
        typeTemplateDao.deleteByExample(typeTemplateQuery);
    }

    @Override
    public List<Map> findBySpecList(Long id) {
        //查询模板
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        String specIds = typeTemplate.getSpecIds();
        List<Map> listMap = JSON.parseArray(specIds, Map.class);
        for (Map map : listMap) {
            //查询规格选项列表
            SpecificationOptionQuery query = new SpecificationOptionQuery();
            query.createCriteria().andSpecIdEqualTo((long) (Integer) map.get("id"));
            List<SpecificationOption> specificationOptionList = specificationOptionDao.selectByExample(query);
            map.put("options", specificationOptionList);
        }
        return listMap;
    }


    /*@Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination topicPageAndSolrDestination;*/

    //运营商后台-模板审核
    @Override
    public void updateStatus(Long[] ids, String status) {
        TypeTemplate typeTemplate = new TypeTemplate();
        typeTemplate.setAuditStatus(status);
        //模板表ID
        for (Long id : ids) {
            typeTemplate.setId(id);
            //1:模板审核状态更改
            typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);
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
