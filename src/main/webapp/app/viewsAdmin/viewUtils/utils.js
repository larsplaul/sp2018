'use strict';


var app = angular.module('myAppRename.admin.utils', ['ngRoute'])

        .config(['$routeProvider', function ($routeProvider) {
            $routeProvider.when('/view_admin_utils', {
              templateUrl: 'app/viewsAdmin/viewUtils/utils.html',
              controller: 'ctrl'
            });
          }]);


var locations = {
  "c": "City",
  "s": "Søerne",
  "n": "Nørrebro",
  "h": "Hillerød",
  "l": "Lyngby"
};

function getDescription(id) {
  const danishSummer = "abcdefghijklmno";
  const danishWinter = "qrst";
  const internationalSummer = "uvx";
  const internationalWinter = "yz";
  
  //This is a guess, not in the description on intranet
  if(id==="p"){
    return "Internship";
  }
  
  if (danishSummer.indexOf(id) > -1) {
    return "Danish class started as a summer clas";
  }
  if (danishWinter.indexOf(id) > -1) {
    return "Danish class started as a Winter clas";
  }
  if (internationalSummer.indexOf(id) > -1) {
    return "International class started as a Summer clas";
  }
  if (internationalWinter.indexOf(id) > -1) {
    return "International class started as a Winter clas";
  }
}

app.controller('ctrl', function ($scope) {

  $scope.classToDecode = "";

  $scope.decode = function () {
    var skipStartYear = false;
       
    $scope.classToDecode = $scope.classToDecode.replace("CPH-","");
    var className = $scope.classToDecode;
    $scope.warning = "";
    if ($scope.classToDecode.length === 9) {
      $scope.warning= "According to description, length should be 11. Assumed this was a class without start year: ";
      skipStartYear = true;
    } else if ($scope.classToDecode.length !== 11) {
      window.alert("Error: Length should be 11, but is: " + $scope.classToDecode.length);
      return;
    }
    
    $scope.location = locations[className[0]];
    $scope.year = 2000 + Number(className.substring(1, 3));
    $scope.education = className.substring(3, 6);
    $scope.semester = className[6];
    $scope.sumWin = getDescription(className[7]);
    if (skipStartYear) {
      $scope.started = "Could not be calculated";
      $scope.runsIn = className[8] == "f" ? "Spring" : "Authum";
    } else {
      $scope.started = 2000 + Number(className.substring(8, 10));
      $scope.runsIn = className[10] == "f" ? "Spring" : "Authum";
    }
  };

});