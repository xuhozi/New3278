package cn.itcast.core.service.seckill;
import cn.itcast.core.dao.seckill.SeckillGoodsDao;
import cn.itcast.core.pojo.seckill.SeckillGoods;
import cn.itcast.core.pojo.seckill.SeckillGoodsQuery;
import cn.itcast.core.service.SeckillGoodsService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Scheduled;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Date;
import java.util.List;


/**
 * 秒杀商品信息  LH
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsDao seckillGoodsDao;

    /**
     * 查询秒杀商品
     * @param page
     * @param rows
     * @param seckillGoods
     * @return
     */
    @Override
    public PageResult search (Integer page, Integer rows, SeckillGoods seckillGoods) {
        PageHelper.startPage (page, rows);
//        SeckillOrderQuery seckillOrderQuery = new SeckillOrderQuery ();
        SeckillGoodsQuery seckillGoodsQuery = new SeckillGoodsQuery ();

        Page<SeckillGoods> o = (Page <SeckillGoods>) seckillGoodsDao.selectByExample (null);

        return new PageResult (o.getTotal (), o.getResult ());
    }

    /**
     * 更新秒杀商品状态
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus (Long[] ids, String status) {
        SeckillGoods seckillGoods = new SeckillGoods ();
        seckillGoods.setStatus (status);
        seckillGoods.setCheckTime (new Date ());
        //秒杀商品表ID
        for (Long id : ids) {
            seckillGoods.setId (id);
            //更改秒杀商品的状态id
            seckillGoodsDao.updateByPrimaryKeySelective (seckillGoods);
            //通过之后发送MQ(不发了)

        }

    }
    @Override
    public List<SeckillGoods> findList() {
        List<SeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
        if (seckillGoodsList == null || seckillGoodsList.size() == 0) {
            SeckillGoodsQuery query = new SeckillGoodsQuery();
            SeckillGoodsQuery.Criteria criteria = query.createCriteria();
            criteria.andStatusEqualTo("1");
            criteria.andNumGreaterThan(0);
            criteria.andStartTimeLessThanOrEqualTo(new Date());
            criteria.andEndTimeGreaterThan(new Date());
            seckillGoodsList = seckillGoodsDao.selectByExample(query);
            if (seckillGoodsList != null && seckillGoodsList.size() > 0) {
                for (SeckillGoods seckillGoods : seckillGoodsList) {
                    redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
                }
            }
            return seckillGoodsList;

        }else{
            //从缓存中取出记录
            for (SeckillGoods seckillGoods : seckillGoodsList) {
                Date endTime = seckillGoods.getEndTime();
                if (endTime.compareTo(new Date()) == -1 || seckillGoods.getNum() == 0) {
                    redisTemplate.boundHashOps("seckillGoods").delete(seckillGoods.getId());
                    seckillGoodsList.remove(seckillGoods);
                }
            }
            return seckillGoodsList;

        }

    }



    @Override
    public SeckillGoods findOneFromRedis(Long id) {
        return (SeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(id);
    }


    /**
     * 秒杀商品增量同步
     */
    @Scheduled(cron = "* 0/10 * * * *")//要执行的时间 每间隔10分钟执行一次
    private void redisSynchronizeFromDb() {
        SeckillGoodsQuery skgQuery = new SeckillGoodsQuery();
        SeckillGoodsQuery.Criteria criteria = skgQuery.createCriteria();
        criteria.andStartTimeLessThanOrEqualTo(new Date());
        criteria.andEndTimeGreaterThan(new Date());
        criteria.andNumGreaterThan(0);
        criteria.andStatusEqualTo("1");
        //获取到集合
        List<SeckillGoods> latestSeckillGoodsList = seckillGoodsDao.selectByExample(skgQuery);
        redisTemplate.delete("seckillGoods");
        for (SeckillGoods seckillGoods : latestSeckillGoodsList) {
            redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
        }
    }
}
