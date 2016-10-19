package com.example.ingebode.googlemapsproject.models;

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
    private String time;
    String description;
    private String point_collection_id, username;
    double distance;

    public Route(){

    }

    public Route(String route_id, String point_collection_id, double start_latitude, double start_longitude, double finish_latitude, double finish_longitude, String route_name, String time, String username, double distance){
        this.route_id=route_id;
        this.start_latitude=start_latitude;
        this.start_longitude=start_longitude;
        this.finish_latitude=finish_latitude;
        this.finish_longitude=finish_longitude;
        this.route_name = route_name;
        this.point_collection_id = point_collection_id;
        this.time = time;
        this.username = username;
        this.distance = distance;

    }

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

    public double getDistance() {
        return distance;
    }

    public String getRoute_name(){
        return route_name;
    }
    public String getDescription(){
        return description;
    }
    public String getPoint_collection_id() { return point_collection_id; }
    public String getTime(){
        return time;
    }

    public static class Point {
        private String user_id;
        private String route_id;
        private double latitude;
        private double longitude;
        private int repetition;
        private int point_number;

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

        public Point(String user_id, String route_id, double latitude, double longitude, int repetition, int point_number){
            this.user_id=user_id;
            this.route_id=route_id;
            this.latitude=latitude;
            this.longitude=longitude;
            this.repetition=repetition;
            this.point_number = point_number;
        }
        public Point(){

        }

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
        public int getPoint_number(){
            return point_number;
        }
        public void setPoint_number(int point_number){
            this.point_number = point_number;
        }
    }
}


