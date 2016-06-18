package com.example.ingebode.googlemapsproject;

public class Point {
    private int point_number;
    private String user_id;
    private String route_id;
    private double latitude;
    private double longitude;
    private int repetition;

    /*public Point(int point_number,String user_id, String route_id, double latitude, double longitude, int repetition){
        this.point_number=point_number;
        this.user_id=user_id;
        this.route_id=route_id;
        this.latitude=latitude;
        this.longitude=longitude;
        this.repetition=repetition;
    }*/

    public Point(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Point(String user_id, String route_id, double latitude, double longitude, int repetition){
        this.user_id=user_id;
        this.route_id=route_id;
        this.latitude=latitude;
        this.longitude=longitude;
        this.repetition=repetition;
    }
    public Point(){

    }



    /*public int getPointNumber(){
        return point_number;
    }*/

    public String getUser_id(){
        return user_id;
    }

    public String getRoute_id(){
        return route_id;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public int getRepetition(){
        return repetition;
    }

}
