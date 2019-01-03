//首页控制器
app.controller('indexController',function($scope,$controller,$http,loginService,userService){

    $controller('baseController',{$scope:$scope});

	$scope.showName=function(){
			loginService.showName().success(
					function(response){
						$scope.loginName=response.loginName;
					}
			);
	}

    $scope.search = function(page,rows){
        // 向后台发送请求获取数据:
        userService.search(page,rows).success(function(response){
            $scope.paginationConf.totalItems = response.total;
            $scope.list = response.rows;
        });
    }

    $scope.xx=[" ","未付款","已付款","未发货","已发货","交易成功","交易关闭","待评价"];

    $scope.findListByLoginUser = function(){
        // 向后台发送请求获取数据:
        userService.findListByLoginUser().success(function(response){
            $scope.list = response;
        });
    }

    $scope.showInfo = function(){
        // 向后台发送请求获取数据:
        userService.showInfo().success(function(response){
            $scope.user = response;
        });
    }
});