app.controller("contentController",function($scope,contentService){
	$scope.contentList = [];
	// 根据分类ID查询广告的方法:
	$scope.findByCategoryId = function(categoryId){
		contentService.findByCategoryId(categoryId).success(function(response){
			$scope.contentList[categoryId] = response;
		});
	}
	
	//搜索  （传递参数）
	$scope.search=function(){
		location.href="http://localhost:9103/search.html#?keywords="+$scope.keywords;
	}


    $scope.allSortListoneId =[];

    $scope.allSortList=function (oneId) {
        contentService.allSortList(oneId).success(function (response) {
            $scope.allSortListoneId[oneId] =response;
        })
    }

	
});