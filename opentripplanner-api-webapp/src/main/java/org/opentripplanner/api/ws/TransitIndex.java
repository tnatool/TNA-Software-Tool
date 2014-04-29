/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package org.opentripplanner.api.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jettison.json.JSONException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.FeedInfo;
import org.opentripplanner.api.model.error.TransitError;
import org.opentripplanner.api.model.transit.AgencyList;
import org.opentripplanner.api.model.transit.AgencyXR;
import org.opentripplanner.api.model.transit.CalendarData;
import org.opentripplanner.api.model.transit.AgencyRoute;
import org.opentripplanner.api.model.transit.AgencyRouteList;
import org.opentripplanner.api.model.transit.AgencySR;
import org.opentripplanner.api.model.transit.AgencySRList;
import org.opentripplanner.api.model.transit.Attr;
import org.opentripplanner.api.model.transit.Test;
import org.opentripplanner.api.model.transit.TripsList;
import org.opentripplanner.api.model.transit.ModeList;
import org.opentripplanner.api.model.transit.RouteData;
import org.opentripplanner.api.model.transit.RouteDataList;
import org.opentripplanner.api.model.transit.RouteList;
import org.opentripplanner.api.model.transit.RouteListR;
import org.opentripplanner.api.model.transit.RouteListm;
import org.opentripplanner.api.model.transit.RouteR;
import org.opentripplanner.api.model.transit.ServiceCalendarData;
import org.opentripplanner.api.model.transit.Shape;
import org.opentripplanner.api.model.transit.StopList;
import org.opentripplanner.api.model.transit.StopListR;
import org.opentripplanner.api.model.transit.StopR;
import org.opentripplanner.api.model.transit.StopTime;
import org.opentripplanner.api.model.transit.StopTimeList;
import org.opentripplanner.api.model.transit.VariantListm;
import org.opentripplanner.common.geometry.GeometryUtils;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.services.StreetVertexIndexService;
import org.opentripplanner.routing.services.TransitIndexService;
import org.opentripplanner.routing.transit_index.RouteSegment;
import org.opentripplanner.routing.transit_index.RouteVariant;
import org.opentripplanner.routing.transit_index.adapters.RouteType;
import org.opentripplanner.routing.transit_index.adapters.ServiceCalendarDateType;
import org.opentripplanner.routing.transit_index.adapters.ServiceCalendarType;
import org.opentripplanner.routing.transit_index.adapters.StopType;
import org.opentripplanner.routing.transit_index.adapters.TripType;
import org.opentripplanner.routing.transit_index.adapters.TripsModelInfo;
import org.opentripplanner.routing.vertextype.TransitStop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.sun.jersey.api.spring.Autowire;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;


// NOTE - /ws/transit is the full path -- see web.xml

@Path("/transit")
@XmlRootElement
@Autowire
public class TransitIndex {
	private static final Logger _log = LoggerFactory.getLogger(TransitIndex.class);
    private static final double STOP_SEARCH_RADIUS = 0.1; //or 0.1 miles

    private GraphService graphService;

    private static final long MAX_STOP_TIME_QUERY_INTERVAL = 86400 * 2;

    @Autowired
    public void setGraphService(GraphService graphService) {
        this.graphService = graphService;
    }

    /**
     * This is a test query
     */
    @GET
    @Path("/saeed")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object saeed(@QueryParam("routerId") String routerId) throws JSONException {

        return new TransitError("This is a test.");
    }
    /**
     * Returns Trips for the given agency
     */
    @GET
    @Path("/Trips")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getcalendar(@QueryParam("agency") String agency, @QueryParam("extended") Boolean extended, @QueryParam("routerId") String routerId) throws JSONException {
    	
    	TransitIndexService transitIndexService = getGraph(routerId).getService(
	            TransitIndexService.class);
		if (transitIndexService == null) {
	        return new TransitError(
	                "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
	    }
		//variant.getTrips()
        TripsList response = new TripsList();
        
       
        //List <ServiceCalendar> calendar = new ArrayList<ServiceCalendar>();
        //calendar = transitIndexService.getca
        
        return response;
    }
    /**
     * Return a list of all stops for a given agency in the graph
     */
    @GET
    @Path("/allstops")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object allstops(@QueryParam("agency") String agency, @QueryParam("extended") Boolean extended, @QueryParam("routerId") String routerId) throws JSONException {

        Graph graph = getGraph(routerId);
        StopList response = new StopList();
                
        for (Vertex gv : graph.getVertices()){
        	if (gv instanceof TransitStop){
        		if (agency != null && agency.equals((((TransitStop)gv).getStopId()).getAgencyId())){
        			StopType stop = new StopType(((TransitStop)gv).getStop(), extended);
        			response.stops.add(stop);        		         		
        			}
        	}
        }
        return response;
    } 
    
    /**
     * Return a list of all stops for a given agency in the graph
     */
    @GET
    @Path("/stops")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object stops(@QueryParam("agency") String agency, @QueryParam("extended") Boolean extended, @QueryParam("routerId") String routerId) throws JSONException {
		TransitIndexService transitIndexService = getGraph(routerId).getService(
	            TransitIndexService.class);
		if (transitIndexService == null) {
	        return new TransitError(
	                "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
	    }
		StopList response = new StopList();		
		ArrayList<String> stopids = new ArrayList<String>();
		Collection<AgencyAndId> allRouteIds = transitIndexService.getAllRouteIds();
		//transitIndexService.get
		for (AgencyAndId routeId : allRouteIds) {
			if (routeId.getAgencyId().equals(agency)) {
				for (RouteVariant variant : transitIndexService.getVariantsForRoute(routeId)) {
					
					Route route = variant.getRoute();
					if (agency != null && agency.equals(route.getAgency().getId())){
						for (org.onebusaway.gtfs.model.Stop stop : transitIndexService.getStopsForRoute(routeId))			        
							if (!(stopids.contains(stop.getId().getId()))){
								stopids.add(stop.getId().getId());
								response.stops.add(new StopType(stop, extended));
				        }
					}
					break;
				}
			}
		}
		return response;
    }
    
    /**
     * Return a list of all stops for a given agency and route in the graph
     */
    @GET
    @Path("/stopsbyroute")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object stopsbyroute(@QueryParam("agency") String agency, @QueryParam("route") String route,@QueryParam("extended") Boolean extended, 
    		@QueryParam("routerId") String routerId) throws JSONException {
    	TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
    	if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }
    	StopList response = new StopList();
    	//for (String agencyId : getAgenciesIds(agency, routerId)) {
	    	AgencyAndId routeId = new AgencyAndId(agency, route);    	
	    	for (org.onebusaway.gtfs.model.Stop stop : transitIndexService.getStopsForRoute(routeId))
	            response.stops.add(new StopType(stop, extended));	    	
    	//}
    	return response;
    	
    }
    /**
     * Return shape for a given agency, route and variant
     */
    @GET
    @Path("/shape")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getshape(@QueryParam("agency") String agency, @QueryParam("route") String route, @QueryParam("variant") String variant,@QueryParam("extended") Boolean extended, 
    		@QueryParam("routerId") String routerId) throws JSONException {
    	TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
    	if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }
    	Shape response = new Shape();
    	AgencyAndId routeId = new AgencyAndId(agency, route); 
    	for (RouteVariant vr : transitIndexService.getVariantsForRoute(routeId)) {
				if (variant != null && variant.equals(vr.getName())){
					response.shape = vr.getGeometry();
				}
			}		
		return response;    	
    }
    /**
     * Return coordinates for a shape specified by agency, route and variant
     */
    @GET
    @Path("/coords")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getcoords(@QueryParam("agency") String agency, @QueryParam("route") String route, @QueryParam("variant") String variant,@QueryParam("extended") Boolean extended, 
    		@QueryParam("routerId") String routerId) throws JSONException {
    	TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
    	if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }
    	Test response = new Test();
    	AgencyAndId routeId = new AgencyAndId(agency, route); 
    	for (RouteVariant vr : transitIndexService.getVariantsForRoute(routeId)) {
				if (variant != null && variant.equals(vr.getName())){
					Coordinate[] points = vr.getGeometry().getCoordinates();
					double SL = 0;
					for (int k =0; k<points.length-1; k++){
					SL += ddistance(points[k].y,points[k].x,points[k+1].y,points[k+1].x);
					}
					response.test = String.valueOf(SL);
					//response.test = points[290].toString();
				}
			}		
		return response;    	
    }
    
    /**
     * Return a Sorted by agency id list of all agency ids in the graph; couldn't make it work because the add. method seem not working the way it is supposed to.
     */
    @GET
    @Path("/agencies")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getAgencies(@QueryParam("routerId") String routerId) throws JSONException {
    	
    	Graph graph = getGraph(routerId);    	
        AgencyList allagns = new AgencyList();
        allagns.agencies = graph.getAgencies();               
        AgencyList response = new AgencyList();
        ArrayList <String> allagencies = getAgenciesIds("", routerId); 
        Collections.sort(allagencies);
        //for (String id : allagencies){
        	for (Agency agency : allagns.agencies){
        		//if (!id.equals(agency.getId()))
        			//continue;
        		response.agencies.add(agency);    
        		
        		//break;
        	}
        	
       // }
        //Graph graph = getGraph(routerId);

        
        
        
        return response;
    }
    /**
     * Return a list of all agency ids in the graph
     */
    @GET
    @Path("/agencyIds")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getAgencyIds(@QueryParam("routerId") String routerId) throws JSONException {
    	
    	Graph graph = getGraph(routerId);    	
        AgencyList response = new AgencyList();
        response.agencies = graph.getAgencies();  
        
        //Collections.sort(response.agencies, new Comparator<Agency>(){        	
        //    public int compare(Agency o1, Agency o2) {
        //    return o1.getId().compareTo(o2.getId()); 
         //   }
        //});
        return response;
    }
    
    /**
     * Generates a sorted by agency id list of routes for the LHS menu
     */
    @GET
    @Path("/menu")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getmenu(@QueryParam("routerId") String routerId) throws JSONException {
    	Graph graph = getGraph(routerId);
    	AgencyList allagencies = new AgencyList();
    	allagencies.agencies = graph.getAgencies();
    	TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
        Collection<AgencyAndId> allRouteIds = transitIndexService.getAllRouteIds();        
        AgencyRouteList response = new AgencyRouteList();    	
        for (Agency instance : allagencies.agencies){
        	AgencyRoute each = new AgencyRoute();
        	Attr attribute = new Attr();        	
        	//Integer rtcount = 0;
        	attribute.id = instance.getId();
        	attribute.type = "agency";        	
        	each.state = "closed";        	
        	each.attr = attribute;        	
    		for (AgencyAndId routeId : allRouteIds) {
    			Boolean Rtfound = false;    			
    			RouteListm eachO = new RouteListm();
	            for (RouteVariant variant : transitIndexService.getVariantsForRoute(routeId)) {
	                Route route = variant.getRoute();	                 
	                if (instance.getId().equals(route.getAgency().getId()))
	                    {	                	 
	                	//Integer Stpcount = variant.getStops().size();	                	
	                	VariantListm eachV = new VariantListm();		            			                	
		                if (!Rtfound) {
		                	Rtfound = true;
		                	//rtcount++;
			            	String str = null;
			                if (route.getLongName()!= null) str = route.getLongName();		                
			                if ((route.getShortName()!= null)){
			                	if (str != null){
			                		str = str + "(" + route.getShortName()+ ")";		                		
			                	} else {
			                		str = route.getShortName();
			                	};
			                };			                
			                eachO.data = str ;//+ "[" + String.valueOf(Stpcount)+ "]";
			                eachO.state = "closed";
			                attribute = new Attr();
			                attribute.id = route.getId().getId();
			                attribute.type = "route";			                
			                eachO.attr = attribute;
			            	
		                };		                
		                attribute = new Attr();
		                eachV.data = variant.getName();//+ "[" + String.valueOf(Stpcount)+ "]";
		                eachV.state = "leaf";
		                attribute.id = variant.getName();
		                attribute.type = "variant";		                
		                eachV.attr = attribute;
		            	eachO.children.add(eachV) ;       
	                    }	                
	            }
	    		each.children.add(eachO);   
	        }
    		each.data = instance.getName();//+ "[" + String.valueOf(rtcount)+ "]";
        	response.data.add(each);
        	
        }        
        return response;
    }
    /**
     * Generates The Routes report
     */
    @GET
    @Path("/RoutesR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getTAR(@QueryParam("agency") String agency, @QueryParam("x") double x, @QueryParam("day") Integer day, @QueryParam("routerId") String routerId) throws JSONException {
    	
    	Graph graph = getGraph(routerId);
    	Graph census = getGraph("census");
    	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
    	x = x * 1609.34;
    	ArrayList<Integer> days = new ArrayList<Integer>();    	
    	while (day > 0) {
    	    days.add( day % 10);
    	    day = day / 10;
    	}
    	StreetVertexIndexService streetVertexIndexService = census.streetIndex;
    	AgencyList allagencies = new AgencyList();
    	allagencies.agencies = graph.getAgencies();
    	TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
    	List<ServiceCalendar> scList = transitIndexService.getCalendarsByAgency(agency);
        Collection<AgencyAndId> allRouteIds = transitIndexService.getAllRouteIds();        
        RouteListR response = new RouteListR();   	
        for (Agency instance : allagencies.agencies){
        	if (instance.getId().equals(agency))response.AgencyName = instance.getName(); 
        }	
    		for (AgencyAndId routeId : allRouteIds) {
    			if (routeId.getAgencyId().equals(agency)) {
    			double ServiceMiles = 0;
    		    double Stopportunity = 0;
    		    double PopStopportunity = 0;
    			double RouteMiles = 0;
    			double length = 0;
    			RouteR each = new RouteR();
    			Boolean checked = false;
    			ArrayList<String> centroidids = new ArrayList<String>();
            	//ArrayList<String> routeids = new ArrayList<String>();            	
    			//filter routeId by ageny ids instead of variant.getroute().getagency.getid
            	for (RouteVariant variant : transitIndexService.getVariantsForRoute(routeId)) {
	                Route route = variant.getRoute();	                
	                //Coordinate[] myroute = variant.getGeometry().getCoordinates();	                
	                //int length = myroute.length;
	                if (!checked) {//this assignments has to be done once per route
		                each.RouteId = route.getId().getId();
		                each.RouteLName = route.getLongName();
		                each.RouteSName = route.getShortName();
		                each.RouteDesc = route.getDesc();
		                each.RouteType = String.valueOf(route.getType());
		                each.StopsCount = String.valueOf(transitIndexService.getStopsForRoute(route.getId()).size());
		                   	//routeids.add(route.getId().getId());	                		
		                	//routelist.routes.add(routeType);
	                	double pop = 0;
	                	for (org.onebusaway.gtfs.model.Stop stop : transitIndexService.getStopsForRoute(route.getId())){
	                		List<TransitStop> centroids = streetVertexIndexService.getNearbyTransitStops(new Coordinate(
	                                stop.getLon(), stop.getLat()), x);                		
	                		for (TransitStop centroid : centroids) {	                            
	                			if (!centroidids.contains(centroid.getStopId().getId())){
			        	            centroidids.add(centroid.getStopId().getId());
			        	            pop += Integer.parseInt(centroid.getName());		                        	
	                			}                        	
	                        }	                		
	                	}
	                	each.PopWithinX = String.valueOf(Math.round(pop));
		                	//Stops = transitIndexService.getStopsForRoute(route.getId()).size()+ Stops;
		                	//ServiceMiles = length + ServiceMiles;	                	
	                	checked = true;
	                    }
	                Coordinate[] points = variant.getGeometry().getCoordinates();
					ArrayList<String> varcentroidids = new ArrayList<String>();					
					double SL = 0;
					double varpop = 0;					
			        int frequency = 0;			        
					for (int k =0; k<points.length-1; k++){ //computing variant length
					SL += ddistance(points[k].y,points[k].x,points[k+1].y,points[k+1].x);
					}					
					for (Stop stp: variant.getStops()){ //computing population served for variant
						for (TransitStop centroid : streetVertexIndexService.getNearbyTransitStops(new Coordinate(stp.getLon(), stp.getLat()), x)) {//computing population around a stop	                            
		        			if (!varcentroidids.contains(centroid.getStopId().getId())){
		        	            varcentroidids.add(centroid.getStopId().getId());
		        	            varpop += Integer.parseInt(centroid.getName());		        	            		        	            
		        			}                        	
		                }														
					}
					for (TripsModelInfo tmi : variant.getTrips()){ //computing total frequency for the given variant
						String sid = tmi.getCalendarId();
						Integer freq = tmi.getNOT();						
						if (scList != null) {
						for (ServiceCalendar sc : scList){
							if (sid.equals(sc.getServiceId().getId())){
								for (int dday :days){
								switch (dday){
									case 1:
										if (sc.getSaturday()==1){
											frequency += freq;											
										}
										break;
									case 2:
										if (sc.getSunday()==1){
											frequency += freq;
										}
										break;
									case 3:
										if (sc.getMonday()==1){
											frequency += freq;
										}
										break;
									case 4:
										if (sc.getTuesday()==1){
											frequency += freq;
										}
										break;
									case 5:
										if (sc.getWednesday()==1){
											frequency += freq;
										}
										break;
									case 6:
										if (sc.getThursday()==1){
											frequency += freq;
										}
										break;
									case 7:
										if (sc.getFriday()==1){
											frequency += freq;
										}
										break;
								}}}}
							} else {
								for (int dday :days){
								frequency += freq;
								}
							}							
					}
					ServiceMiles += SL * frequency;
					Stopportunity += frequency * variant.getStops().size();
					PopStopportunity += frequency * variant.getStops().size()* varpop;
					length = Math.max(length, SL);
	            }            	 
            	each.RouteLength = String.valueOf(Math.round(length*100.0)/100.0);                
                each.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0); 
                each.Stopportunity = String.valueOf(Math.round(Stopportunity));;
                each.PopStopportunity = String.valueOf(Math.round(PopStopportunity));
            	response.RouteR.add(each);            	
    		}
	        }                 
        return response;    
    }
    /**
     * Generates The Routes report
     */
    @GET
    @Path("/StopsR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getTAS(@QueryParam("agency") String agency, @QueryParam("x") double x, @QueryParam("day") Integer day, @QueryParam("route") String routeid,@QueryParam("routerId") String routerId) throws JSONException {
    	
    	Graph graph = getGraph(routerId);
    	Graph census = getGraph("census");
    	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
    	x = x * 1609.34;
    	StreetVertexIndexService streetVertexIndexService = census.streetIndex;    	
    	TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
        Collection<AgencyAndId> allRouteIds = transitIndexService.getAllRouteIds();         
        StopListR response = new StopListR();
        ArrayList<String> stopids = new ArrayList<String>();             
        Agency theagency = transitIndexService.getAgency(agency);
        response.AgencyName = theagency.getName();        	
    		for (AgencyAndId routeId : allRouteIds) {
    			if ((routeId.getId().equals(routeid))||(routeid == null)) {
	    			if (routeId.getAgencyId().equals(agency)) {		    			
		            	//ArrayList<String> routeids = new ArrayList<String>();            	
		    			//filter routeId by ageny ids instead of variant.getroute().getagency.getid
		            	for (RouteVariant variant : transitIndexService.getVariantsForRoute(routeId)) {
			                Route route = variant.getRoute();			                
			                for (org.onebusaway.gtfs.model.Stop stop : transitIndexService.getStopsForRoute(route.getId()))	{		        
								if (!(stopids.contains((stop.getId()).getId()))){
									StopR each = new StopR();
									ArrayList<String> centroidids = new ArrayList<String>();
									stopids.add(stop.getId().getId());
									each.StopId = stop.getId().getId();
									each.StopName = stop.getName();
									each.URL = stop.getUrl();
									each.Routes = "";
									ArrayList<String> myrouteids = new ArrayList<String>();
									for (AgencyAndId myroutes : transitIndexService.getRoutesForStop(stop.getId())){
										if (!(myrouteids.contains(myroutes.getId()))){
											myrouteids.add(myroutes.getId());
											each.Routes += myroutes.getId()+" ;";
											}
										}
									if ((each.Routes).length() >1) each.Routes = (each.Routes).substring(0,(each.Routes).length()-1);//deleting unnecessary comma from the end of routes string
									double pop = 0;
									List<TransitStop> centroids = streetVertexIndexService.getNearbyTransitStops(new Coordinate(
			                                stop.getLon(), stop.getLat()), x);                		
			                		for (TransitStop centroid : centroids) {	                            
			                			if (!centroidids.contains(centroid.getStopId().getId())){
					        	            centroidids.add(centroid.getStopId().getId());
					        	            pop += Integer.parseInt(centroid.getName());		                        	
			                			}                        	
			                        }
			                		each.PopWithinX = String.valueOf(Math.round(pop));
			                		response.StopR.add(each);			                		
								}
			                }
			                break;		                	
			                	//Stops = transitIndexService.getStopsForRoute(route.getId()).size()+ Stops;
			                	//ServiceMiles = length + ServiceMiles;		                				                    
			            }
	    			}
    			}	
	        }                 
        return response;
    }
    /**
     * Generates The Agency Summary report
     */
    @GET
    @Path("/AgencySR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getASR(@QueryParam("x") double x, @QueryParam("routerId") String routerId) throws JSONException {
    	Graph graph = getGraph(routerId);
    	Graph census = getGraph("census");
    	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
    	x = x * 1609.34;
    	StreetVertexIndexService streetVertexIndexService = census.streetIndex;
    	AgencyList allagencies = new AgencyList();
    	allagencies.agencies = graph.getAgencies();
    	TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
        Collection<AgencyAndId> allRouteIds = transitIndexService.getAllRouteIds();        
        AgencySRList response = new AgencySRList();    	
        for (Agency instance : allagencies.agencies){
        	ArrayList<String> centroidids = new ArrayList<String>();
        	double pop = 0;
        	AgencySR each = new AgencySR();
        	each.AgencyName = instance.getName();
        	each.AgencyId = instance.getId();
        	each.Phone = instance.getPhone();
        	each.URL = instance.getUrl();
        	each.FareURL = instance.getFareUrl();        	
        	ArrayList<String> stopids = new ArrayList<String>();        	
			int routecnt = 0;
        	//double ServiceMiles = 0;        	
    		for (AgencyAndId routeId : allRouteIds) {    			
	            if (instance.getId().equals(routeId.getAgencyId())){
	            	routecnt++;
	            	for (org.onebusaway.gtfs.model.Stop stop : transitIndexService.getStopsForRoute(routeId))	{		        
						if (!(stopids.contains((stop.getId()).getId()))){
							stopids.add(stop.getId().getId());
							List<TransitStop> centroids = streetVertexIndexService.getNearbyTransitStops(new Coordinate(stop.getLon(), stop.getLat()), x);
							for (TransitStop centroid : centroids) {	                            
	                			if (!centroidids.contains(centroid.getStopId().getId())){
			        	            centroidids.add(centroid.getStopId().getId());
			        	            pop += Integer.parseInt(centroid.getName());		                        	
	                			}                        	
	                        }
						}						
	            	//double length = 0;	            	
	    			//for (RouteVariant variant : transitIndexService.getVariantsForRoute(routeId)) {
	    			//	Coordinate[] points = variant.getGeometry().getCoordinates();
					//	double SL = 0;
					//	for (int k =0; k<points.length-1; k++){
					//	SL += ddistance(points[k].y,points[k].x,points[k+1].y,points[k+1].x);
					//	}		              
		                //List <AgencyAndId> Trips = variant.getTrips();
		                //for (AgencyAndId trip : Trips){
		                //	graph.getCalendarService().getServiceIdsOnDate(arg0);
						
		                //}		                
		            //    length = Math.max(length, SL);
		            //}
	    			//ServiceMiles+= length;
	            	}	            
	            }
    		}
    		each.RoutesCount = String.valueOf(routecnt) ;
            each.StopsCount = String.valueOf(stopids.size());
            //each.RouteMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0);
            each.PopServed = String.valueOf(Math.round(pop));
            response.agencySR.add(each);
        }
    return response;
    }
    /**
     * Generates The Agency Extended report
     */
    @GET
    @Path("/AgencyXR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getAXR(@QueryParam("agency") String agency, @QueryParam("day") Integer day,@QueryParam("x") double x, @QueryParam("routerId") String routerId) throws JSONException {
    	//Graph graph = getGraph(routerId);
    	Graph census = getGraph("census");
    	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
    	x = x * 1609.34;
    	ArrayList<Integer> days = new ArrayList<Integer>();    	
    	while (day > 0) {
    	    days.add( day % 10);
    	    day = day / 10;
    	}
    	StreetVertexIndexService streetVertexIndexService = census.streetIndex;
    	TransitIndexService transitIndexService = getGraph(routerId).getService(TransitIndexService.class);
    	if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found. Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }
    	Agency theagency = transitIndexService.getAgency(agency);
    	AgencyXR response = new AgencyXR();
    	response.AgencyId = theagency.getId();
    	response.AgencyName = theagency.getName();
    	List<ServiceCalendar> scList = transitIndexService.getCalendarsByAgency(agency);    		
    	ArrayList<String> centroidids = new ArrayList<String>();    	
        ArrayList<String> stopids = new ArrayList<String>();        	
        double pop = 0;
        double ServiceMiles = 0;
        double ServiceStops = 0;
        //double Stopportunity = 0;
        //double PopStopportunity = 0;
        double PopServedByService = 0;
		double RouteMiles = 0;
		double StopCount = 0;
    	for (AgencyAndId routeId : transitIndexService.getAllRouteIds()) {    			
    		if ((theagency.getName()).equals((transitIndexService.getAgency(routeId.getAgencyId())).getName())){
		        for (org.onebusaway.gtfs.model.Stop stop : transitIndexService.getStopsForRoute(routeId))	{//computing served population		        
					if (!(stopids.contains((stop.getId()).getId()))){
						stopids.add(stop.getId().getId());
						StopCount++;
						for (TransitStop centroid : streetVertexIndexService.getNearbyTransitStops(new Coordinate(stop.getLon(), stop.getLat()), x)) {//computing population around a stop	                            
		        			if (!centroidids.contains(centroid.getStopId().getId())){
		        	            centroidids.add(centroid.getStopId().getId());
		        	            pop += Integer.parseInt(centroid.getName());		        	            		        	            
		        			}                        	
		                }
					}
					}
		        double length = 0;		        		        
				for (RouteVariant variant : transitIndexService.getVariantsForRoute(routeId)) { //loop over variants for the given route
					Coordinate[] points = variant.getGeometry().getCoordinates();
					ArrayList<String> varcentroidids = new ArrayList<String>();					
					double SL = 0;
					//int VariantStops = variant.getStops().size();
					double varpop = 0;					
			        int frequency = 0;			        
					for (int k =0; k<points.length-1; k++){ //computing variant length
					SL += ddistance(points[k].y,points[k].x,points[k+1].y,points[k+1].x);
					}					
					for (Stop stp: variant.getStops()){ //computing population served for variant
						for (TransitStop centroid : streetVertexIndexService.getNearbyTransitStops(new Coordinate(stp.getLon(), stp.getLat()), x)) {//computing population around a stop	                            
		        			if (!varcentroidids.contains(centroid.getStopId().getId())){
		        	            varcentroidids.add(centroid.getStopId().getId());
		        	            varpop += Integer.parseInt(centroid.getName());		        	            		        	            
		        			}                        	
		                }														
					}
					
					for (TripsModelInfo tmi : variant.getTrips()){ //computing total frequency for the given variant
						String sid = tmi.getCalendarId();
						Integer freq = tmi.getNOT();						
						if (scList != null) {
						for (ServiceCalendar sc : scList){
							if (sid.equals(sc.getServiceId().getId())){
								for (int dday :days){
									switch (dday){
										case 1:
											if (sc.getSaturday()==1){
												frequency += freq;
												//ServiceMiles += SL * freq ;
											}
											break;
										case 2:
											if (sc.getSunday()==1){
												frequency += freq;
												//ServiceMiles += SL * freq ;
											}
											break;
										case 3:
											if (sc.getMonday()==1){
												frequency += freq;
												//ServiceMiles += SL * freq ;
											}
											break;
										case 4:
											if (sc.getTuesday()==1){
												frequency += freq;
												//ServiceMiles += SL * freq ;
											}
											break;
										case 5:
											if (sc.getWednesday()==1){
												frequency += freq;
												//ServiceMiles += SL * freq ;
											}
											break;
										case 6:
											if (sc.getThursday()==1){
												frequency += freq;
												//ServiceMiles += SL * freq ;
											}
											break;
										case 7:
											if (sc.getFriday()==1){
												frequency += freq;
												//ServiceMiles += SL * freq ;
											}
											break;
									}
								}
							}}
							} else {
								for (int dday :days){
									frequency += freq;
									//ServiceMiles += SL * freq ;
								}
							}							
					}
					//ServiceStops += frequency * VariantStops;
					ServiceMiles += SL * frequency;
					//PopServedByService += frequency * varpop;
					ServiceStops += frequency * variant.getStops().size();
					PopServedByService += frequency * varpop;
					
					length = Math.max(length, SL);
	            }
					RouteMiles += length;	            		            
	            }
    		}
    		response.RouteMiles = String.valueOf(Math.round(RouteMiles*100.0)/100.0);
            response.PopServed = String.valueOf(Math.round(pop));
            response.PopServedByService = String.valueOf(Math.round(PopServedByService));
            response.ServiceStops = String.valueOf(Math.round(ServiceStops));
            response.StopCount = String.valueOf(Math.round(StopCount));
            response.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0); 
            //response.Stopportunity = String.valueOf(Math.round(Stopportunity));;
            //response.PopStopportunity = String.valueOf(Math.round(PopStopportunity));
            response.StopPerRouteMile = String.valueOf(Math.round((StopCount*10000.0)/(RouteMiles))/10000.0);
    return response;
    }
    
    /**
     * Return data about a route, such as its variants and directions, that OneBusAway's API doesn't handle
     */
    @GET
    @Path("/routeData")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getRouteData(@QueryParam("agency") String agency, @QueryParam("id") String id,
            @QueryParam("references") Boolean references, @QueryParam("extended") Boolean extended,
            @QueryParam("routerId") String routerId) throws JSONException {

        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }
        RouteDataList respond = new RouteDataList();

        for (String agencyId : getAgenciesIds(agency, routerId)) {
            AgencyAndId routeId = new AgencyAndId(agencyId, id);

            List<RouteVariant> variants = transitIndexService.getVariantsForRoute(routeId);

            if (variants.isEmpty())
                continue;

            RouteData response = new RouteData();
            response.id = routeId;
            response.variants = variants;            
            response.directions = new ArrayList<String>(
                    transitIndexService.getDirectionsForRoute(routeId));
            response.route = new RouteType();
            for (RouteVariant variant : transitIndexService.getVariantsForRoute(routeId)) {
            	Route route = variant.getRoute();
            	response.route = new RouteType(route, extended);
            	break;
            }

            if (references != null && references.equals(true)) {
                response.stops = new ArrayList<StopType>();
                for (org.onebusaway.gtfs.model.Stop stop : transitIndexService
                        .getStopsForRoute(routeId))
                    response.stops.add(new StopType(stop, extended));
            }

            respond.routeData.add(response);
        }

        return respond;
    }

    /**
     * Return a list of route ids
     */
    @GET
    @Path("/routes")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getRoutes(@QueryParam("agency") String agency,
            @QueryParam("extended") Boolean extended, @QueryParam("routerId") String routerId)
            throws JSONException {

        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }
        Collection<AgencyAndId> allRouteIds = transitIndexService.getAllRouteIds();
        RouteList response = makeRouteList(allRouteIds, agency, extended, routerId);
        return response;
    }

    private RouteList makeRouteList(Collection<AgencyAndId> routeIds, String agencyFilter,
            @QueryParam("extended") Boolean extended, @QueryParam("routerId") String routerId) {
        RouteList response = new RouteList();
        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
        for (AgencyAndId routeId : routeIds) {
            for (RouteVariant variant : transitIndexService.getVariantsForRoute(routeId)) {
                Route route = variant.getRoute();
                if (agencyFilter != null && !agencyFilter.equals(route.getAgency().getId()))
                    continue;
                RouteType routeType = new RouteType(route, extended);                
                response.routes.add(routeType);
                break;
            }
        }
        return response;
    }
    /**
     * Return pop within x meters of a point,
     * but this can be changed with the radius parameter (in meters)
     */
    @GET
    @Path("/PopNearPoint")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getPopNearPoint(@QueryParam("agency") String agency,
            @QueryParam("lat") Double lat, @QueryParam("lon") Double lon,
            @QueryParam("extended") Boolean extended, @QueryParam("routerId") String routerId,
	     @QueryParam("radius") Double radius)
            throws JSONException {

       // default search radius.
       Double searchRadius = (radius == null) ? STOP_SEARCH_RADIUS : radius;

       Graph graph = getGraph("census");
       StopList response = new StopList();
       if (Double.isNaN(searchRadius) || searchRadius <= 0) {
           searchRadius = STOP_SEARCH_RADIUS;
       }

        StreetVertexIndexService streetVertexIndexService = graph.streetIndex;
        List<TransitStop> stops = streetVertexIndexService.getNearbyTransitStops(new Coordinate(
                lon, lat), searchRadius);
        TransitIndexService transitIndexService = graph.getService(TransitIndexService.class);
        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }
        
        Integer pop = 0;
        for (TransitStop transitStop : stops) {
            
        	pop += Integer.parseInt(transitStop.getStopId().getId());  
        	StopType stop = new StopType(transitStop.getStop(), extended);
        	response.stops.add(stop);
        }

        return response;
    }
    /**
     * Return stops near a point.  The default search radius is 200m,
     * but this can be changed with the radius parameter (in meters)
     */
    @GET
    @Path("/stopsNearPoint")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getStopsNearPoint(@QueryParam("agency") String agency,
            @QueryParam("lat") Double lat, @QueryParam("lon") Double lon,
            @QueryParam("extended") Boolean extended, @QueryParam("routerId") String routerId,
	     @QueryParam("radius") Double radius)
            throws JSONException {

       // default search radius.
       Double searchRadius = (radius == null) ? STOP_SEARCH_RADIUS : radius;

       Graph graph = getGraph(routerId);

       if (Double.isNaN(searchRadius) || searchRadius <= 0) {
           searchRadius = STOP_SEARCH_RADIUS;
       }

        StreetVertexIndexService streetVertexIndexService = graph.streetIndex;
        List<TransitStop> stops = streetVertexIndexService.getNearbyTransitStops(new Coordinate(
                lon, lat), searchRadius);
        TransitIndexService transitIndexService = graph.getService(TransitIndexService.class);
        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }

        StopList response = new StopList();
        for (TransitStop transitStop : stops) {
            AgencyAndId stopId = transitStop.getStopId();
            if (agency != null && !agency.equals(stopId.getAgencyId()))
                continue;
            StopType stop = new StopType(transitStop.getStop(), extended);
            stop.routes = transitIndexService.getRoutesForStop(stopId);
            response.stops.add(stop);
        }

        return response;
    }

    /**
     * Return routes that a stop is served by
     */
    @GET
    @Path("/routesForStop")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getRoutesForStop(@QueryParam("agency") String agency,
            @QueryParam("id") String id, @QueryParam("extended") Boolean extended,
            @QueryParam("routerId") String routerId) throws JSONException {

        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }

        RouteList result = new RouteList();

        for (String string : getAgenciesIds(agency, routerId)) {
            List<AgencyAndId> routes = transitIndexService.getRoutesForStop(new AgencyAndId(string,
                    id));
            result.routes.addAll(makeRouteList(routes, null, extended, routerId).routes);
        }

        return result;
    }

    /**
     * Return stop times for a stop, in seconds since the epoch startTime and endTime are in milliseconds since epoch
     */
    @GET
    @Path("/stopTimesForStop")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getStopTimesForStop(@QueryParam("agency") String stopAgency,
            @QueryParam("id") String stopId, @QueryParam("startTime") long startTime,
            @QueryParam("endTime") Long endTime, @QueryParam("extended") Boolean extended,
            @QueryParam("references") Boolean references, @QueryParam("routeId") String routeId,
            @QueryParam("routerId") String routerId) throws JSONException {

        startTime /= 1000;

        if (endTime == null) {
            endTime = startTime + 86400;
        } else {
            endTime /= 1000;
        }

        if (endTime - startTime > MAX_STOP_TIME_QUERY_INTERVAL) {
            return new TransitError("Max stop time query interval is " + (endTime - startTime));
        }
        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }

        // if no stopAgency is set try to search through all diffrent agencies
        Graph graph = getGraph(routerId);

        // add all departures
        HashSet<TripType> trips = new HashSet<TripType>();
        StopTimeList result = new StopTimeList();
        result.stopTimes = new ArrayList<StopTime>();

        if (references != null && references.equals(true)) {
            result.routes = new HashSet<Route>();
        }

        for (String stopAgencyId : getAgenciesIds(stopAgency, routerId)) {

            AgencyAndId stop = new AgencyAndId(stopAgencyId, stopId);
            Edge preBoardEdge = transitIndexService.getPreBoardEdge(stop);
            if (preBoardEdge == null)
                continue;
            Vertex boarding = preBoardEdge.getToVertex();

            RoutingRequest options = makeTraverseOptions(startTime, routerId);

            HashMap<Long, Edge> seen = new HashMap();
            OUTER: for (Edge e : boarding.getOutgoing()) {
                // each of these edges boards a separate set of trips
                for (StopTime st : getStopTimesForBoardEdge(startTime, endTime, options, e,
                        extended)) {
                    // diffrent parameters
                    st.phase = "departure";
                    if (extended != null && extended.equals(true)) {
                        if (routeId != null && !routeId.equals("")
                                && !st.trip.getRoute().getId().getId().equals(routeId))
                            continue;
                        if (references != null && references.equals(true))
                            result.routes.add(st.trip.getRoute());
                        result.stopTimes.add(st);
                    } else
                        result.stopTimes.add(st);
                    trips.add(st.trip);
                    if (seen.containsKey(st.time)) {
                        Edge old = seen.get(st.time);
                        System.out.println("DUP: " + old);
                        getStopTimesForBoardEdge(startTime, endTime, options, e,
                                extended);
                        //break OUTER;
                    }
                    seen.put(st.time, e);
                }
            }
/*
            // add the arriving stop times for cases where there are no departures
            Edge preAlightEdge = transitIndexService.getPreAlightEdge(stop);
            Vertex alighting = preAlightEdge.getFromVertex();
            for (Edge e : alighting.getIncoming()) {
                for (StopTime st : getStopTimesForAlightEdge(startTime, endTime, options, e,
                        extended)) {
                    if (!trips.contains(st.trip)) {
                        // diffrent parameters
                        st.phase = "arrival";
                        if (extended != null && extended.equals(true)) {
                            if (routeId != null && !routeId.equals("")
                                    && !st.trip.getRoute().getId().getId().equals(routeId))
                                continue;
                            if (references != null && references.equals(true))
                                result.routes.add(st.trip.getRoute());
                            result.stopTimes.add(st);
                        } else
                            result.stopTimes.add(st);
                    }
                }
            }
            */
        }
        Collections.sort(result.stopTimes, new Comparator<StopTime>(){

            @Override
            public int compare(StopTime o1, StopTime o2) {
                if (o1.phase.equals("arrival") && o2.phase.equals("departure")) return 1;
                if (o1.phase.equals("departure") && o2.phase.equals("arrival")) return -1;
                return o1.time - o2.time > 0 ? 1 : -1;
            }

        });

        return result;
    }

    private RoutingRequest makeTraverseOptions(long startTime, String routerId) {
        RoutingRequest options = new RoutingRequest();
        // if (graphService.getCalendarService() != null) {
        // options.setCalendarService(graphService.getCalendarService());
        // options.setServiceDays(startTime, agencies);
        // }
        // TODO: verify correctness
        options.dateTime = startTime;
        Graph graph = getGraph(routerId);
        Collection<Vertex> vertices = graph.getVertices();
        Iterator<Vertex> it = vertices.iterator();
        options.setFrom(it.next().getLabel());
        options.setTo(it.next().getLabel());
        options.setRoutingContext(graph);
        return options;
    }
    /**
     * Return variant for a trip
     */
    @GET
    @Path("/variantForTrip")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getVariantForTrip(@QueryParam("tripAgency") String tripAgency,
            @QueryParam("tripId") String tripId, @QueryParam("routerId") String routerId)
            throws JSONException {

        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);

        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }

        AgencyAndId trip = new AgencyAndId(tripAgency, tripId);
        RouteVariant variant = transitIndexService.getVariantForTrip(trip);

        return variant;
    }

    /**
    * Return information about calendar for given agency
    */
        @GET
        @Path("/calendar")
        @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
        public Object getCalendar(@QueryParam("agency") String agency,
                @QueryParam("routerId") String routerId) throws JSONException {

            TransitIndexService transitIndexService = getGraph(routerId).getService(
                    TransitIndexService.class);

            if (transitIndexService == null) {
                return new TransitError(
                        "No transit index found. Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
            }

            CalendarData response = new CalendarData();
            response.calendarList = new ArrayList<ServiceCalendarType>();
            response.calendarDatesList = new ArrayList<ServiceCalendarDateType>();

            for (String agencyId : getAgenciesIds(agency, routerId)) {
                List<ServiceCalendar> scList = transitIndexService.getCalendarsByAgency(agencyId);
                List<ServiceCalendarDate> scdList = transitIndexService
                        .getCalendarDatesByAgency(agencyId);

                if (scList != null)
                    for (ServiceCalendar sc : scList)
                        response.calendarList.add(new ServiceCalendarType(sc));
                if (scdList != null)
                    for (ServiceCalendarDate scd : scdList)
                        response.calendarDatesList.add(new ServiceCalendarDateType(scd));
            }

            return response;
        }
        
    /**
     * Return subsequent stop times for a trip; time is in milliseconds since epoch
     */
    @GET
    @Path("/stopTimesForTrip")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getStopTimesForTrip(@QueryParam("stopAgency") String stopAgency,
            @QueryParam("stopId") String stopId, @QueryParam("tripAgency") String tripAgency,
            @QueryParam("tripId") String tripId, @QueryParam("time") long time,
            @QueryParam("extended") Boolean extended, @QueryParam("routerId") String routerId)
            throws JSONException {

        time /= 1000;

        AgencyAndId firstStop = null;
        if (stopId != null) {
            firstStop = new AgencyAndId(stopAgency, stopId);
        }
        AgencyAndId trip = new AgencyAndId(tripAgency, tripId);

        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);

        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }

        RouteVariant variant = transitIndexService.getVariantForTrip(trip);
        RoutingRequest options = makeTraverseOptions(time, routerId);

        StopTimeList result = new StopTimeList();
        result.stopTimes = new ArrayList<StopTime>();
        State state = null;
        RouteSegment start = null;
        for (RouteSegment segment : variant.getSegments()) {
            // this is all segments across all patterns that match this variant
            if (segment.stop.equals(firstStop)) {
                // this might be the correct start segment, but we need to try traversing and see if we get this trip
                // TODO: verify options and state creation correctness (AMB)
                State s0 = new State(segment.board.getFromVertex(), options);
                state = segment.board.traverse(s0);
                if (state == null)
                    continue;
                if (state.getBackTrip().getId().equals(trip)) {
                    start = segment;
                    StopTime st = new StopTime();
                    st.time = state.getTime();
                    for (org.onebusaway.gtfs.model.Stop stop : variant.getStops())
                        if (stop.getId().equals(segment.stop)) {
                            st.stop = new StopType(stop, extended);
                        }
                    result.stopTimes.add(st);
                    break;
                }
            }
        }
        if (start == null) {
            return null;
        }

        for (RouteSegment segment : variant.segmentsAfter(start)) {
            // TODO: verify options/state init correctness
            State s0 = new State(segment.hopIn.getFromVertex(), state.getTime(), options);
            state = segment.hopIn.traverse(s0);
            StopTime st = new StopTime();
            st.time = state.getTime();
            for (org.onebusaway.gtfs.model.Stop stop : variant.getStops())
                if (stop.getId().equals(segment.stop))
                    if (stop.getId().equals(segment.stop)) {
                        if (extended != null && extended.equals(true)) {
                            st.stop = new StopType(stop, extended);
                        }
                    }
            result.stopTimes.add(st);
        }

        return result;
    }

    private List<StopTime> getStopTimesForBoardEdge(long startTime, long endTime,
            RoutingRequest options, Edge e, Boolean extended) {
        List<StopTime> out = new ArrayList<StopTime>();
        State result;
        long time = startTime;
        do {
            // TODO verify options/state correctness
            State s0 = new State(e.getFromVertex(), time, options);
            result = e.traverse(s0);
            if (result == null)
                break;
            time = result.getTime();
            if (time > endTime)
                break;
            StopTime stopTime = new StopTime();
            stopTime.time = time;
            stopTime.trip = new TripType(result.getBackTrip(), extended);
            out.add(stopTime);

            time += 1; // move to the next board time
        } while (true);
        return out;
    }

    private List<StopTime> getStopTimesForAlightEdge(long startTime, long endTime,
            RoutingRequest options, Edge e, Boolean extended) {
        List<StopTime> out = new ArrayList<StopTime>();
        State result;
        long time = endTime;
        options = options.reversedClone();
        do {
            // TODO: verify options/state correctness
            State s0 = new State(e.getToVertex(), time, options);
            result = e.traverse(s0);
            if (result == null)
                break;
            time = result.getTime();
            if (time < startTime)
                break;
            StopTime stopTime = new StopTime();
            stopTime.time = time;
            stopTime.trip = new TripType(result.getBackTrip(), extended);
            out.add(stopTime);
            time -= 1; // move to the previous alight time
        } while (true);
        return out;
    }

    /**
     * Return a list of all available transit modes supported, if any.
     * 
     * @throws JSONException
     */
    @GET
    @Path("/modes")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getModes(@QueryParam("routerId") String routerId) throws JSONException {
        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }

        ModeList modes = new ModeList();
        modes.modes = new ArrayList<TraverseMode>();
        for (TraverseMode mode : transitIndexService.getAllModes()) {
            modes.modes.add(mode);
        }
        return modes;
    }

    private Graph getGraph(String routerId) {
        return graphService.getGraph(routerId);
    }

    public Object getCalendarServiceDataForAgency(@QueryParam("agency") String agency,
            @QueryParam("routerId") String routerId) {
        TransitIndexService transitIndexService = getGraph(routerId).getService(
                TransitIndexService.class);
        if (transitIndexService == null) {
            return new TransitError(
                    "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
        }

        ServiceCalendarData data = new ServiceCalendarData();

        data.calendars = transitIndexService.getCalendarsByAgency(agency);
        data.calendarDates = transitIndexService.getCalendarDatesByAgency(agency);

        return data;
    }

    /**
     * Return a list of all stops that are inside a rectangle given by lat lon positions.
     */
    @GET
    @Path("/stopsInRectangle")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object stopsInRectangle(@QueryParam("agency") String agency,
            @QueryParam("leftUpLat") Double leftUpLat, @QueryParam("leftUpLon") Double leftUpLon,
            @QueryParam("rightUpLat") Double rightUpLat,
            @QueryParam("rightUpLon") Double rightUpLon, @QueryParam("extended") Boolean extended,
            @QueryParam("routerId") String routerId) throws JSONException {

        Graph graph = getGraph(routerId);
        StopList response = new StopList();

        StreetVertexIndexService streetVertexIndexService = graph.streetIndex;
        if (leftUpLat == null || leftUpLon == null || rightUpLat == null || rightUpLon == null) {
            double METERS_PER_DEGREE_LAT = 111111;
            double distance = 2000;
            for (Vertex gv : graph.getVertices()) {
                if (gv instanceof TransitStop) {
                    Coordinate c = gv.getCoordinate();
                    Envelope env = new Envelope(c);
                    double meters_per_degree_lon_here = METERS_PER_DEGREE_LAT
                            * Math.cos(Math.toRadians(c.y));
                    env.expandBy(distance / meters_per_degree_lon_here, distance
                            / METERS_PER_DEGREE_LAT);
                    StopType stop = new StopType(((TransitStop) gv).getStop(), extended);
                    response.stops.add(stop);
                }
            }
        } else {
            Coordinate cOne = new Coordinate(leftUpLat, leftUpLon);
            Coordinate cTwo = new Coordinate(rightUpLat, rightUpLon);
            List<TransitStop> stops = streetVertexIndexService.getNearbyTransitStops(cOne, cTwo);
            TransitIndexService transitIndexService = graph.getService(TransitIndexService.class);
            if (transitIndexService == null) {
                return new TransitError(
                        "No transit index found.  Add TransitIndexBuilder to your graph builder configuration and rebuild your graph.");
            }

            for (TransitStop transitStop : stops) {
                AgencyAndId stopId = transitStop.getStopId();
                if (agency != null && !agency.equals(stopId.getAgencyId()))
                    continue;
                StopType stop = new StopType(transitStop.getStop(), extended);
                if (extended != null && extended.equals(true))
                    stop.routes = transitIndexService.getRoutesForStop(stopId);
                response.stops.add(stop);
            }
        }

        return response;
    }

    /**
     * Return a list of all routes that operate between start stop and end stop.
     */
    @GET
    @Path("/routesBetweenStops")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object routesBetweenStops(@QueryParam("startAgency") String startAgency,
            @QueryParam("endAgency") String endAgency,
            @QueryParam("startStopId") String startStopId,
            @QueryParam("endStopId") String endStopId, @QueryParam("extended") Boolean extended,
            @QueryParam("routerId") String routerId) throws JSONException {

        RouteList response = new RouteList();

        RouteList routeList = (RouteList) this.getRoutesForStop(startAgency, startStopId, extended,
                routerId);

        for (RouteType route : routeList.routes) {
            for (String agency : getAgenciesIds(null, routerId)) {
                if (ifRouteBetweenStops(route, agency, routerId, startStopId, endStopId, endAgency))
                    response.routes.add(route);
            }
        }

        return response;
    }

    private double ddistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        if (dist>1) dist =1;
        if (dist<-1) dist = -1;
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;      
        return (dist);
      }

      /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
      /*::  This function converts decimal degrees to radians             :*/
      /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
      private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
      }

      /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
      /*::  This function converts radians to decimal degrees             :*/
      /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
      private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
      }     
    
    private Boolean ifRouteBetweenStops(RouteType route, String agency, String routerId,
            String startStopId, String endStopId, String endAgency) throws JSONException {

        RouteDataList routeDataList = (RouteDataList) this.getRouteData(agency, route.getId()
                .getId(), false, false, routerId);
        for (RouteData routeData : routeDataList.routeData)
            for (RouteVariant variant : routeData.variants)
                for (String endStopAgency : getAgenciesIds(endAgency, routerId)) {
                    Boolean start = false;
                    for (Stop stop : variant.getStops()) {
                        if (stop.getId().getId().equals(startStopId))
                            start = true;
                        if (start && stop.getId().equals(new AgencyAndId(endStopAgency, endStopId))) {
                            return true;
                        }
                    }
                }
        return false;
    }

    private ArrayList<String> getAgenciesIds(String agency, String routerId) {

        Graph graph = getGraph(routerId);

        ArrayList<String> agencyList = new ArrayList<String>();
        if (agency == null || agency.equals("")) {
            for (String a : graph.getAgencyIds()) {
                agencyList.add(a);
            }
        } else {
            agencyList.add(agency);
        }
        return agencyList;
    }
}

