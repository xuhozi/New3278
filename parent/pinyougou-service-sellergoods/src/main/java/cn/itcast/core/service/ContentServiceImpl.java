package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private ContentDao contentDao;
	@Autowired
	private ItemCatDao itemCatDao;

	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public List<Content> findAll() {
		List<Content> list = contentDao.selectByExample(null);
		return list;
	}

	@Override
	public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<Content> page = (Page<Content>)contentDao.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void add(Content content) {

		contentDao.insertSelective(content);
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}

	/**
	 * 修改
	 * @param content
	 */
	@Override
	public void edit(Content content) {
		//根据id查询一i个广告
		Content c = contentDao.selectByPrimaryKey(content.getId());
		contentDao.updateByPrimaryKeySelective(content);
		if (!c.getCategoryId().equals(content.getCategoryId())){
			//删除原来的缓存
			redisTemplate.boundHashOps("content").delete(c.getCategoryId());
		}
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}

	@Override
	public Content findOne(Long id) {
		Content content = contentDao.selectByPrimaryKey(id);
		return content;
	}

	@Override
	public void delAll(Long[] ids) {
		if(ids != null){
			for(Long id : ids){
				Content content = contentDao.selectByPrimaryKey(id);
				contentDao.deleteByPrimaryKey(id);
				redisTemplate.boundHashOps("content").delete(content.getCategoryId());
			}
		}

	}

    @Override
    public List<Content> findByCategoryId(Long categoryId) {
		List<Content> contents = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
		if(null == contents){

			ContentQuery query = new ContentQuery();
			query.createCriteria().andCategoryIdEqualTo(categoryId)
					.andStatusEqualTo("1");
			query.setOrderByClause("sort_order desc");
			contents = contentDao.selectByExample(query);
			redisTemplate.boundHashOps("content").put(categoryId, contents);
		}
		return contents;


	}

	@Override
	public List<ItemCat> allSortList(Long oneId) {

		List<ItemCat> itemCats = (List<ItemCat>) redisTemplate.boundHashOps("ccc").get(oneId);
		if (itemCats ==null||itemCats.size() ==0) {
			ItemCatQuery itemCatQuery = new ItemCatQuery();
			itemCatQuery.createCriteria().andParentIdEqualTo(oneId);
			List<ItemCat> itemCat = itemCatDao.selectByExample(itemCatQuery);
			for (ItemCat itemCa : itemCat) {
				ItemCatQuery catQuery = new ItemCatQuery();
				catQuery.createCriteria().andParentIdEqualTo(itemCa.getId());
				itemCa.setItemCatList(itemCatDao.selectByExample(catQuery));
			}
			redisTemplate.boundHashOps("ccc").put(oneId,itemCat);

		}
		return itemCats;
	}

}
