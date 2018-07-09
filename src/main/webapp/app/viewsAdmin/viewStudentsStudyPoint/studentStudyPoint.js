'use strict';

var app = angular.module('myAppRename.admin.studyPointsForStudent', ['ngRoute'])
        .config(['$routeProvider', function ($routeProvider) {
            $routeProvider.when('/viewAdmin_studentDetail/:studentId/:name', {
              templateUrl: 'app/viewsAdmin/viewStudentsStudyPoint/studentStudyPoint.html',
              controller: 'StudentStudyPointCtrlAdmin'
            })
                    .when("/viewAdmin_studentDetail/:studentId/:name/:classId/:periodId", {
                      templateUrl: 'app/viewsAdmin/viewStudentsStudyPoint/studentStudyPoint.html',
                      controller: 'StudentStudyPointCtrlAdmin'
                    });
          }]);


app.filter("sumPointsForPeriod", function () {
  return function (scores) {
    if (typeof (scores) === "undefined") {
      return 0;
    }
    var sum = 0;
    for (var i = 0; i < scores.length; i++) {
      sum += Number(scores[i].score);
    }
    return sum;
  };
});


app.controller('StudentStudyPointCtrlAdmin', ['$scope', '$http', '$routeParams', '$filter', function ($scope, $http, $routeParams, $filter, resrErrorHandler) {

    $scope.name = $routeParams.name; //Only set from Admin View
    $scope.studentId = $routeParams.studentId;
    $scope.max = 100;
    $scope.safeStatus = null;



    $scope.showPeriod = function (periodName) {
      $scope.safeStatus = null;
      $scope.period = getPeriod($scope.allPeriods, periodName);
    };

    var urlForStudentsClasses = "api/admin/classesForStudent/" + $routeParams.studentId;
    $http.get(urlForStudentsClasses)
            .success(function (data, status, headers, config) {
              $scope.classes = data;
              if($scope.classes.length === 1){//No need to show a select button, just show the class
               $scope.oneClassOnly = true;
               $scope.selectedClassId = $scope.classes[0]._id; 
              }  
              $scope.allPeriods = null;
              $scope.period = null;
              ;
              $scope.error = null;
            })
            .error(function (data, status, headers, config) {
              restErrorHandler.handleErrors(data, status, $scope);
            });



    $scope.getClass = function (id, editedPeriod) {
      $scope.selectedClassId = id;
      //var urlForStudentsClass = "adminApi/class/"+id+"/"+$routeParams.studentId ;
      var urlForStudentsClass = "api/admin/studypointsForStudentClass/" + id + "/" + $routeParams.studentId;
      $http.get(urlForStudentsClass)
              .success(function (data, status, headers, config) {
                $scope.allPeriods = data.periods;
                $scope.maxPointForSemester = data.maxPointForSemester;
                $scope.requiredPointsPercentage = data.requiredPoints;
                $scope.reqPointsPercentageString = data.requiredPoints + "%";
                $scope.requiredPoints = $scope.maxPointForSemester * data.requiredPoints / 100;
                //$scope.period = editedPeriod;
                $scope.showPeriod(editedPeriod);
                var pointsScoredForPeriod = $filter('sumForSemester')($scope.allPeriods);
                var pointScoredPercent = pointsScoredForPeriod * 100 / $scope.maxPointForSemester;
                if (pointScoredPercent < $scope.requiredPointsPercentage) {
                  $scope.type = 'warning';
                } else {
                  $scope.type = 'success';
                }
                //$scope.period = null;
              })
              .error(function (data, status, headers, config) {
                $scope.error = data.toString();
              });
    };

    if ($routeParams.classId && $routeParams.periodId) {
      $scope.getClass($routeParams.classId, $routeParams.periodId);
    }

    $scope.savePeriod = function () {
      $scope.period = null;
      var scores = [];
      $scope.allPeriods.forEach(function (p) {
        p.tasks.forEach(function (task) {
          scores.push({id: task.spId, score: task.score});
        });
      });
      $http.put("api/admin/scores", scores)
              .success(function (data) {
                $scope.studyPointFormForUser.$dirty = false;
                $scope.studyPointFormForUser.$setPristine();
                $scope.safeStatus = "New study points successfully added";
                $scope.getClass($scope.selectedClassId, null);
                //setTimeout(function(){
                //  $scope.period = data
                //},1000);
              })
              .error(function (data, status, headers, config) {
                restErrorHandler.handleErrors(data, status, $scope);
              })
    };




    function getPeriod(periods, periodName) {
      for (var i = 0; i < periods.length; i++) {
        if (periods[i].periodName === periodName)
          return periods[i];
      }
      return null;
    }

  }]);

