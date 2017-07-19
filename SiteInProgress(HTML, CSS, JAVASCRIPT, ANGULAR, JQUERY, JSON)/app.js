var app = angular.module('app',['ngRoute', 'ngTable']);

app.controller('MainCtrl', function($scope, $http, $filter, NgTableParams){
    $http.get("questionsAnswersData.JSON")
    .then(function (response) {$scope.questions = response.data.QandA;});
    $http.get("customerData.JSON")
    .then(function (response) {$scope.customers = response.data.customers;});

    $scope.sort = {
        column: '',
        descending: false
    };    
    
    $scope.start = function() {
      scope.id = 0;
      scope.quizOver = false;
      scope.inProgress = true;
      scope.getQuestion();
      console.log("start function works");
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
        return column == $scope.sort.column && 'sort-' + $scope.sort.descending;
    };

    var fff = [{name: "a", id: 1, state: "a"},
               {name: "b", id: 2, state: "b"}
              ];
    $scope.tableParams = new NgTableParams({page: 1, count: 10}, {customers: $scope.customers});

})

.config(['$routeProvider',function($routeProvider){
$routeProvider.
when('/', {
  templateUrl:'home.html',
  controller:'MainCtrl'
  }).
  when('/quiz', {
  templateUrl:'quiz.html',
  controller:'MainCtrl'
  }).
  when('/sorting', {
  templateUrl:'sorting.html',
  controller:'MainCtrl'
  }).
  when('/filtering', {
  templateUrl:'filtering.html',
  controller:'MainCtrl'
  }).
  when('/photos', {
  templateUrl:'photos.html',
  controller:'MainCtrl'
  }).
  when('/contact', {
  templateUrl:'contact.html',
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
