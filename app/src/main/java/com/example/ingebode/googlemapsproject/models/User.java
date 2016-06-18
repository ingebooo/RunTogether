package com.example.ingebode.googlemapsproject.models;

import java.util.HashMap;

/**
 * Created by ingebode on 28/04/16.
 */
public class User {
    private String username;
    private String user_id;
    //private String email;
    //private HashMap<String, Object> timestampJoined;
    //private boolean hasLoggedInWithPassword;


    /**
     * Required public constructor
     */
    public User() {
    }

    /**
     * Use this constructor to create new User.
     * Takes user name, email and timestampJoined as params
     *
     * @param username
     * @param email
     * @param timestampJoined
     */
    public User(String username, String user_id) {
        this.username = username;
        this.user_id = user_id;

    }

    public String getUsername() {
        return username;
    }
    public String getUser_id() {
        return user_id;
    }

}
