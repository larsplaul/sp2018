'use strict';

/* Filters */

angular.module('myAppRename.filters', [])
  .filter('checkmark', function () {
    return function(input) {
      return input ? '\u2713' : '\u2718';
    };
  })
  .filter("sumForPeriod", function () {
    return function (tasks) {
      if (typeof(tasks) === "undefined" || tasks === null) {
        return 0;
      }
      var sum = 0;
      for (var i = 0; i < tasks.length; i++) {
        sum += Number(tasks[i].score);
      }
      return sum;
    }
  })
  .filter("sumForSemester", function () {
    return function (allPeriods) {
      if (typeof(allPeriods) === "undefined" || allPeriods === null ) {
        return 0;
      }
      var sum = 0;
      for(var i = 0; i < allPeriods.length; i++) {
        for (var j = 0; j < allPeriods[i].tasks.length; j++) {
          sum += Number(allPeriods[i].tasks[j].score);
        }
      }
      return sum;
    }
  });
