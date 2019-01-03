app.service('orderService', function ($http) {
        this.findAll = function () {
            return $http.get('../order/findAll.do');
        }
        //分页
        this.findPage = function (page, rows) {
            return $http.get('../order/findPage.do?page=' + page + '&rows=' + rows);
        }
        //查询实体
        this.findOne = function (id) {
            return $http.get('../order/findOne.do?id=' + id);
        }

        this.search = function (page, rows, searchEntity) {
            return $http.post('../order/search.do?page=' + page + "&rows=" + rows, searchEntity);
        }
        //查询订单数
        this.findCount = function () {
            return $http.get('../order/findCount.do');
        }

        this.importData = function () {
            return $http.get("../order/importData.do");
        }

    }
)