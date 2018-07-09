'use strict';

var app = angular.module('myAppRename.admin.editClass', ['ngRoute'])

  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider.when('/view_admin_editClass', {
      templateUrl: 'app/viewsAdmin/viewEditClasses/classEdit.html',
      controller: 'classEditCtrl'
    });
  }]);


app.controller('classEditCtrl', function ($scope, $http,restErrorHandler) {


  $http.get("api/admin/classes")
    .success(function (data, status, headers, config) {
      $scope.classes = data;
    })
    .error(function (data, status, headers, config) {
      restErrorHandler.handleErrors(data,status,$scope);
    })

  $scope.getClass = function (id) {
    $scope.selectedClassId = id;
    $http.get("api/admin/classesFullInfo/" + id)
      .success(function (data, status, headers, config) {
        
        $scope.selectedClass = data;
        $scope.selectedClass.students.forEach(function (student) {
          student.delete = false;
        });
      })
      .error(function (data, status, headers, config) {
        restErrorHandler.handleErrors(data,status,$scope);
      });
  };

  $scope.saveEditedClass = function(){
    var classToEdit = JSON.parse(JSON.stringify($scope.selectedClass));
    delete classToEdit.students;
    classToEdit.studentsToRemove = [];
    classToEdit.studentsToRemove = $scope.selectedClass.students.filter(function(student){
       return student.delete === true; 
    });
    $scope.selectedClass = null;
    
    setTimeout(function() {
     // $http.put("api/admin/removeFromClass/" + $scope.selectedClassId, selectedClass)
      $http.put("api/admin/removeFromClass/", classToEdit)
        .success(function (data, status, headers, config) {

          $scope.getClass($scope.selectedClassId);
        })
        .error(function (data, status, headers, config) {
          restErrorHandler.handleErrors(data,status,$scope);
        })
    },500);
  }

});

