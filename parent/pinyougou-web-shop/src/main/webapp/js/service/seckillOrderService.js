app.service('seckillOrderService', function ($http) {
        this.findAll = function () {
            return $http.get('../seckillOrder/findAll.do');
        }
        //分页
        this.findPage = function (page, rows) {
            return $http.get('../seckillOrder/findPage.do?page=' + page + '&rows=' + rows);
        }
        //查询实体
        this.findOne = function (id) {
            return $http.get('../seckillOrder/findOne.do?id=' + id);
        }

        this.search = function (page, rows, searchEntity) {
            return $http.post('../seckillOrder/search.do?page=' + page + "&rows=" + rows, searchEntity);
        }
        //查询订单数
        this.findCount = function () {
            return $http.get('../seckillOrder/findCount.do');
        }
    }
)