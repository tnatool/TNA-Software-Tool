//This is My main Javascript function library
var rootURL = "http://localhost:8080/opentripplanner-api-webapp/ws";

//Register listeners
$('#btnSearch').click(function() {
	search($('#searchKey').val());
	return false;
});

function search(searchKey) {
	
		findByName(searchKey);
}

function test() {
	console.log('finding agnecies registered in the database');
	$.ajax({
		type: 'GET',
		url: rootURL + '/transit/agencyIds',
		dataType: "json",
		success: renderList 
	});
}

