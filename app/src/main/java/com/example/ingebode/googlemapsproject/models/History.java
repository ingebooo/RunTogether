package com.example.ingebode.googlemapsproject.models;

public class History {
    private String user_id;
    private String route_id;
    private String distance;
    private String avg_speed;
    String point_collection_id;
    String time;

    public History(String user_id,String route_id, String distance, String avg_speed, String time){
        this.user_id=user_id;
        this.route_id=route_id;
        this.distance=distance;
        this.avg_speed=avg_speed;
        this.time = time;
    }


    public String getRoute_id(){
        return route_id;
    }

    public String getUser_id(){
        return user_id;
    }

    public String getDistance(){
        return distance;
    }

    public String getAvg_speed(){
        return avg_speed;
    }
    public String getTime(){
        return time;
    }

}
