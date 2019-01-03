package cn.itcast.core.service;

import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.item.ItemCat;
import entity.PageResult;

import java.util.List;

public interface ContentService {

	public List<Content> findAll();
	
	public PageResult findPage(Content content, Integer pageNum, Integer pageSize);
	
	public void add(Content content);
	
	public void edit(Content content);
	
	public Content findOne(Long id);
	
	public void delAll(Long[] ids);

    List<Content> findByCategoryId(Long categoryId);

	List<ItemCat> allSortList (Long oneId);
}
