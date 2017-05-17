var app = angular.module('app',['ngRoute', 'ngTable']);

app.controller('MainCtrl',function($scope, $http){
    $http.get("questionsAnswers.JSON")
    .then(function (response) {$scope.questions = response.data.QandA;});
    $http.get("customerData.JSON")
    .then(function (response) {$scope.customers = response.data.customers;});

    $scope.sort = {
        column: '',
        descending: false
    };    
    
    $scope.changeSorting = function(column) {

        var sort = $scope.sort;

        if (sort.column == column) {
            sort.descending = !sort.descending;
        } else {
            sort.column = column;
            sort.descending = false;
        }
    };
        
    $scope.selectedCls = function(column) {
        return column == scope.sort.column && 'sort-' + scope.sort.descending;
    };
})

.config(['$routeProvider',function($routeProvider){
$routeProvider.
when('/', {
  templateUrl:'home.html',
  controller:'MainCtrl'
  }).
  when('/1Page', {
  templateUrl:'1Page.html',
  controller:'MainCtrl'
  }).
  when('/2Page', {
  templateUrl:'2Page.html',
  controller:'MainCtrl'
  }).
  when('/3Page', {
  templateUrl:'3Page.html',
  controller:'MainCtrl'
  }).
  when('/errorPage', {
  templateUrl:'errorPage.html',
  controller:'MainCtrl'
  }).
  otherwise( {
  redirectTo : '/errorPage'
  });
}]);
