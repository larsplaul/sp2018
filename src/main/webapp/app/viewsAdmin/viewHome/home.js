'use strict';

//Todo: Refactor into a separate factory or service
var users = [];

angular.module('myAppRename.admin.home', ['ngRoute'])

        .config(['$routeProvider', function ($routeProvider) {
            $routeProvider.when('/view_admin_home', {
              templateUrl: 'app/viewsAdmin/viewHome/home.html',
              controller: 'AdminHomeCtrl'
            });
          }])
        .filter("showClasses", function () {
          return function (cl) {
            if (cl == null)
              return;
            var list = [];
            cl.forEach(function (classInfo) {
              list.push(classInfo.id);
            });
            return list.join(",");
          }
        })
        .controller('AdminHomeCtrl', function ($scope, $http, $modal, restErrorHandler) {
          $scope.usersToShow = [];
          $scope.itemsPerPage = 8;
          $scope.currentPage = 1;
          $scope.roleToShow = "User";
          $scope.dataReady = false;

          $scope.isAdding = false;
          $scope.isEditing = false;



          $scope.multiSelectSettings = {
            enableSearch: true,
            buttonClasses: "btn btn-default btn-sm",
            scrollable: true,
            dynamicTitle: false,
            showCheckAll: false,
            showUncheckAll: false,
            closeOnSelect: true

          };
          $scope.multiSelectText = {
            buttonDefaultText: 'Select Classes'
                    //dynamicButtonTextSuffix: "Classes"
          }
          $http.get("api/admin/classes")
                  .success(function (data, status, headers, config) {
                    
                    $scope.allclasses = [];
                    if (data == null)
                      return;
                    data.forEach(function (classItem) {
                      $scope.allclasses.push({id: classItem._id, label: classItem.nameShort});
                    });
                  })
                  .error(function (data, status, headers, config) {
                    restErrorHandler.handleErrors(data, status, $scope);
                  })

          $scope.getUsers = function (alwaysFetch) {
            if (users.length > 0 && !alwaysFetch) {
              $scope.users = users;
               $scope.dataReady = true;
              return;
            }

            $scope.users = [];

            $http({method: 'GET', url: 'api/admin/users'})
                    .success(function (data, status, headers, config) {
                      //Refactor into a service
                      //In order to use the multi select control classes must be converted to an object with an id with the value as value
                       $scope.dataReady = true;
                      data.forEach(function (user) {

                        addIdToClasses(user);
                      })
                      users = data;
                      $scope.users = users;
                      $scope.getUsersToShow();
                      $scope.error = null;
                    })
                    .error(function (data, status, headers, config) {
                      restErrorHandler.handleErrors(data, status, $scope);
                    });
          };

          //In order to use the multi select control classes must be converted to an object with an id with the value as value
          function addIdToClasses(user) {
            var classes = [];
            user.classes.forEach(function (classId) {
              classes.push({id: classId});
            });
            user.classes = classes;
          }


          $scope.getUsersToShow = function () {
            var begin = (($scope.currentPage - 1) * $scope.itemsPerPage);
            var end = begin + $scope.itemsPerPage;
            $scope.usersToShow = $scope.users.slice(begin, end);
          };

          $scope.$watch("currentPage + numPerPage", function () {
            $scope.getUsersToShow();
          });


          //Todo --> cache this value and reload only after running a script
          $scope.getUsers(true);
          $scope.getUsersToShow();


          $scope.showAddUser = function () {
            $scope.isAdding = true;
            $scope.isEditing = false;
            $scope.newUser = {};
            $scope.addEditHeading = "Add new User";
            $scope.newUser.firstName = "";
            $scope.newUser.lastName = "";
            $scope.newUser.email = "";
            $scope.newUser.phone = "";
            $scope.newUser.role = "User";
            $scope.newUser.classes = [];
          }

          $scope.cancel = function () {
            $scope.newUser = {};
            $scope.isAdding = false;
            $scope.isEditing = false;
          }


          $scope.saveUser = function () {
            var method = $scope.newUser._id ? "PUT" : "POST";

            //Change the classes array back into a simple list of ID's
            var list = [];
            $scope.newUser.classes.forEach(function (item) {
              list.push(item.id);
            });
            var userToSave = angular.copy($scope.newUser);
            //$scope.newUser.classes = list;
            userToSave.classes = list;


            $http({method: method, url: 'api/admin/addEditUser', data: userToSave})
                    .success(function (data, status, headers, config) {
                      //$scope.getUsers(true); //Requires a server roundtrip to fetch ALL users
                      var newUser = data;
                      addIdToClasses(newUser);
                      if (method == "POST") {
                        $scope.users.push(newUser);

                      }
                      else {
                        $scope.isEditing = false;
                        $scope.newUser = {};
                        var indexForEditedUser = -1;
                        for (var ii = 0; ii < $scope.users.length; ii++) {
                          if ($scope.users[ii]._id === newUser._id) {
                            indexForEditedUser = ii;
                            $scope.users[indexForEditedUser] = newUser;
                            break;
                          }
                        }
                        ;
                      }
                      $scope.getUsersToShow();
                      $scope.showAddUser();

                      $scope.error = null;
                    })
                    .error(function (data, status, headers, config) {
                      restErrorHandler.handleErrors(data, status, $scope);
                    })
          }

          $scope.edit = function (userToEdit) {
            ;
            $scope.newUser = angular.copy(userToEdit);
            $scope.addEditHeading = "Edit " + userToEdit.user;
            $scope.isAdding = true;
            $scope.isEditing = true;
          }

          $scope.resetPassword = function () {
            $http.put("api/admin/resetPasswordFor/" + $scope.newUser._id)
                    .success(function (data, status, headers, config) {
                      $scope.cancel();
                    })
                    .error(function (data, status, headers, config) {
                      restErrorHandler.handleErrors(data, status, $scope);
                    })

          }
          
          $scope.resetEditWindow = function(){
            $scope.isAdding = false;
            $scope.isEditing = false;
          }

          $scope.delete = function (id) {
            var modalInstance = $modal.open({
              templateUrl: 'warningDelete.html',
              controller: 'warningDeleteNameCtrl',
              backdrop: "static",
              size: "sm"
            });

            modalInstance.result.then(function (selection) {
              if (selection === "DELETE") {
                $http.delete("api/admin/deleteUser/" + id)
                        .success(function (data, status, headers, config) {
                          //$scope.getUsers(true); //Requires a server roundtrip for ALL users
                          var indexForDeletedUser = -1;
                          for (var ii = 0; ii < $scope.users.length; ii++) {
                            if ($scope.users[ii]._id === id) {
                              indexForDeletedUser = ii;
                              $scope.users.splice(indexForDeletedUser, 1);
                              $scope.getUsersToShow();
                              break;
                            }
                          }
                          ;
                        })
                        .error(function (data, status, headers, config) {
                          restErrorHandler.handleErrors(data, status, $scope);
                        });
              }
            });
          };
        })
        .controller('warningDeleteNameCtrl', function ($scope, $modalInstance) {
          $scope.cancel = function () {
            $modalInstance.close("CANCEL");
          };
          $scope.delete = function () {
            $modalInstance.close("DELETE");
          };
        });








