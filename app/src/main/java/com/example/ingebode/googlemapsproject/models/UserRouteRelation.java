package com.example.ingebode.googlemapsproject.models;

/**
 * Created by ingebode on 27/04/16.
 */
public class UserRouteRelation {

    String user_id;
    String route_id;
    String username;
    String point_collection_id;
    String time;
    int times_infront;
    int times_behind;
    int times_beside;

    public UserRouteRelation(String route_id, String user_id, String username, String point_collection_id, String time, int times_behind, int times_infront, int times_beside){
        this.user_id = user_id;
        this.route_id = route_id;
        this.username = username;
        this.point_collection_id = point_collection_id;
        this.time = time;
        this.times_behind = times_behind;
        this.times_infront = times_infront;
        this.times_beside = times_beside;
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
    public String getTime(){
        return time;
    }

    public int getTimes_behind() {
        return times_behind;
    }

    public int getTimes_infront() {
        return times_infront;
    }
    public int getTimes_beside(){
        return times_beside;
    }
}
