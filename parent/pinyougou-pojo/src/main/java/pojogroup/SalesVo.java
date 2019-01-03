package pojogroup;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class SalesVo implements Serializable{
    private Date day;
    private Integer pay;

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public Integer getPay() {
        return pay;
    }

    public void setPay(Integer pay) {
        this.pay = pay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SalesVo SalesVo = (SalesVo) o;
        return Objects.equals(day, SalesVo.day);

    }

    @Override
    public int hashCode() {

        return Objects.hash(day);
    }
}
