package cn.itcast.core.service;

import cn.itcast.core.dao.address.AddressDao;
import cn.itcast.core.dao.address.AreasDao;
import cn.itcast.core.dao.address.CitiesDao;
import cn.itcast.core.dao.address.ProvincesDao;
import cn.itcast.core.pojo.address.Address;
import cn.itcast.core.pojo.address.AddressQuery;
import cn.itcast.core.pojo.address.Cities;
import cn.itcast.core.pojo.address.Provinces;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 查询当前用户地址信息
 */
@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressDao addressDao;

    @Autowired
    private AreasDao areasDao;

    @Autowired
    private CitiesDao citiesDao;

    @Autowired
    private ProvincesDao provincesDao;

    @Override
    public List<Address> findListByLoginUser(String name) {
        AddressQuery addressQuery=new AddressQuery();
        addressQuery.createCriteria().andUserIdEqualTo(name);
        List<Address> addresses = addressDao.selectByExample(addressQuery);
        for (Address address : addresses) {
            address.setProvinceId(provincesDao.selectByPrimaryKey(Integer.valueOf(address.getProvinceId())).getProvince());
            address.setCityId(citiesDao.selectByPrimaryKey(Integer.valueOf(address.getCityId())).getCity());
            address.setTownId(areasDao.selectByPrimaryKey(Integer.valueOf(address.getTownId())).getArea());
        }
        return addresses;
    }

    @Override
    public void add(Address address) {
        addressDao.insertSelective(address);
    }

    @Override
    public void delete(Long[] ids) {

    }
}
