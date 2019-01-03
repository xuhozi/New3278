app.service("contentService",function($http){
	this.findByCategoryId = function(categoryId){
		return $http.get("content/findByCategoryId.do?categoryId="+categoryId);
	}

    this.allSortList =function (oneId) {
        return $http.get('http://localhost:9103/content/allSortList.do?oneId='+oneId)
    }




});