app.controller('orderController' ,function($scope,$controller,$location,orderService){
    $controller('baseController',{$scope:$scope});//继承

    $scope.findAll=function(){
        orderService.findAll().success(
            function(response){
                $scope.list=response;
            }
        );
    }

    $scope.zhexian={};




    $scope.findPage=function(page,rows){
        orderService.findPage(page,rows).success(
            function(response){
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }

    $scope.findOne=function() {
        var id = $scope.searchEntity()['orderId'];
        if (null == id) {
            return;
        }
        orderService.findOne(id).success(
            function(response) {
                $scope.entity = response;
            }
            )
    }

    $scope.searchEntity={};//定义搜索对象
    $scope.type = [" ","在线支付","货到付款"];
    $scope.status = ["未付款","已付款","未发货","已发货","交易成功","交易关闭","待评价"];
    //搜索
    $scope.search=function(page,rows){
        orderService.search(page,rows,$scope.searchEntity).success(
            function(response){
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }


    $scope.deliverSearch=function(page,rows){
        orderService.search(page,rows,$scope.searchEntity).success(
            function(response){
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }

    // $scope.deliver=function () {
    //     orderService.deliver(selectIds).success(
    //         function (response) {
    //             if (response.falg == true) {
    //                 $scope.reloadList();
    //                 $scope.selectIds = [];
    //                 alert(response.message);
    //             } else {
    //                 // 保存失败
    //                 alert(response.message);
    //             }
    //         });
    // }

    $scope.deliver = function(){
        orderService.deliver($scope.selectIds).success(function(response){
            // 判断保存是否成功:
            if(response.flag==true){
                // 保存成功
                alert(response.message);
                $scope.reloadList();
                $scope.selectIds = [];
            }else{
                // 保存失败
                alert(response.message);
            }
        });
    }





})