package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {
    @Autowired
    private ItemCatDao itemCatDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public List<ItemCat> ItemCatService(Long parentId) {
        //去数据库查询所有分类
        List<ItemCat> itemCats = findAll();
        for (ItemCat itemCat : itemCats) {
            //将所有的分类放到缓存（hash）
            redisTemplate.boundHashOps("itemCats").put(itemCat.getName(),itemCat.getTypeId());
        }

        //根据条件进行查询
        ItemCatQuery itemCatQuery = new ItemCatQuery();
        ItemCatQuery.Criteria criteria = itemCatQuery.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        return itemCatDao.selectByExample(itemCatQuery);
    }

    @Override
    public PageResult search(Integer page, Integer rows, ItemCat itemCat) {
        PageHelper.startPage(page,rows);
        ItemCatQuery itemCatQuery = new ItemCatQuery();
        ItemCatQuery.Criteria criteria = itemCatQuery.createCriteria();
        criteria.andParentIdEqualTo(itemCat.getParentId());
        Page<ItemCat> p= (Page<ItemCat>) itemCatDao.selectByExample(itemCatQuery);
        return new PageResult(p.getTotal(),p.getResult());
    }

    @Override
    public void add(ItemCat itemCat) {

        itemCatDao.insertSelective(itemCat);
    }

    @Override
    public ItemCat findOne(Long id) {

        return itemCatDao.selectByPrimaryKey(id);
    }

    @Override
    public List<ItemCat> findAll() {

        return itemCatDao.selectByExample(null);
    }
}
