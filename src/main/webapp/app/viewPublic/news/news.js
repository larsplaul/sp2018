'use strict';

angular.module('myAppRename.viewNews', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider

    .when('/news', {
    templateUrl: 'app/viewPublic/news/news.html',
    
  });
}]);
