var INIT_LOCATION = new L.LatLng(44.574606,-123.27987); // OSU campus, Corvallis, OR
var AUTO_CENTER_MAP = true;
var ROUTER_ID = "";

var map = new L.Map('map', {
	minZoom : 6,
	maxZoom : 17,
	// what we really need is a fade transition between old and new tiles without removing the old ones
});

var OSMURL    = "http://{s}.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png";
var aerialURL = "http://{s}.mqcdn.com/tiles/1.0.0/sat/{z}/{x}/{y}.png";

var minimalLayer = new L.StamenTileLayer("toner");
//var mapboxAttrib = "Tiles from <a href='http://mapbox.com/about/maps' target='_blank'> Streets</a>";
//var minimalLayer = new L.TileLayer(CloudURL, {maxZoom: 17, attribution: mapboxAttrib});
var osmAttrib = 'Map data &copy; 2013 OpenStreetMap contributors';
var osmLayer = new L.TileLayer(OSMURL, 
		{subdomains: ["otile1","otile2","otile3","otile4"], maxZoom: 18, attribution: osmAttrib});

var aerialLayer = new L.TileLayer(aerialURL, 
		{subdomains: ["oatile1","oatile2","oatile3","oatile4"], maxZoom: 18, attribution: osmAttrib});

map.addLayer(osmLayer);
var Layers = 0;
function getdata(type,agency,route,variant,k,callback,popup) {	
	switch (type){
	case 1:
		var points = [];
		$.ajax({
			type: 'GET',
			datatype: 'json',
			url: '/opentripplanner-api-webapp/ws/transit/stops?&agency='+agency,
			success: function(d){		
			$.each(d.stops, function(i,stop){        	
				points.push([new L.LatLng(Number(stop.stopLat), Number(stop.stopLon)),stop.stopName]);	
				//points.push([Number(stop.stopLon),Number(stop.stopLat)]); 			
	        });				
			if (points.length!=0) callback("A"+agency,k,points,popup);
	    }});
		break;
	case 2:
		var points = [];
		$.ajax({
			type: 'GET',
			datatype: 'json',
			url: '/opentripplanner-api-webapp/ws/transit/stopsbyroute?&agency='+agency+'&route='+route,
			success: function(d){		
			$.each(d.stops, function(i,stop){        	
				points.push([new L.LatLng(Number(stop.stopLat), Number(stop.stopLon)),stop.stopName]);	
				//points.push([Number(stop.stopLon),Number(stop.stopLat)]); 			
	        });				
			if (points.length!=0) callback("R"+agency+route,k,points,popup);
	    }});
		break;
	case 3:
		//alert(agency+","+ route+","+variant);		
		$.ajax({
			type: 'GET',
			datatype: 'json',
			url: '/opentripplanner-api-webapp/ws/transit/shape?&agency='+agency+'&route='+route+'&variant='+variant,
			success: function(d){				
			if (d.shape.points!= null) callback(k,d.shape.points,"V"+agency+route+variant);
	    }});
		break;
	}		
};

var info = L.control();
info.onAdd = function (map) {
    this._div = L.DomUtil.create('div', 'info'); // create a div with a class "info"
    this.update();
    return this._div;
};

info.update = function (props) {
    this._div.innerHTML = (props ?
        '<div id="box"><b>County: </b>' + props.NAME + ' <br/>' + '<b>Area: </b>'+ props.CENSUSAREA + ' mi<sup>2</sup></div>' 
        :'');
};

function style(feature) {
    return {
        fillColor: 'orange',
        weight: 2,
        opacity: 1,
        color: 'white',
        dashArray: '3',
        fillOpacity: 0.1
    };
}
function zoomToFeature(e) {
    map.fitBounds(e.target.getBounds());
}
function resetHighlight(e) {
	shapes.resetStyle(e.target);
	info.update();
}
function highlightFeature(e) {
    var layer = e.target;
    layer.setStyle({              
        fillOpacity: 0
    });
    //layer.bringToFront(); //enable if borders are changed on mouseover
    info.update(layer.feature.properties);    
}
function onEachFeature(feature, layer) {
    layer.on({
        mouseover: highlightFeature,
        mouseout: resetHighlight,
        click: zoomToFeature        
    });
}
var stops = new L.LayerGroup().addTo(map);
var routes = new L.LayerGroup().addTo(map);

var shapes = L.geoJson(shapedata, {style: style, onEachFeature: onEachFeature});
//var uscounties = new L.LayerGroup(shapes);
//uscounties.addLayer(shapes);
//uscounties.onAdd = function (obj) {
//function uscountiesonadd(obj){
    //this._div = L.DomUtil.create('div', 'info'); // create a div with a class "info"
    //this.update();
    //return this._div;
//	info.addTo(obj);
//	info.update();
	//return this._div;
//};
//uscounties.addLayer(shapes);
//uscounties.addLayer(info);
//map.addControl(new info());

var colorset = ["#6ECC39","#FF33FF","#05FAFC","#FE0A0A", "#7A00F5", "#CC6600"];
function disponmap2(layerid,k,points,popup){	
	//alert (points.length);
	//alert (k);
	var geojsonMarkerOptions = {
		    radius: 5,
		    fillColor: colorset[k],
		    color: "#000",
		    weight: 1,
		    opacity: 1,
		    fillOpacity: 0.6
		};
	var geojsonFeature = {
		    "type": "Feature",
		    "properties": {
		        "name": "stop",
		        "popupContent": popup
		    },
		    "geometry": {
		        "type": "MultiPoint",
		        "coordinates": points
		    }
		};
	
	function onEachFeature(feature, layer) {
		var popupContent = "";

		if (feature.properties && feature.properties.popupContent) {
			popupContent += feature.properties.popupContent;
		}

		layer.bindPopup(popupContent);
	}
	var mylayer = new L.geoJson(geojsonFeature, {
	    pointToLayer: function (feature, latlng) {
	        return L.circleMarker(latlng, geojsonMarkerOptions);
	    },
	    onEachFeature: onEachFeature
	});
	
	mylayer._leaflet_id = layerid;
	//mylayer.bindpopup("test");
	stops.addLayer(mylayer);	
	//stops.bringToFront();
};

function disponmap(layerid,k,points,popup){
	//alert('test');
	
	//maxClusterRadius: 120,
	//iconCreateFunction: function (cluster) {
	//	return new L.DivIcon({ html: cluster.getChildCount(), className: 'mycluster', iconSize: new L.Point(40, 40) });
	//},
	var markers;
	switch (k){
	case 0:
		markers = new L.MarkerClusterGroup({
			maxClusterRadius: 120,
			iconCreateFunction: function (cluster) {
			return new L.DivIcon({ html: cluster.getChildCount(), className: 'gcluster', iconSize: new L.Point(30, 30) });
		},
		spiderfyOnMaxZoom: true, showCoverageOnHover: true, zoomToBoundsOnClick: true, singleMarkerMode: true, maxClusterRadius: 30
	});
	break;
	case 1:
		markers = new L.MarkerClusterGroup({
			maxClusterRadius: 120,
			iconCreateFunction: function (cluster) {
				return new L.DivIcon({ html: cluster.getChildCount(), className: 'picluster', iconSize: new L.Point(30, 30) });
			},
			spiderfyOnMaxZoom: true, showCoverageOnHover: true, zoomToBoundsOnClick: true, singleMarkerMode: true, maxClusterRadius: 30
		});
		break;
	case 2:
		markers = new L.MarkerClusterGroup({
			maxClusterRadius: 120,
			iconCreateFunction: function (cluster) {
				return new L.DivIcon({ html: cluster.getChildCount(), className: 'ccluster', iconSize: new L.Point(30, 30) });
			},
			spiderfyOnMaxZoom: true, showCoverageOnHover: true, zoomToBoundsOnClick: true, singleMarkerMode: true, maxClusterRadius: 30
		});
		break;
	case 3:
		markers = new L.MarkerClusterGroup({
			maxClusterRadius: 120,
			iconCreateFunction: function (cluster) {
				return new L.DivIcon({ html: cluster.getChildCount(), className: 'rcluster', iconSize: new L.Point(30, 30) });
			},
			spiderfyOnMaxZoom: true, showCoverageOnHover: true, zoomToBoundsOnClick: true, singleMarkerMode: true, maxClusterRadius: 30
		});
		break;
	case 4:
		markers = new L.MarkerClusterGroup({
			maxClusterRadius: 120,
			iconCreateFunction: function (cluster) {
				return new L.DivIcon({ html: cluster.getChildCount(), className: 'pucluster', iconSize: new L.Point(30, 30) });
			},
			spiderfyOnMaxZoom: true, showCoverageOnHover: true, zoomToBoundsOnClick: true, singleMarkerMode: true, maxClusterRadius: 30
		});
		break;
	case 5:
		markers = new L.MarkerClusterGroup({
			maxClusterRadius: 120,
			iconCreateFunction: function (cluster) {
				return new L.DivIcon({ html: cluster.getChildCount(), className: 'brcluster', iconSize: new L.Point(30, 30) });
			},
			spiderfyOnMaxZoom: true, showCoverageOnHover: true, zoomToBoundsOnClick: true, singleMarkerMode: true, maxClusterRadius: 30
		});
		break;
	}
	for (var i = 0; i < points.length; i++) {
		var p = points[i];
		var marker = new L.Marker(p[0],{title:popup});
		marker.bindPopup(p[1]);
		markers.addLayer(marker);
	}
	markers._leaflet_id = layerid;
	stops.addLayer(markers);
	
}

function dispronmap(k,points,name){	
	var polyline = L.Polyline.fromEncoded(points, {
		weight: 5,
		color: colorset[k],
		//fillColor: colorset[k],
		//color: "#000",
		//weight: 1,
		opacity: .5,
		//fillOpacity: 0.6
		smoothFactor: 1
		});	
		//mylayer.bindpopup(name);		
		polyline._leaflet_id = name;	
		routes.addLayer(polyline);
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
map.setView(initLocation,7);


// add layers to map 
// do not add analyst layer yet -- it will be added in refresh() once params are pulled in

var baseMaps = {
	    "OSM": osmLayer,
	    "Toner": minimalLayer,
	    "Aerial Photo": aerialLayer
	};
		        
var overlayMaps = {
		"Stops": stops,
		"Routes": routes,
		"Counties": shapes
	};

map.addControl(new L.Control.Layers(baseMaps,overlayMaps));
info.addTo(map);
var $mylist = $("#list");

$mylist
.jstree({
	"checkbox": {        
        two_state: true,
        real_checkboxes: false,
        override_ui:false
     },
	"json_data" : {
		"ajax" : {
            "url" : "/opentripplanner-api-webapp/ws/transit/menu",
            "type" : "get",	                
            "success" : function(ops) {            	
            	return ops.data;            	
            }    	               
        },
        "progressive_render" : true       
	},
	"types" : {
		"types": {
		"default": {
		"select_node" : false
		}
		}
	},
	"themes": {
        "theme": "default-rtl",
        "url": "js/lib/jstree-v.pre1.0/themes/default-rtl/style.css",
        "dots": false,
        "icons":false
    },
    "contextmenu" : {
        "items" : function (node) {
        	if ((node.attr("type"))!=="variant") {        	       	 
        	return { 
        		"show" : {
                    "label" : "Show Route Shapes",
                    "action" : function (node) { 
                    	//alert(node.attr("type"));
                    	switch (node.attr("type")){
                    	case "agency":                    		
                    		if ($.jstree._reference($mylist)._is_loaded(node)){
                    			$.each($.jstree._reference($mylist)._get_children(node), function(i,child){
                    				if ($.jstree._reference($mylist)._is_loaded(child)){
                    					$.each($.jstree._reference($mylist)._get_children(child), function(i,gchild){
            								$.jstree._reference($mylist).change_state(gchild, false);
            								});
                    				}else{
                    					$.jstree._reference($mylist).load_node_json(child, function(){$.each($.jstree._reference($mylist)._get_children(child), function(i,gchild){
            								$.jstree._reference($mylist).change_state(gchild, false);
            								});},function(){alert("Node Load Error");});
                    				}                        			
                                		});
                			} else{
                				$.jstree._reference($mylist).load_node_json(node,function(){$.each($.jstree._reference($mylist)._get_children(node), function(i,child){
                					if ($.jstree._reference($mylist)._is_loaded(child)){
                						$.each($.jstree._reference($mylist)._get_children(child), function(i,gchild){
            								$.jstree._reference($mylist).change_state(gchild, false);
            								});
                					}else{
                						$.jstree._reference($mylist).load_node_json(child, function(){$.each($.jstree._reference($mylist)._get_children(child), function(i,gchild){
            								$.jstree._reference($mylist).change_state(gchild, false);
            								});},function(){alert("Node Load Error");});
                					}                        			
                                		});},function(){alert("Node Load Error");});
                			}                    		                    		
	                    	break; 
                    	case "route":
                    		if ($.jstree._reference($mylist)._is_loaded(node)){
	                    		$.each($.jstree._reference($mylist)._get_children(node), function(i,child){
	                    		$.jstree._reference($mylist).change_state(child, false);
	                    		}); 
                    		}else {
                    			$.jstree._reference($mylist).load_node_json(node, function(){$.each($.jstree._reference($mylist)._get_children(node), function(i,child){
                            		$.jstree._reference($mylist).change_state(child, false);
                            		});},function(){alert("Node Load Error");});
                    		}
                    		break;
                    	}
                    }
                },
                "hide" : {
                    "label" : "Hide Route Shapes",
                    "action" : function (node) { 
                    	switch (node.attr("type")){
                    	case "agency":                    		
                    		$.each($.jstree._reference($mylist)._get_children(node), function(i,child){
                    			$.each($.jstree._reference($mylist)._get_children(child), function(i,gchild){
                            		$.jstree._reference($mylist).uncheck_node(gchild);
                            		});
                        		});
	                    	break; 
                    	case "route":
                    		$.each($.jstree._reference($mylist)._get_children(node), function(i,child){
                    		$.jstree._reference($mylist).uncheck_node(child);
                    		});
                    		break;
                    	}
                    }
                },
        	};        	     
                }            
        }
    },
	"plugins" : [ "themes","types","json_data", "checkbox", "sort", "ui" ,"contextmenu"]			
})
.bind("loaded.jstree", function (event, data) {
	$mylist
	.dialog({ 
		"title" : "Oregon Transportation Agencies", 
		width : 400,
		height: 720,
		maxHeight: 820,
		maxWidth: 600,
		closeOnEscape: false,
		position: [40,2],
		show: {
		 effect: "blind",
		 duration: 1000
		 }
		})	
	.dialogExtend({
	  "maximizable" : false,
	  "closable" : true,
	  "expandTitle":"test",
	  "minimizable" : true,
	  "collapsable" : true,
	  "dblclick" : "collapse",
	  "titlebar" : "transparent",
	  "icons" : { 
		  "collapse" : "ui-icon-circle-arrow-n",
	      "restore" : "ui-icon-newwin",
	      "close": "ui-icon-document",	     
	    },
	    //"events" : {
	    "load" : function(evt, dlg) {
		  $mylist.dialogExtend("collapse");
		  $("#minimize").attr("title", "Minimize");		  
		  //$('button').eq(0).attr('title','Reports');
		  //$('button').eq(1).attr('title','Extend');
		  //$('ui-button-icon-only').attr('title','test');
		  
		 //alert('hey');
		 //$('#collapse').attr('title','Minimize');
		 //$(function() {
		//	 $( document ).tooltip();
		//	 });		  
	    },
	    "restore": function(evt,dlg){
	    	$("#collapse").attr("title", "Collapse");
	    },
	    "minimize" : function(evt,dlg){
	    	$("#collapse").attr("title", "Restore");			
	    	//$('<div class="ui-dialog-titlebar-buttonpane"></div>').find('.ui-dialog-titlebar-restore').attr("title", "test");
	    	//$("#b2").attr("title", "test");
	    	//alert('minimized');
	    }
	    
	   //}
	});        
	//alert("TREE IS LOADED");
	    })
.bind("change_state.jstree", function (e, d) {
    var tagName = d.args[0].tagName;
    var refreshing = d.inst.data.core.refreshing;
    //alert(tagName);
    if ((tagName == "A" || tagName == "INS"|| tagName == "LI")&&(refreshing != true && refreshing != "undefined")) {
    	//alert(d.rslt.attr("id")+" value: "+$("#" + d.rslt.attr("id") + ".jstree-checked").length); 
    	//id = d.rslt.attr("id");
    	//alert(id+','+d.rslt.attr("type"));
    	node = d.rslt;
    	//alert(node.attr("type")=='variant');
    	switch (node.attr("type")){
    	case "agency":    		
    		//mynode = $("#" + node.attr("id") + ".jstree-checked");
    		//alert(mynode.length);
    		//checkbox is checked
    		if ($.jstree._reference($mylist).is_checked(node)){    			
    			//if it has any checked children   			
    			$.jstree._reference($mylist)._get_children(node).each( function( idx, listItem ) {    				
    				if ($.jstree._reference($mylist).is_checked($(listItem))){
	    				//alert($(listItem).attr("id"));
	    				//if (($("#" + $(listItem).attr("id") + ".jstree-checked").length)>0){ 
	    				//alert($(listItem).attr("id"));
	    				//Layers = Layers -1;    				
	    				stops.eachLayer(function (layer) {
	    				if (layer._leaflet_id == "R"+node.attr("id")+ $(listItem).attr("id")){
	    					stops.removeLayer(layer);
	    				}
	    				});
	    				$(listItem).css("background-color","");	    				
	    				//$("#" + $(listItem).attr("id") + ".jstree-checked").css("background-color","");
	    				$.jstree._reference($mylist).uncheck_node($(listItem));
    				};
                 });
    			//alert(d.rslt.attr("id"));
    			//var nodeid = d.rslt.attr("id").split("_")[1];
    			//alert(nodeid);
    			node.css("opacity", "1");
    			node.css("background-color", colorset[Layers%6]);    			
    			getdata(1,node.attr("id"),"","",Layers%6,disponmap,$.jstree._reference($mylist).get_text(node));
    			Layers = Layers + 1;
    			//var marker = L.marker([44.574606,-123.27987]).addTo(stops);
    		} else {    			
    			//mynode = $("#" + node.attr("id") + ".jstree-unchecked");
    			//Layers = Layers -1;
    			node.css("background-color","");
    			if ((($($mylist).jstree("get_checked",node,true)).length)>0) node.css("opacity", "0.6");
    			stops.eachLayer(function (layer) {
    			if (layer._leaflet_id == "A"+node.attr("id")){    					
    				stops.removeLayer(layer);
    				}
    			});	    				
    		}
    		break;
    	case "route":
    		//mynode = $("#" + node.attr("id") + ".jstree-checked");
    		if ($.jstree._reference($mylist).is_checked(node)){
    			//parent= $("#" + d.inst._get_parent(node).attr("id") + ".jstree-checked");
    			rparent = $.jstree._reference($mylist)._get_parent(node);
    			//parent = d.inst._get_parent(node);
    			if($.jstree._reference($mylist).is_checked(rparent)){
    			//if((parent.length)>0){
    				//Layers = Layers -1;
    				stops.eachLayer(function (layer) {
    				if (layer._leaflet_id == "A"+ rparent.attr("id")){
    				stops.removeLayer(layer);
    				}
    				});
    				rparent.css("background-color","");    				
    				$.jstree._reference($mylist).uncheck_node(rparent);
    			}
    			//alert(d.rslt.attr("type")+" : "+d.inst._get_parent(d.rslt).attr("id")+" : "+d.rslt.attr("id"));
    			//var nodeid = d.rslt.attr("id").split("_")[1];
    			//alert(nodeid);
    			//$.jstree._reference($mylist).uncheck_node(d.inst._get_parent(d.rslt));
    			//d.inst._get_parent(d.rslt).uncheck_node();
    			node.css("background-color", colorset[Layers%6]); 
    			//alert(parent.attr("id"));
    			//$("#" + parent.attr("id")).addClass("hasSelections");
    			//parent.addClass("hasSelections");
    			rparent.css("opacity", "0.6");
    			getdata(2,rparent.attr("id"),node.attr("id"),"",Layers%6,disponmap,$.jstree._reference($mylist).get_text(node));
    			Layers = Layers + 1;
    			//var marker = L.marker([44.574606,-123.27987]).addTo(stops);
    		} else {    			
    			//mynode = $("#" + node.attr("id") + ".jstree-unchecked");
    			//Layers = Layers -1;    			
    			//alert($.jstree._reference("#list").get_checked(rparent, ture));
    			if ((($($mylist).jstree("get_checked",rparent,true)).length)==0) rparent.css("opacity", "1");
    			node.css("background-color","");
    			//parent.removeClass("hasSelections");    			
    			stops.eachLayer(function (layer) {
    				if (layer._leaflet_id == "R"+d.inst._get_parent(node).attr("id")+node.attr("id")){
    				stops.removeLayer(layer);
    				}
    			});	    				
    		}
    		break;
    	case "variant":
    		vparent = $.jstree._reference($mylist)._get_parent(node);
    		//alert($.jstree._reference($mylist).is_checked(node));
    		//mynode = $("#" + node.attr("id")+ ".jstree-checked");    		
    		//alert(d.inst.is_checked(mynode));
    		if ($.jstree._reference($mylist).is_checked(node)){
    			//alert(id+','+d.rslt.attr("type"));
    			//alert(d.rslt.attr("id"));
    			//alert(d.rslt.attr("type")+", routeid: "+d.rslt.attr("id")+", variant: "+d.rslt.text()+", agencyid: "+ d.inst._get_parent((d.inst._get_parent(d.rslt))).attr("id"));
    			//var nodeid = d.rslt.attr("id").split("_")[1];
    			//alert(nodeid);
    			node.css("background-color", colorset[Layers%6]);
    			vparent.css("font-weight", "bold");
    			$.jstree._reference($mylist)._get_parent(vparent).css("opacity", "0.6");
    			getdata(3,d.inst._get_parent((d.inst._get_parent(node))).attr("id"),(d.inst._get_parent(node)).attr("id"),node.attr("id"),Layers%6,dispronmap,node.attr("id"));
    			Layers = Layers + 1;
    			//var marker = L.marker([44.574606,-123.27987]).addTo(stops);
    		} else {    			
    			//mynode = $("#" + d.rslt.attr("id") + ".jstree-unchecked");
    			//alert('unchecked'+(d.rslt.text()).replace(/^\s+|\s+$/g, ''));
    			//Layers = Layers -1;
    			node.css("background-color","");
    			if ((($($mylist).jstree("get_checked",vparent,true)).length)==0) {
    				vparent.css("font-weight", "normal");
    				if ((($($mylist).jstree("get_checked",$.jstree._reference($mylist)._get_parent(vparent),true)).length)==0) {
    					$.jstree._reference($mylist)._get_parent(vparent).css("opacity", "1");
    				}
    			}    			
    			routes.eachLayer(function (layer) {
    				if (layer._leaflet_id == "V"+d.inst._get_parent((d.inst._get_parent(node))).attr("id")+(d.inst._get_parent(node)).attr("id")+node.attr("id")){
    				routes.removeLayer(layer);
    				}
    			});	    				
    		}
    		break;
    	}    	
    };
});