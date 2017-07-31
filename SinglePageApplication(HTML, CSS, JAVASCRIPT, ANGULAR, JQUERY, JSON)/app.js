var app = angular.module('app',['ngRoute', 'ngTable']);


app.directive('quiz', function(quizFactory) {
  return {
    restrict: 'AE',
    scope: {},
    templateUrl: 'template.html',
    link: function(scope, elem, attrs) {
      scope.start = function() {
        scope.id = 0;
        scope.quizOver = false;
        scope.inProgress = true;
        scope.getQuestion();
      };

      scope.reset = function() {
        scope.inProgress = false;
        scope.score = 0;
      }

      scope.getQuestion = function() {
        var q = quizFactory.getQuestion(scope.id);
        if(q) {
          scope.question = q.question;
          scope.options = q.options;
          scope.answer = q.answer;
          scope.answerMode = true;
        } else {
          scope.quizOver = true;
        }
      };

      scope.checkAnswer = function() {
        if(!$('input[name=answer]:checked').length) return;

        var ans = $('input[name=answer]:checked').val();

        if(ans == scope.options[scope.answer]) {
          scope.score++;
          scope.correctAns = true;
        } else {
          scope.correctAns = false;
        }

        scope.answerMode = false;
      };

      scope.nextQuestion = function() {
        scope.id++;
        scope.getQuestion();
      }

      scope.reset();
    }
  }
});

app.factory('quizFactory', function() {
  var questions = [
    {
      question: "Should you really you hire Wyatt Sorenson?", 
      options: ["Yes, of course!  Just do it.", "No", "Maybe", "I don't know"],
      answer: 0
    },
    {
      question: "Why should you hire Wyatt Sorenson?",
      options: ["He's got the skills!", "He's a nice guy", "He knows all the best jokes", "All of the above"],
      answer: 3
    },
    {
      question: "What is the basis of his skills",
      options: ["ABET accredited CS degree", "Web Development Internship", "Years of work experience", "All of the above"],
      answer: 3
    },
    {
      question: "Are you going to contact him for an interview?",
      options: ["Interviews are of the past. We hire without interviews.", "Yes, right away!", "No, his code isn't perfect", "No, he needs 5 years expierience"],
      answer: 1
    },
    { 
      question: "What is Wyatt Sorenson doing this very moment?",
      options: ["Crying in the corner because he has no job", "Twidling his thumbs waiting for someone to give him a job", "Practicing his skills to showcase them", "Becoming disenchanted of the American Dream"],
      answer: 2
    }
  ];

  return {
    getQuestion: function(id) {
      if(id < questions.length) {
        return questions[id];
      } else {
        return false;
      }
    }
  };
});

app.controller('MainCtrl', function($scope, $http, $filter, NgTableParams){
    $http.get("questionsAnswersData.JSON")
    .then(function (response) {$scope.questions = response.data.QandA;});
    $http.get("customerData.JSON")
    .then(function (response) {$scope.customers = response.data.customers;});

    $scope.sort = {
        column: '',
        descending: false
    };    

    $scope.nameGiven = true;

    $scope.start = function() {
      if ($scope.firstName == undefined || $scope.lastName == undefined || $scope.age == undefined || $scope.streetAddress == undefined || $scope.city == undefined || $scope.state == undefined || $scope.zipCode == undefined || $scope.phone == undefined){
        alert("Please fill out all fields");
      }
      else {
        $scope.nameGiven = false;
      }
      
      console.log("start running");
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
  when('/video', {
  templateUrl:'video.html',
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
