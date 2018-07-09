//'use strict';
//
////Todo: Refactor into a separate factory or service
//var users = [];
//
//var app = angular.module('myAppRename.admin.gridDemo', ['ngRoute','ngTouch', 'ui.grid', 'ui.grid.pinning', 'ui.grid.edit', 'ui.grid.cellNav']);
//
//  app.config(['$routeProvider', function ($routeProvider) {
//    $routeProvider.when('/view_admin_gridDemo', {
//      templateUrl: 'app/viewsAdmin/Grid-Demo/gridDemo.html',
//      controller: 'StudyPointGridCtrl'
//    });
//  }]);
//
////app.filter("sumOfAllPoints", function () {
////    return function (tasks, index) {
////      if (typeof(tasks) == "undefined" || typeof(index) == "undefined") {
////        return 0;
////      }
////      var sum = 0;
////      for (var i = 0; i < tasks.length; i++) {
////        sum += Number(tasks[i].scores[index].score);
////      }
////      return sum;
////    }
////  });
//
//app.controller('StudyPointGridCtrl', function ($scope, $http, $modal) {
//
//    $scope.getClass = function (id) {
//      $http.get("adminApi/classFromId/" + id)
//        .success(function (data, status, headers, config) {
//          $scope.period = null;
//          $scope.selectedClass = data;
//
//        })
//        .error(function (data, status, headers, config) {
//          scope.error = data.toString();
//        })
//    }
//
//    $scope.showPeriod = function (id) {
//        getPeriod(id);
//      //}
//    }
//    $scope.gridOptions = {};
//    $scope.gridOptions.columnDefs = [];
//    $scope.gridOptions.data = {};
//    //$scope.gridOptions.enablePinning = true;
//
//    function getPeriod(id){
//
//      $http.get("adminApi/period/" + id)
//        .success(function (data, status, headers, config) {
//          var studyPoints = [];
//          $scope.gridOptions.columnDefs = [];
//          $scope.gridOptions.columnDefs.push({ name:'name', width:100, pinnedLeft:true });
//          for(var j = 0; j < data.tasks.length; j++) {
//            $scope.gridOptions.columnDefs.push({name: data.tasks[j].name, width: 100,enableCellEditOnFocus:true,
//              enableColumnMenu: false,
//              enableSorting: false,
//              type:"number" });
//          }
//          //Hack to show last column, grid is BUGGY
//          $scope.gridOptions.columnDefs.push({name: "Sum", width: 100});
//          for(var i = 0 ; i <  $scope.selectedClass.students.length;i++){
//            var studyPoint = {};
//            studyPoint.name =  $scope.selectedClass.students[i].firstName+ " "+ $scope.selectedClass.students[i].lastName;
//            for(var j = 0; j < data.tasks.length; j++){
//              studyPoint[data.tasks[j].name] = data.tasks[j].scores[i].score;
//            }
//            studyPoints.push(studyPoint);
//          }
//          $scope.gridOptions.data = studyPoints;
//          $scope.period = data;
//          $scope.rows = $scope.selectedClass.students.length;
//          //$scope.studyPointGrid.$dirty = false;
//        })
//        .error(function (data, status, headers, config) {
//          scope.error = data.toString();
//        })
//    }
//
//
//    $scope.savePeriod = function () {
//
//      var period = {
//        _id: $scope.period._id,
//        periodName: $scope.period.periodName,
//        periodDescription: $scope.period.periodDescription,
//        tasks: $scope.period.tasks
//      }
//
//      $http.put("adminApi/period", period)
//        .success(function (data, status, headers, config) {
//          $scope.period = data;
//          $scope.rows = $scope.period.students.length;
//          $scope.studyPointGrid.$dirty = false;
//        })
//        .error(function (data, status, headers, config) {
//          scope.error = data.toString();
//        })
//    }
//    //Used to se the Tab-index (when a double {{..}} expression is used is seems not no threat a number a number but as a string
//    $scope.calculateTabIndex = function (str) {
//      return Number(str) + 1;
//    }
//
//  });
//
//  app.controller('WarningDirtyFormCtrl', function ($scope, $modalInstance) {
//
//    $scope.cancel = function () {
//      $modalInstance.close("CANCEL");
//    };
//    $scope.continueAndDrop = function () {
//      $modalInstance.close("CONTINUE_DROP");
//    };
//  });
