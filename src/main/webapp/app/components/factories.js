'use strict';

/* Factories */

var app = angular.module('myAppRename.factories', []);

app.factory('restErrorHandler', function () {
  var handleErrors = function (data, status, $scope) {
    $scope.error = data.error;
    if (status == 401) {
      //if (data.error == "jwt expired") {
      $scope.error = data.error;
      $scope.$emit("logOutEvent");
      return;
    }
    if (status == 403) {
      // if (data.error == "jwt expired") {
      $scope.$emit("logOutEvent");
      return;
    }
    //$scope.error = data.error;
  }
  return {
    handleErrors: handleErrors
  }
});

app.factory('authInterceptor', function ($rootScope, $q, $window) {
  return {
    request: function (config) {
      config.headers = config.headers || {};

      var skipAddHeader = !(config.url.toLowerCase().indexOf('https://jokes-plaul.rhcloud.com') === 0);
      if (skipAddHeader) {
        if ($window.sessionStorage.token) {
          config.headers.Authorization = 'Bearer ' + $window.sessionStorage.token;
        }
      }
      return config;
    },
    //Clean up this mess
    responseError: function (rejection) {
      var err = rejection.data;
      if (typeof err.error === 'undefined' || typeof err.error.message === 'undefined' || typeof err.error.code === 'undefined') {
        return $q.reject(rejection);
      }
      $rootScope.error = err.error;

      return $q.reject(rejection);
    }
  };
});

;