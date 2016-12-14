package com.example.ingebode.googlemapsproject.models;

/**
 * Created by ingebode on 27/04/16.
 */
public class UserRouteRelation {

    String user_id;
    String route_id;
    String username;
    String point_collection_id;


    public UserRouteRelation(String route_id, String user_id, String username, String point_collection_id){
        this.user_id = user_id;
        this.route_id = route_id;
        this.username = username;
        this.point_collection_id = point_collection_id;
    }
    public UserRouteRelation(){

    }
    public String getPoint_collection_id(){
        return point_collection_id;
    }
    public String getRoute_id() {
        return route_id;
    }
    public String getUsername(){
        return username;
    }

    public String getUser_id() {
        return user_id;
    }
}
