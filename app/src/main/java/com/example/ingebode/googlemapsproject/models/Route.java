package com.example.ingebode.googlemapsproject.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ingebode on 09/03/16.
 */
public class Route {
    private String route_id;
    private double start_latitude;
    private double start_longitude;
    private double finish_latitude;
    private double finish_longitude;
    private String route_name;
    String description;
    private String point_collection_id, username;
    //private Map<String, Object> runnedByMap;

    public Route(){

    }

    public Route(String route_id, String point_collection_id, double start_latitude, double start_longitude, double finish_latitude, double finish_longitude, String route_name){
        this.route_id=route_id;
        this.start_latitude=start_latitude;
        this.start_longitude=start_longitude;
        this.finish_latitude=finish_latitude;
        this.finish_longitude=finish_longitude;
        this.route_name = route_name;
        this.point_collection_id = point_collection_id;
        //this.username = username;

    }

/*	public Route(double start_latitude,double start_longitude,double finish_latitude,double finish_longitude,String description){
		this.start_latitude=start_latitude;
		this.start_longitude=start_longitude;
		this.finish_latitude=finish_latitude;
		this.finish_longitude=finish_longitude;
		this.description=description;
    public Map<String, Object> getRunnedByMap(){
        return runnedByMap;
    }*/
    public String getUsername(){
        return this.username;
    }
    public void setRoute_id(String route_id){
        this.route_id = route_id;
    }

    public String getRoute_id(){
        return route_id;
    }

    public double getStart_latitude(){
        return start_latitude;
    }

    public double getStart_longitude(){
        return start_longitude;
    }

    public double getFinish_latitude(){
        return finish_latitude;
    }

    public double getFinish_longitude(){
        return finish_longitude;
    }

    public String getRoute_name(){
        return route_name;
    }
    public String getDescription(){
        return description;
    }
    public String getPoint_collection_id() { return point_collection_id; }

}


