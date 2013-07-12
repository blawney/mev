'use strict';

/* Controllers */

angular.module('myApp.controllers', []).
  controller('MyCtrl1', [function() {

  }])
  .controller('MyCtrl2', [function() {

  }])
  .controller('controllerUpload', ['$scope', function($scope){
		$scope.output = "Select your file to upload.";
    //Function to upload files
    $scope.sendFile = function(){
			//Variable declarations
			var input = document.getElementById("file");
			var bar = document.getElementById("bar"); //DOM element of the growing progress bar
			var percent = document.getElementById("percent"); //Value reported inside progress bar
			var upfile = input.files[0]; //Assign file object of the input file to a variable
			var formdata = new FormData; //Dynamically create a new form
			formdata.append('upload', upfile); //Append file object to the form
			$scope.output = "Uploading..."; //Alert user that uploading has begun
			//AJAX code
			var xhr = new XMLHttpRequest();
			//Event Listener that will provide progress to the user
			xhr.upload.addEventListener("progress", function(e){
				var pc = parseInt((e.loaded / e.total * 100)); //Calculate % completion
				//Update the progress elements with the percent completion quantity.
				bar.style.width = pc + "%";
				percent.innerHTML = pc + "%";
			});
			//Event Listener that will output the server response (if any) as well as update the user feedback elements to 100% completion.
			xhr.onreadystatechange = function(){
				if (xhr.readyState == 4 && xhr.status == 200){
					$scope.$apply(function(){ //$scope.$apply() is used so angular controller can update $scope.output upon the xhr.responseText assignment.
						$scope.output = xhr.responseText;
					});
					bar.style.width = "100%";
					percent.innerHTML = "100%";
				};
			};
			//Send the uploaded file.
			xhr.open('POST', 'http://bioed.bu.edu/cgi-bin/students_13/correiak/upload.py', true); //Action (2nd parameter) is currently set to my school webspace for testing purposes. To be changed.
			xhr.send(formdata);
    };
  }]);