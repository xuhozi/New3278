package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

/**
 * 搜索管理
 */
@Service
public class ItemsearchServiceImpl implements ItemSearchService {

    //索引库
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    //搜索  入参: 关键词
    public Map<String, Object> search(Map<String, String> searchMap) {

        //        //1:结果集
        //        //2:总条数
        Map<String, Object> map = search1(searchMap);
        //        //3:商品分类
        List<String> categoryList = searchCategoryByKeywords(searchMap);
        map.put("categoryList", categoryList);
        if (null != categoryList && categoryList.size() > 0) {
            //模板ID
            Object typeId = redisTemplate.boundHashOps("itemCats").get(categoryList.get(0));
            //        //4:品牌列表
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
            //        //5:规格列表
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("brandList", brandList);
            map.put("specList", specList);

        }
        return map;

    }

    //查询商品分类 结果集 List<String>
    public List<String> searchCategoryByKeywords(Map<String, String> searchMap) {

        //关键词
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        Query query = new SimpleQuery(criteria);
        //分组 域
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        List<String> categoryList = new ArrayList<>();

        //执行查询  分组
        GroupPage<Item> page = solrTemplate.queryForGroupPage(query, Item.class);

        GroupResult<Item> category = page.getGroupResult("item_category");
        Page<GroupEntry<Item>> groupEntries = category.getGroupEntries();
        List<GroupEntry<Item>> content = groupEntries.getContent();
        for (GroupEntry<Item> itemGroupEntry : content) {
            categoryList.add(itemGroupEntry.getGroupValue());
        }
        return categoryList;
    }

    //        //4:结果集
    //        //5:总条数
    public Map<String, Object> search1(Map<String, String> searchMap) {
     //关键词处理
        searchMap.put("keywords",searchMap.get("keywords").replaceAll(" ",""));

        //关键词
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        HighlightQuery highlightQuery = new SimpleHighlightQuery(criteria);
        //TODO 过滤条件 5
        //商品分类
        if (null != searchMap.get("category") && !"".equals(searchMap.get("category").trim())) {
            FilterQuery filterQuery = new SimpleFilterQuery(new Criteria("item_category").
                    is(searchMap.get("category").trim()));
            highlightQuery.addFilterQuery(filterQuery);
        }
        //品牌
        if (null != searchMap.get("brand") && !"".equals(searchMap.get("brand").trim())) {
            FilterQuery filterQuery = new SimpleFilterQuery(new Criteria("item_brand").
                    is(searchMap.get("brand").trim()));
            highlightQuery.addFilterQuery(filterQuery);
        }
        //定义搜索对象的结构  category:商品分类
        //  $scope.searchMap={'spec':'{网络:3G,..}',pageNo':1,'pageSize':40,'sort':'','sortField':''};

        //规格
   /*     "item_spec_包装": "5瓶",
          "item_spec_酒精度": "55度",
          "item_spec_容量": "2500ml",*/
        if (null != searchMap.get("spec") && !"".equals(searchMap.get("spec"))) {
            Map<String, String> specMap = JSON.parseObject(searchMap.get("spec"), Map.class);
            Set<Map.Entry<String, String>> entries = specMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                FilterQuery filterQuery = new SimpleFilterQuery(new Criteria("item_spec_" + entry.getKey()).
                        is(entry.getValue()));
                highlightQuery.addFilterQuery(filterQuery);
            }
        }

        //价格  0-500 3000-*
        if (null != searchMap.get("price") && !"".equals(searchMap.get("price").trim())) {
            String[] p = searchMap.get("price").trim().split("-");
            FilterQuery filterQuery = null;
            if (searchMap.get("price").trim().contains("*")) {
                //包含*
                filterQuery = new SimpleFilterQuery(
                        new Criteria("item_price").greaterThanEqual(p[0]));
            } else {
                //不包含*
                filterQuery = new SimpleFilterQuery(
                        new Criteria("item_price")
                                .between(p[0], p[1], true, false));
            }
            highlightQuery.addFilterQuery(filterQuery);

        }


        //TODO 排序
        if (null != searchMap.get("sortField") && !"".equals(searchMap.get("sortField"))) {
            if ("DESC".equals(searchMap.get("sort"))) {
                highlightQuery.addSort(new Sort(Sort.Direction.DESC, "item_" + searchMap.get("sortField")));
            } else {

                highlightQuery.addSort(new Sort(Sort.Direction.ASC, "item_" + searchMap.get("sortField")));
            }
        }

        //分页
        String pageNo = searchMap.get("pageNo");
        String pageSize = searchMap.get("pageSize");
        highlightQuery.setOffset((Integer.parseInt(pageNo) - 1) * Integer.parseInt(pageSize));
        //每页数
        highlightQuery.setRows(Integer.parseInt(pageSize));

        //开启高亮
        HighlightOptions highlightOptions = new HighlightOptions();
        //需要高亮的域名
        highlightOptions.addField("item_title");
        //前缀
        highlightOptions.setSimplePrefix("<span style='color:red'>");
        //后缀
        highlightOptions.setSimplePostfix("</span>");
        highlightQuery.setHighlightOptions(highlightOptions);
        //执行查询
        HighlightPage<Item> page = solrTemplate.queryForHighlightPage(highlightQuery, Item.class);

        List<HighlightEntry<Item>> highlighted = page.getHighlighted();
        for (HighlightEntry<Item> highlight : highlighted) {
            Item item = highlight.getEntity();
            List<HighlightEntry.Highlight> highlights = highlight.getHighlights();
            if (null != highlights && highlights.size() > 0) {
                //高亮的名称
                item.setTitle(highlights.get(0).getSnipplets().get(0));
            }
        }

        Map<String, Object> map = new HashMap<>();
        //结果集
        map.put("rows", page.getContent());
        //总条数
        map.put("total", page.getTotalElements());
        //总页数
        map.put("totalPages", page.getTotalPages());
        return map;

    }

}
