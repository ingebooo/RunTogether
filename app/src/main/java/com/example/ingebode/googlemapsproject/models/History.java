package com.example.ingebode.googlemapsproject.models;

public class History {
    //private int history_id;
    private String user_id;
    private String route_id;
    private double distance;
    private double avg_speed;
    double topSpeed;
    String time;

    public History(String user_id,String route_id, double distance,double avg_speed, double topSpeed, String time){
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

    public double getDistance(){
        return distance;
    }

    public double getAvg_speed(){
        return avg_speed;
    }
    public double getTopSpeed (){
        return topSpeed;
    }
    public String getTime(){
        return time;
    }
}
