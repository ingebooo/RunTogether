package com.example.ingebode.googlemapsproject.models;

public class History {
    //private int history_id;
    private String user_id;
    private String route_id;
    private String date;
    private double distance;
    private double avg_speed;

    public History(String user_id,String route_id,String date, double distance,double avg_speed){
        this.user_id=user_id;
        this.route_id=route_id;
        this.date=date;
        this.distance=distance;
        this.avg_speed=avg_speed;
    }


    public String getRoute_id(){
        return route_id;
    }

    public String getUser_id(){
        return user_id;
    }

    public String getDate(){
        return date;
    }

    public double getDistance(){
        return distance;
    }

    public double getAvg_speed(){
        return avg_speed;
    }
}
