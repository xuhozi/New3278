package cn.itcast.core.service;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
public class SellerServiceImpl implements SellerService {
    @Autowired
    private SellerDao sellerDao;

    @Override
    public void add(Seller seller) {

        //未审核的商品
        seller.setStatus("0");
        //商品注册的时间
        seller.setCreateTime(new Date());
        //密码加密
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        encoder.encode(seller.getPassword());
        seller.setPassword(encoder.encode(seller.getPassword()));
        sellerDao.insertSelective(seller);
    }

    @Override
    public PageResult search(Integer page, Integer rows, Seller seller) {
        PageHelper.startPage(page, rows);
        Page<Seller> p;
        //查询未审核状态
        SellerQuery sellerQuery = new SellerQuery();
        if (null!=seller.getStatus()){
            sellerQuery.createCriteria().andStatusEqualTo(seller.getStatus());
        }
        p = (Page<Seller>) sellerDao.selectByExample(sellerQuery);
        return new PageResult(p.getTotal(), p.getResult());
    }

    @Override
    public Seller findOne(String id) {
        return sellerDao.selectByPrimaryKey(id);
    }

    @Override
    public void updateStatus(String sellerId, String status) {
        Seller seller = new Seller();
        seller.setSellerId(sellerId);
        seller.setStatus(status);
        sellerDao.updateByPrimaryKeySelective(seller);
    }
}