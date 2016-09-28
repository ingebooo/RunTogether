package com.example.ingebode.googlemapsproject;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by ingeborgoftedal on 15/09/16.
 */
public class CSVFile {
    private Context context;

    // constructor
    public CSVFile(Context context) {
        this.context = context.getApplicationContext();
    }

//        public Point(String user_id, String route_id, double latitude, double longitude, int repetition, int point_number){


    //Write to Points CSV file
    public File writePoints(String filename,String user_id,String route_id,String latitude,String longitude, String count_points){
        File file = new File(android.os.Environment.getExternalStorageDirectory(),filename);
        FileOutputStream outputStream=null;
        try {
            outputStream = new FileOutputStream(file,true);
        } catch (FileNotFoundException e1) {
            System.out.println("File not found");
            e1.printStackTrace();
        }

        try {
            outputStream.write(user_id.getBytes());
            outputStream.write(",".getBytes());
            outputStream.write(route_id.getBytes());
            outputStream.write(",".getBytes());
            outputStream.write(latitude.getBytes());
            outputStream.write(",".getBytes());
            outputStream.write(longitude.getBytes());
            outputStream.write(",".getBytes());
            outputStream.write(count_points.getBytes());
            outputStream.write(";".getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}
