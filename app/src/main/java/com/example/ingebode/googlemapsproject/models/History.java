package com.example.ingebode.googlemapsproject.models;

public class History {
    //private int history_id;
    private String user_id;
    private String route_id;
    private String distance;
    private String avg_speed;
    String topSpeed;
    String time;

    public History(String user_id,String route_id, String distance, String avg_speed, String topSpeed, String time){
        this.user_id=user_id;
        this.route_id=route_id;
        this.distance=distance;
        this.avg_speed=avg_speed;
        this.topSpeed = topSpeed;
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
    public String getTopSpeed (){
        return topSpeed;
    }
    public String getTime(){
        return time;
    }
}
