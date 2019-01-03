package cn.itcast.core.service;

import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentCategory;
import cn.itcast.core.pojo.good.Goods;
import entity.PageResult;
import pojogroup.GoodsVo;

import java.util.List;

public interface GoodsService {
    void add (GoodsVo vo);

    PageResult search (Integer page, Integer rows, Goods goods);

    void updateStatus (Long[] ids, String status);

    GoodsVo findOne (Long id);

    void update (GoodsVo vo);

    void delete (Long[] ids);

    void updateItemStatus (Long[] ids, String status);

    interface ContentCategoryService {

        public List <ContentCategory> findAll ();

        public PageResult findPage (ContentCategory contentCategory, Integer pageNum, Integer pageSize);

        public void add (ContentCategory contentCategory);

        public void edit (ContentCategory contentCategory);

        public ContentCategory findOne (Long id);

        public void delAll (Long[] ids);
    }

    interface ContentService {

        public List <Content> findAll ();

        public PageResult findPage (Content content, Integer pageNum, Integer pageSize);

        public void add (Content content);

        public void edit (Content content);

        public Content findOne (Long id);

        public void delAll (Long[] ids);

        List <Content> findByCategoryId (Long categoryId);
    }
}
