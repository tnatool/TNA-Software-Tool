/* This program is free software: you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public License
   as published by the Free Software Foundation, either version 3 of
   the License, or (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>. 
*/

var INIT_LOCATION = new L.LatLng(44.574606,-123.27987); // OSU campus, Corvallis, OR
var AUTO_CENTER_MAP = false;
var ROUTER_ID = "";
var MSEC_PER_HOUR = 60 * 60 * 1000;
var MSEC_PER_DAY = MSEC_PER_HOUR * 24;
// Note: time zone does not matter since we are turning this back into text before sending it
var BASE_DATE_MSEC = new Date().getTime() - new Date().getTime() % MSEC_PER_DAY; 
// var BASE_DATE_MSEC = Date.parse('2012-11-15');


var map = new L.Map('map', {
	minZoom : 7,
	maxZoom : 17,
	// what we really need is a fade transition between old and new tiles without removing the old ones
});

var mapboxURL = "https://tiles.mapbox.com/v3/username.map-abcdefgh/{z}/{x}/{y}.png";
var OSMURL    = "http://{s}.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png";
var aerialURL = "http://{s}.mqcdn.com/naip/{z}/{x}/{y}.png";

var mapboxAttrib = "Tiles from <a href='http://mapbox.com/about/maps' target='_blank'> Streets</a>";
var mapboxLayer = new L.TileLayer(mapboxURL, {maxZoom: 17, attribution: mapboxAttrib});

var osmAttrib = 'Map data &copy; 2011 OpenStreetMap contributors';
var osmLayer = new L.TileLayer(OSMURL, 
		{subdomains: ["otile1","otile2","otile3","otile4"], maxZoom: 18, attribution: osmAttrib});

var aerialLayer = new L.TileLayer(aerialURL, 
		{subdomains: ["oatile1","oatile2","oatile3","oatile4"], maxZoom: 18, attribution: osmAttrib});

var flags = {
	twoEndpoint: false,
	twoSearch: false
};

// convert a map of query parameters into a query string, 
// expanding Array values into multiple query parameters
var buildQuery = function(params) {
	ret = [];
	for (key in params) {
		vals = params[key];
		// wrap scalars in array
		if ( ! (vals instanceof Array)) vals = new Array(vals);
		for (i in vals) { 
			val = vals[i]; // js iterates over indices not values!
			// skip params that are empty or stated to be the same as previous
			// if (val == '' || val == 'same')
			if (val == 'same') // empty string needed for non-banning
				continue;
			param = [encodeURIComponent(key), encodeURIComponent(val)].join('=');
			ret.push(param);
		}
	}
	return "?" + ret.join('&');
};

var analystUrl = "/opentripplanner-api-webapp/ws/tile/{z}/{x}/{y}.png"; 
var analystLayer = new L.TileLayer(analystUrl, {attribution: osmAttrib});

// create geoJSON layers for DC Purple Line



function refreshpoints(agency,k,callback) {	
	var points = [];
	$.ajax({
		type: 'GET',
		datatype: 'json',
		url: '/opentripplanner-api-webapp/ws/transit/allstops?&agency='+agency,
		success: function(d){		
		$.each(d.stops, function(i,stop){        	
				points.push([Number(stop.stopLon),Number(stop.stopLat)]); 			
        });	
		callback(k,points);
    }});	
};

var purpleLineCoords = [[-123.27987,44.574606]];

var purpleLineStopsFeature = { 
	"type": "Feature",
	"geometry": {
	    "type": "MultiPoint",
	    "coordinates": purpleLineCoords,
	    "properties": {
	        "name": "Purple Line stops"
	    }	
	}
};

var geojsonMarkerOptions = {
		radius: 4,
		fillColor: "#000",
		color: "#000",
		weight: 0,
		opacity: 0,
		fillOpacity: 0.8
};


//var purpleLineStopsLayer = new Array;
//var purpleLineStopsLayer = new L.GeoJSON(purpleLineStopsFeature, {
//	pointToLayer: function (latlng) { 
//		return new L.CircleMarker(latlng, geojsonMarkerOptions);
//	}});
	
//L.geoJson(purpleLineStopsFeature, {
//    pointToLayer: function (latlng) {
//        return L.circleMarker(latlng, geojsonMarkerOptions);
//    }
//}).addTo(map);

//L.geoJson(purpleLineStopsFeature).addTo(map);	
//map.addLayer(purpleLineStopsLayer);
//var myLayer = L.geoJson().addTo(map);

//myLayer.addData(purpleLineStopsFeature);

var purpleLineAlignmentFeature = { 
	"type": "Feature",
	"geometry": {
	    "type": "LineString",
	    "coordinates": purpleLineCoords,
	    "properties": {
	        "name": "Purple Line alignment",
	        "style": {
	            "color": "#004070",
	            "weight": 20,
	            "opacity": 0.8
	        }
	    }	
	}
};

var purpleLineAlignmentLayer = new L.GeoJSON(purpleLineAlignmentFeature);

var colorset = ["#8A2BE2","#0000FF","#8A2BE2",
                "#A52A2A","#7FFF00","#FF7F50","#B8860B","#8B008B",
                "#FF8C00","#FF1493","#800000","#FF0000","#8B4513",
                "#FFFF00","#708090","#2F4F4F","#008000","#E0FFFF",
                "#20B2AA","#808000","#FA8072","#C0C0C0","#9ACD32",
                "#D2B48C","#F0E68C"];

function disponmap(k,points){	
	purpleLineStopsFeature = { 
			"type": "Feature",
			"geometry": {
			    "type": "MultiPoint",
			    "coordinates": points,
			    "properties": {
			        "name": "Purple Line stops"
			    }	
			}
		};	
	geojsonMarkerOptions = {
			radius: 4,
			fillColor: colorset[k],
			color: colorset[k],
			weight: 0,
			opacity: 0,
			fillOpacity: 0.8
	};
	
	purpleLineStopsLayer = new L.GeoJSON(purpleLineStopsFeature, {
		pointToLayer: function (latlng) { 
			return new L.CircleMarker(latlng, geojsonMarkerOptions);
		}});
	
	mylayer.addData(purpleLineStopsLayer);

}
//map.addLayer(purpleLineStopsLayer);
var layers = 0;

function dispstops(){	
	$('input[type=checkbox]').parent().css('background','');
	//while (layers>0){
	map.removeLayer(purpleLineStopsLayer);
	//layers = layers - 1;
	//}	
	
	purpleLineStopsFeature = { 
			"type": "Feature",
			"geometry": {
			    "type": "MultiPoint",
			    "coordinates": [],
			    "properties": {
			        "name": "Purple Line stops"
			    }	
			}
		};	
	geojsonMarkerOptions = {
			radius: 4,
			fillColor: colorset[1],
			color: colorset[1],
			weight: 0,
			opacity: 0,
			fillOpacity: 0.8
	};
	
	purpleLineStopsLayer = new L.GeoJSON(purpleLineStopsFeature, {
		pointToLayer: function (latlng) { 
			return new L.CircleMarker(latlng, geojsonMarkerOptions);
		}});
	
	mylayer = map.addLayer(purpleLineStopsLayer);
	$("input:checkbox[name=agency]:checked").each(function()
			{
			layers = layers + 1;
			$(this).parent().css("background-color", colorset[layers]);			
			refreshpoints($(this).val(),layers,disponmap);   			
			});	
	};
function dispstops1(){	
	$('input[type=checkbox]').parent().css('background','');
	while (layers>0){
	map.removeLayer(purpleLineStopsLayer[layers]);
	layers = layers - 1;
	}	
	$("input:checkbox[name=agency]:checked").each(function()
			{
			layers = layers + 1;
			$(this).parent().css("background-color", colorset[layers]);			
			refreshpoints($(this).val(),layers,disponmap);   			
			});	
	};

var baseMaps = {
    "OSM": osmLayer,
    "MapBox": mapboxLayer,
    "Aerial Photo": aerialLayer
};
	        
var overlayMaps = {
    "Analyst Tiles": analystLayer,
    "Stops": purpleLineStopsLayer,
	"Alignment": purpleLineAlignmentLayer
};

var initLocation = INIT_LOCATION;
if (AUTO_CENTER_MAP) {
	// attempt to get map metadata (bounds) from server
	var request = new XMLHttpRequest();
	request.open("GET", "/opentripplanner-api-webapp/ws/metadata", false); // synchronous request
	request.setRequestHeader("Accept", "application/xml");
	request.send(null);
	if (request.status == 200 && request.responseXML != null) {
		var x = request.responseXML;
		var minLat = parseFloat(x.getElementsByTagName('minLatitude')[0].textContent);
		var maxLat = parseFloat(x.getElementsByTagName('maxLatitude')[0].textContent);
		var minLon = parseFloat(x.getElementsByTagName('minLongitude')[0].textContent);
		var maxLon = parseFloat(x.getElementsByTagName('maxLongitude')[0].textContent);
		var lon = (minLon + maxLon) / 2;
		var lat = (minLat + maxLat) / 2;
		initLocation = new L.LatLng(lat, lon);
	}
}
map.setView(initLocation, 16);
var initLocation2 = new L.LatLng(initLocation.lat + 0.05, initLocation.lng + 0.05);

//Marker icons

var greenMarkerIcon = new L.Icon({ iconUrl: 'js/lib/leaflet/images/marker-green.png' });
var redMarkerIcon = new L.Icon({ iconUrl: 'js/lib/leaflet/images/marker-red.png' });
var origMarker = new L.Marker(initLocation,  {draggable: true, icon: greenMarkerIcon });
var destMarker = new L.Marker(initLocation2, {draggable: true, icon: redMarkerIcon });
//origMarker.on('dragend', mapSetupTool);
//destMarker.on('dragend', mapSetupTool);

// add layers to map 
// do not add analyst layer yet -- it will be added in refresh() once params are pulled in

map.addLayer(osmLayer);
//map.addLayer(origMarker);
map.addControl(new L.Control.Layers(baseMaps, overlayMaps));

// use function statement rather than expression to allow hoisting -- is there a better way?
function mapSetupTool() {

	var params = { 
		batch: true,
	};

	// pull search parameters from form
	switch($('#searchTypeSelect').val()) {
	case 'single':
		params.layers = 'traveltime';
		params.styles = 'color30';
		break;
	case 'ppa':
		params.layers = 'hagerstrand';
		params.styles = 'transparent';
		break;
	case 'diff2':
		params.layers = 'difference';
		params.styles = 'difference';
		break;
	case 'diff1':
		params.layers = 'difference';
		params.styles = 'difference';
		params.bannedRoutes = ["Test_Purple", ""];
		break;
	}
	// store one-element arrays so we can append as needed for the second search
	params.time = [$('#setupTime').val()];
	params.mode = [$('#setupMode').val()];
	params.maxWalkDistance = [$('#setupMaxDistance').val()];
	params.arriveBy = [$('#arriveByA').val()];
	switch($('#compressWaits').val()) {
		case 'optimize':
			params.reverseOptimizeOnTheFly = ['true'];
			break;
		case 'initial':
		default:
			params.clampInitialWait = [$('#timeLenience').val() * 60];
	}
	if (flags.twoSearch) {
		var pushIfDifferent = function (elementId, paramName) {
			console.log(elementId);
			var elemval = document.getElementById(elementId).value;
			if (elemval != 'same') {
				params[paramName].push(elemval);
			}
		};
		var args = [['setupTime2', 'time'],
		            ['setupMode2', 'mode'],
		            ['setupMaxDistance2', 'maxWalkDistance'],
		            ['arriveByB', 'arriveBy']];
		for (i in args) {
			pushIfDifferent.apply(this, args[i]);
		}
	}
    
    // get origin and destination coordinate from map markers
	var o = origMarker.getLatLng();
	params.fromPlace = [o.lat + ',' + o.lng];
    if (flags.twoEndpoint) {
    	var d = destMarker.getLatLng();
    	params.fromPlace.push(d.lat + ',' + d.lng);
    }
	// set from and to places to the same string(s) so they work for both arriveBy and departAfter
	params.toPlace = params.fromPlace;
    	
    var URL = analystUrl + buildQuery(params);
    console.log(params);
    console.log(URL);
    
    // is there a better way to trigger a refresh than removing and re-adding?
	if (analystLayer != null)
		map.removeLayer(analystLayer);
	analystLayer._url = URL;
    map.addLayer(analystLayer);
	legend.src = "/opentripplanner-api-webapp/ws/legend.png?width=300&height=40&styles=" 
		+ params.styles;

	return false;
};     

var downloadTool = function () { 
    var dlParams = {
        format: document.getElementById('downloadFormat').value,
        srs: document.getElementById('downloadProj').value,
        resolution: document.getElementById('downloadResolution').value
    };

    // TODO: this bounding box needs to be reprojected!
    var bounds = map.getBounds();
    var bbox;

    // reproject
    var src = new Proj4js.Proj('EPSG:4326');
    // TODO: undefined srs?
    var dest = new Proj4js.Proj(dlParams.srs);

    // wait until ready then execute
    var interval;
    interval = setInterval(function () {
        // if not ready, wait for next iteration
        if (!(src.readyToUse && dest.readyToUse))
            return;

        // clear the interval so this function is not called back.
        clearInterval(interval);

        var swll = bounds.getSouthWest();
        var nell = bounds.getNorthEast();
        
        var sw = new Proj4js.Point(swll.lng, swll.lat);
        var ne = new Proj4js.Point(nell.lng, nell.lat);

        Proj4js.transform(src, dest, sw);
        Proj4js.transform(src, dest, ne);

        // left, bot, right, top
        bbox = [sw.x, sw.y, ne.x, ne.y].join(',');

        var url = '/opentripplanner-api-webapp/ws/wms' +
            buildQuery(params) +
            '&format=' + dlParams.format + 
            '&srs=' + dlParams.srs +
            '&resolution=' + dlParams.resolution +
            '&bbox=' + bbox;
            // all of the from, to, time, &c. is taken care of by buildQuery.
        
        window.open(url);
    }, 1000); // this is the end of setInterval, run every 1s

    // prevent form submission
    return false;
};

var displayTimes = function(fractionalHours, fractionalHoursOffset) {
	console.log("fhour", fractionalHours);
	// console.log("offset", fractionalHoursOffset);
	var msec = BASE_DATE_MSEC + fractionalHours * MSEC_PER_HOUR; 
	document.getElementById('setupTime').value = new Date(msec).toISOString().substring(0,19);
	msec += fractionalHoursOffset * MSEC_PER_HOUR; 
	document.getElementById('setupTime2').value = new Date(msec).toISOString().substring(0,19);
};

function setFormDisabled(formName, disabled) {
	var form = document.forms[formName];
    var limit = form.elements.length;
    var i;
    for (i=0;i<limit;i++) {
    	console.log('   ', form.elements[i], disabled);
        form.elements[i].disabled = disabled;
    }
}


/* Bind JS functions to events (handle almost everything at the form level) */

// anytime a form element changes, refresh the map
//$('#searchTypeForm').change( mapSetupTool );

// intercept slider change event bubbling to avoid frequent map rendering
(function(slider, offset) {
    slider.bind('change', function() {
    	displayTimes(slider.val(), offset.val()); 
        return false; // block event propagation
    }).change();
    slider.bind('mouseup', function() {
    	slider.parent().trigger('change');
    });
    offset.bind('change', function() {
    	displayTimes(slider.val(), offset.val()); 
    });
}) ($("#timeSlider"), $('#setupRelativeTime2'));

//hide some UI elements when they are irrelevant
$('#searchTypeSelect').change( function() { 
	var type = this.value;
	console.log('search type changed to', type);
	if (type == 'single' || type == 'diff1') {
		// switch to or stay in one-endpoint mode
		map.removeLayer(destMarker);
		flags.twoEndpoint = false;
	} else { 
		if (!(flags.twoEndpoint)) { 
			// switch from one-endpoint to two-endpoint mode
			var llo = origMarker.getLatLng();
			var lld = destMarker.getLatLng();
			lld.lat = llo.lat;
			lld.lng = llo.lng + 0.02;
			map.addLayer(destMarker);
			flags.twoEndpoint = true;
		}
	}
	if (type == 'single') {
		$('.secondaryControl').fadeOut( 500 );
		flags.twoSearch = false;
	} else { 
		$('.secondaryControl').fadeIn( 500 );
		flags.twoSearch = true;
	}
	if (type == 'ppa') {
		// lock arriveBy selectors and rename endpoints
		$('#headerA').text('Origin Setup');
		$('#headerB').text('Destination Setup');
		$('#arriveByA').val('false').prop('disabled', true);
		$('#arriveByB').val('true').prop('disabled', true);
	} else {
		$('#arriveByA').prop('disabled', false);
		$('#arriveByB').prop('disabled', false);
		if (type == 'single') {
			$('#headerA').text('Search Setup');
		} else {
			$('#headerA').text('Search A Setup');
			$('#headerB').text('Search B Setup');
		}
	}
}).change(); // trigger this event (and implicitly a form change event) immediately upon binding

