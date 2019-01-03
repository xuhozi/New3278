app.controller('seckillOrderController', function ($scope, $controller, $location, seckillOrderService) {
    $controller('baseController', {$scope: $scope});//继承

    $scope.findAll = function () {
        seckillOrderService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }


    $scope.findPage = function (page, rows) {
        seckillOrderService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    $scope.findOne = function () {
        var id = $location.search()['id'];
        if (null == id) {
            return;
        }
        seckillOrderService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        )
    }

    $scope.searchEntity = {};//定义搜索对象
    $scope.type = [" ", "在线支付", "货到付款"];
    $scope.status = ["未付款", "已付款", "未发货", "已发货", "交易成功", "交易关闭", "待评价"];
    //搜索
    $scope.search = function (page, rows) {
        seckillOrderService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询订单数
    $scope.findCount = function () {
        seckillOrderService.findCount().success(
            function (response) {
                $scope.count = response;
            }
        );
    }
})