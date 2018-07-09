'use strict';

// Declare app level module which depends on views, and components
angular.module('myAppRename', [
  'ngRoute',
  'ngSanitize',
  'ui.bootstrap',
  'angularUtils.directives.dirPagination',
  'angular-jwt',
  'myAppRename.controllers',
  'myApp.security',
  'myAppRename.directives',
  'myAppRename.factories',
  'myAppRename.filters',
  'myAppRename.constants',
  'myAppRename.viewPublic1',
  'myAppRename.viewNews',
  'myAppRename.user.home',
  'myAppRename.admin.home',
  'myAppRename.admin.studypoint',
  'myAppRename.admin.studyPointsForStudent',
  'myAppRename.admin.editClass',
  'myAppRename.admin.classInfo',
  'myAppRename.admin.scripts',
  'myAppRename.admin.delphi',
  'myAppRename.admin.utils',
  'myAppRename.viewUsersDetails',
  //'myAppRename.admin.gridDemo',
  'angularjs-dropdown-multiselect'
]).
config(['$routeProvider', function($routeProvider) {
    $routeProvider.otherwise({redirectTo: '/view1'});
}])
.config(function ($httpProvider) {
   $httpProvider.interceptors.push('AuthInterceptor');
  });



