'use strict';

//Todo: Refactor into a separate factory or service
var users = [];

var app = angular.module('myAppRename.admin.studypoint', ['ngRoute']);

app.config(['$routeProvider', function ($routeProvider) {
    $routeProvider.when('/view_admin_studypoints', {
      templateUrl: 'app/viewsAdmin/viewStudyPoint/studypoint.html',
      controller: 'AdminStudyPointCtrl'
    });
  }]);

app.directive('ngModel', function () {
  return {
    require: 'ngModel',
    link: function (scope, elem, attr, ngModel) {
      elem.on('blur', function () {
        ngModel.$dirty = true;
        scope.$apply();
      });

      ngModel.$viewChangeListeners.push(function () {
        ngModel.$dirty = false;
      });

      scope.$on('$destroy', function () {
        elem.off('blur');
      });
    }
  }
});

app.filter("sumOfAllPoints", function () {
  return function (student) {
    if (typeof (student) == "undefined") {
      return 0;
    }
    var sum = 0;
    for (var i = 0; i < student.scores.length; i++) {
      sum += Number(student.scores[i].score);
    }
    student.sumOfAllPoints = sum;
    return sum;
  }
});

app.filter("filterTasks", function () {
  /*
   list is either a list of Task (when filter is used for the headers) or a list of scores (when filter is used to present scores)
   For headers list and info.tasks should be the same
   */
  return function (list, info) {
    if (typeof list == "undefined" || typeof info.filterText == "undefined" || typeof info.tasks == "undefined" || typeof info.header == "undefined") {
      return null;
    }
    var foundItemArray = [];
    if (!info.likeMatch) {
      for (var i = 0; i < info.tasks.length; i++) {
        if (info.tasks[i].name == info.filterText) {
          foundItemArray.push(list[i]);
          break;
        }
      }
    } else {
      for (var i = 0; i < info.tasks.length; i++) {
        if (info.tasks[i].name.indexOf(info.filterText) >= 0) {
          var name = info.tasks[i].name;
          var found = (info.tasks[i].name.indexOf(info.filterText)) >= 0;
          console.log(name + ", " + found);
          foundItemArray.push(list[i]);
          // break;
        }
      }
    }
    var returnval = foundItemArray.length == 0 ? list : foundItemArray;
    return returnval;
  }
});

app.controller('AdminStudyPointCtrl', function ($scope, $http, $modal, $location, restErrorHandler) {

  $scope.loading = false;
  $scope.saving = false;
  $scope.predicate = 'fullName';
  $scope.filteredTasks = "";
  $scope.likeMatch = false;
  $scope.safeStatus = null; //Bind information represententing safe status (from server)

  //$http.get("adminApi/class")
  $http.get("api/admin/classes")
          .success(function (data, status, headers, config) {
            $scope.classes = data;
          })
          .error(function (data, status, headers, config) {
            restErrorHandler.handleErrors(data, status, $scope);
          })


  function dirtyCheckOnRouteNavigation() {
    var changeEvent = $scope.$on('$locationChangeStart', function (event, next, current) {
      if ($scope.studyPointForm.$dirty) {
        event.preventDefault();
        var modalInstance = $modal.open({
          templateUrl: 'DirtyForm.html',
          controller: 'DirtyFormCtrl',
          backdrop: "static",
          size: "sm"
        });
        event.preventDefault();
        modalInstance.result.then(function (selection) {
          if (selection === "save_changes") {
            $scope.savePeriod();
          }
          changeEvent();
          setTimeout(function () {
            $location.path(next);
          }, 50);
        });
      }
    });
  }


  $scope.getClass = function (id) {
    //http.get("adminApi/classFromId/" + id)
    $http.get("api/admin/classFromId/" + id)
            .success(function (data, status, headers, config) {
              $scope.period = null;
              $scope.selectedClass = data;
            })
            .error(function (data, status, headers, config) {
              restErrorHandler.handleErrors(data, status, $scope);
            })
  }

  $scope.showPeriod = function (id) {
    $scope.safeStatus = null;
    dirtyCheckOnRouteNavigation();

    //TODO --> IS this nessecary: 
    //$scope.period = null;
    if ($scope.studyPointForm.$dirty) {
      var modalInstance = $modal.open({
        templateUrl: 'DirtyForm.html',
        controller: 'DirtyFormCtrl',
        backdrop: "static",
        size: "sm"
      });
      modalInstance.result.then(function (selection) {
        if (selection == "save_changes") {
          $scope.savePeriod();
        }
        getPeriod(id);
      });
    } else {
      getPeriod(id);
    }
  };

  function getPeriod(id) {
    $scope.loading = true;
    $scope.period = null;
    $http.get("api/admin/period/" + id)
            .success(function (data, status, headers, config) {
              $scope.period = data;
              $scope.loading = false;

              $scope.rows = $scope.period.students.length;
              //XXXXXXXXXXXXXXXXXXXXXXX

              $scope.originalPoints = [];
              $scope.period.students.forEach(function (student) {
                student.scores.forEach(function (s) {
                  var score = {};
                  score.id = s.id;
                  score.score = s.score;
                  $scope.originalPoints.push(score);
                });
              });
              //*****************************************



              $scope.studyPointForm.$dirty = false;
            })
            .error(function (data, status, headers, config) {
              $scope.loading = false;
              restErrorHandler.handleErrors(data, status, $scope);

            });
  }

  $scope.getCode = function (id) {
    console.log("Getting code for : " + id);

    $http.get("api/admin/attendancecode/" + id)
            .success(function (data, status, headers, config) {

              var thisClass = $scope.period.classId;
              var period = $scope.period.periodName;
              $scope.period = null;
              $modal.open({
                templateUrl: 'showCode.html',
                controller: 'showCodeCtrl',
                resolve: {
                  info: function () {
                    return {
                      code: data.code,
                      thisClass: thisClass,
                      period: period
                    };
                  }
                },
                backdrop: "static",
                size: "lg"
              });
            })
            .error(function (data, status, headers, config) {
              restErrorHandler.handleErrors(data, status, $scope);
            });
  };

  $scope.savePeriod = function () {
    var scores = [];


    $scope.period.students.forEach(function (student) {
      student.scores.forEach(function (score) {
        //TODO XXXXXXXXXXXXXXXX
        var org = $scope.originalPoints;
        var wasChanged = org.filter(function (s) {
          return s.id === score.id && s.score != score.score;
        }).length > 0;
        if (wasChanged) {
          scores.push({id: score.id, score: score.score});
        }
        //************************************
        //scores.push({id: score.id, score: score.score});
      });
    });
    $scope.saving = true;
    $scope.period = null;
     
    $http.put("api/admin/scores", scores)
            .success(function (data, status, headers, config) {
              //TODO--> Should we return status ??? 
              $scope.period = null;
              $scope.saving = false;
              $scope.safeStatus = "New study points successfully added";

              //TODO --> CHECK this $scope.rows = $scope.selectedClass.students.length;
              //$scope.rows = $scope.period.students.length;
              $scope.studyPointForm.$dirty = false;
              $scope.studyPointForm.$setPristine();
            })
            .error(function (data, status, headers, config) {
              $scope.waiting = false;
              restErrorHandler.handleErrors(data, status, $scope);
            })
  }
//Used to se the Tab-index (when a double {{..}} expression is used is seems not no threat a number as a number but as a string
  $scope.calculateTabIndex = function (str) {
    return Number(str) + 1;
  }
})
        ;

app.controller('DirtyFormCtrl', function ($scope, $modalInstance) {

  $scope.saveChanges = function () {
    $modalInstance.close("save_changes");
  };
  $scope.continueAndDrop = function () {
    $modalInstance.close("drop_changes");
  };
});

app.controller('showCodeCtrl', function ($scope, $modalInstance, info) {



  $scope.code = info.code;
  $scope.thisclass = info.thisClass;
  $scope.period = info.period;
  $scope.url = location.protocol + "//" + location.host + "//" + location.pathname;
  $modalInstance.close();
  $scope.saveChanges = function () {
    $modalInstance.close();
  };
  $scope.continueAndDrop = function () {
    $modalInstance.close("drop_changes");
  };
});
