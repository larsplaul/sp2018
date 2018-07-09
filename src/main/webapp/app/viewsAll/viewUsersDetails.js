'use strict';

angular.module('myAppRename.viewUsersDetails', ['ngRoute'])

        .config(['$routeProvider', function ($routeProvider) {
            $routeProvider
                    .when('/view_all_userDetails', {
                      templateUrl: 'app/viewsAll/viewUsersDetails.html',
                      controller: "userDetailsCtrl"
                    });
          }])
        .filter("showClasses", function () {
          return function (classes) {
            if (classes == null)
              return;
            var result = classes.length === 1 ? classes[0] : classes.join(",");
            return result;
          };
        })
        .controller('userDetailsCtrl', function ($scope, $http,restErrorHandler) {
          $scope.canEdit = false;
          $scope.isChangingPassword = false;
          $scope.passwordInfo = makeEmptyPasswordInfo();
          $http.get("api/userDetails")
                  .success(function (data, status, headers, config) {
                    $scope.currentUser = data;
                    $scope.originalValue = angular.copy(data);
//        if(data == null)
//         return;
//        data.forEach(function(classItem){
//          $scope.allclasses.push({id : classItem._id, label: classItem.nameShort});
//        });
                  })
                  .error(function (data, status, headers, config) {
                    restErrorHandler.handleErrors(data, status, $scope);
                  })

          $scope.saveUser = function () {
            $http.put("api/userDetails/edit", JSON.stringify($scope.currentUser))
                    .success(function (data, status, headers, config) {
                      $scope.currentUser = data;
                      $scope.canEdit = false;
                      $scope.error = null;
                    })
                       .error(function (data, status, headers, config) {
                      restErrorHandler.handleErrors(data, status, $scope);
                    })
          };
          
          $scope.editUser = function(){
            $scope.canEdit = true;
          }
          
          $scope.cancel = function(){
            $scope.canEdit = false;
            $scope.currentUser = $scope.originalValue;
          }
          
          $scope.changePassword = function(){
            $scope.isChangingPassword = true;
          }
          
          $scope.cancelChangePassword = function(){
            $scope.isChangingPassword = false;
            $scope.error = null;
          }
          
          $scope.saveNewPassword = function(){
            
            $http.put("api/userDetails/changePassword",JSON.stringify($scope.passwordInfo))
                    .success(function(data, status, headers, config){
                      $scope.isChangingPassword = false;
                      $scope.passwordInfo = {};
                      $scope.error = null;
                    })
                    .error(function (data, status, headers, config) {
                      $scope.passwordInfo = makeEmptyPasswordInfo();
                      restErrorHandler.handleErrors(data, status, $scope);
                    });
            
          }
          
          function makeEmptyPasswordInfo (){
           return {old:"",newPw1: "",newPw2: ""}
          }
          

        });