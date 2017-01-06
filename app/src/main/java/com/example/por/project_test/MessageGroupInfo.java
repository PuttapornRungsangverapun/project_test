package com.example.por.project_test;

/**
 * Created by Por on 5/1/2560.
 */

public class MessageGroupInfo {
    String message, filename, type, tmpLat, tmpLon, time;
    int message_id, message_sender_id, message_status;
    double latitude, longtitude;

    MessageGroupInfo(int message_id, String message, int message_status, int message_sender_id, String filename, String latitude, String longtitude, String time, String type) {
        this.message_id = message_id;
        this.message = message;
        this.message_status = message_status;
        this.message_sender_id = message_sender_id;
        this.filename = filename;
        this.time = time;
        this.type = type;

        if ((!latitude.equals("null") && !longtitude.equals("null")) && (!latitude.isEmpty() && !longtitude.isEmpty())) {
            this.tmpLat = latitude;
            this.tmpLon = longtitude;

        }
    }
}
