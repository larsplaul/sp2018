'use strict';

var app = angular.module('myAppRename.admin.delphi', ['ngRoute'])

        .config(['$routeProvider', function ($routeProvider) {
            $routeProvider.when('/view_admin_delphi', {
              templateUrl: 'app/viewsAdmin/viewDelphi/delphi.html',
              controller: 'delphiCtrl'
            });
          }])
        .factory("evalData", function () {
          var evaluation = {};
          var getEvaluation = function () {
            return evaluation;
          };
          return getEvaluation;
        });

var text1 = "Enter this URL into your browser: ";
var text2 = "Login with this code: ";
var text3 = "DO NOT close your Browser Window until the evaluation is over. Results from other members of the class should pop up, every time a new reply is posted.";
text3 += "\nWhen the evaluation is closed, you should hopefully all get the final result, which we will use for a discussion";
text3 += "\n-------------------------------------------------------------------------------------------------------";

app.controller('delphiCtrl', function ($http, $scope, CONSTANTS, evalData) {

  $scope.evaluation = evalData();
  $scope.submitted = false;
  //var domain= $location.protocol() + "://" + $location.host() + ":" + $location.port();
  var domain = CONSTANTS.DELPHI_DOMAIN;

  $scope.submit = function () {
    $http({
      method: 'POST',
      url: CONSTANTS.DELPHI_API,
      data: $scope.evaluation
    }).then(function successCallback(res) {
      $scope.url = domain + "/eval/" + res.data.url;
      $scope.idOpen = domain + "/eval/open/" + res.data.url + "/" + res.data.idOpen;
      $scope.idClose = domain + "/eval/close/" + res.data.url + "/" + res.data.idClose;
      $scope.idResult = domain + "/eval/res/" + res.data.url;
      $scope.submitted = true;
      
      $scope.text1 = text1;
      $scope.text2 = text2;
      $scope.text3 = text3;
      $scope.codes = res.data.codes;
      $scope.codesStr = res.data.codes.join("\n");
      
    }, function errorCallback(res) {
      $scope.error = res.status + ": " + res.data.statusText;
    });
  };

  $scope.makeHandOuts = function () {
    var handOutText = "";
    $scope.codes.forEach(function (code) {
      handOutText += $scope.text1 + " " + $scope.url + "\n";
      handOutText += $scope.text2 + " " + code + "\n";
      handOutText += $scope.text3 + "\n\n";
    });
    $scope.handOutText = handOutText;
  };

  $scope.clear = function () {
    $scope.submitted = false;
    $scope.evaluation = {};
    $scope.url = null;
    $scope.codes = [];
    $scope.text1 = "";
    $scope.text2 = "";
    $scope.text3 = "";
    $scope.handOutText = null;
  };

});