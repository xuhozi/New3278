package pojogroup;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class OrderVoo implements Serializable{
    private Date createTime;

    private String goods;
    private Integer sales;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderVoo orderVoo = (OrderVoo) o;
        return Objects.equals(createTime, orderVoo.createTime) &&
                Objects.equals(goods, orderVoo.goods);
    }

    @Override
    public int hashCode() {

        return Objects.hash(createTime, goods);
    }

    public Date getCreateTime() {

        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getGoods() {
        return goods;
    }

    public void setGoods(String goods) {
        this.goods = goods;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }
    //时间段 销售额 商品



}
