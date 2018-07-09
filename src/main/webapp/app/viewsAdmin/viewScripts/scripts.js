'use strict';
//http://stackoverflow.com/questions/18839048/how-to-read-a-file-in-angularjs
angular.module('myAppRename.admin.scripts', ['ngRoute'])

        .config(['$routeProvider', function ($routeProvider) {
                $routeProvider.when('/view_admin_script', {
                    templateUrl: 'app/viewsAdmin/viewScripts/scripts.html',
                    controller: 'scriptController'
                });
            }])
        .controller('scriptController', function ($scope, $http, fileReader, restErrorHandler) {

            $scope.showSpinner = false;
            $scope.getFile = function () {
                $scope.status = "";
                $scope.progress = 0;
                fileReader.readText($scope.file, $scope)
                        .then(function (result) {
                            $scope.script = result;
                        });
            };

            $scope.$on("fileProgress", function (e, progress) {
                $scope.progress = progress.loaded / progress.total;
            });

            $scope.sendScript = function () {
                var req = {
                    method: 'POST',
                    url: 'api/script',
                    headers: {
                        'Content-Type': "text/plain"
                    },
                    data: $scope.script
                };
                $scope.showSpinner = true;
                $http(req)
                        .success(function (data, status, headers, config) {
                            $scope.status = data.status;
                            $scope.showSpinner = false;
                        })
                        .error(function (data, status, headers, config) {
                            $scope.showSpinner = false;
                            $scope.status = data.error.message;
                        });

            };
        })
        .factory("fileReader", ["$q", function ($q) {

                var onLoad = function (reader, deferred, scope) {
                    return function () {
                        scope.$apply(function () {
                            deferred.resolve(reader.result);
                        });
                    };
                };

                var onError = function (reader, deferred, scope) {
                    return function () {
                        scope.$apply(function () {
                            deferred.reject(reader.result);
                        });
                    };
                };

                var onProgress = function (reader, scope) {
                    return function (event) {
                        scope.$broadcast("fileProgress",
                                {
                                    total: event.total,
                                    loaded: event.loaded
                                });
                    };
                };

                var getReader = function (deferred, scope) {
                    var reader = new FileReader();
                    reader.onload = onLoad(reader, deferred, scope);
                    reader.onerror = onError(reader, deferred, scope);
                    reader.onprogress = onProgress(reader, scope);
                    return reader;
                };

                var readText = function (file, scope) {
                    var deferred = $q.defer();

                    var reader = getReader(deferred, scope);
                    //reader.readAsDataURL(file);
                    reader.readAsText(file, 'ISO-8859-1');

                    return deferred.promise;
                };

                return {
                    readText: readText
                };
            }])
        .directive("ngFileSelect", function () {

            return {
                link: function ($scope, el) {

                    el.bind("change", function (e) {
                        $scope.file = (e.srcElement || e.target).files[0];
                        $scope.getFile();
                    })
                }}
        });







