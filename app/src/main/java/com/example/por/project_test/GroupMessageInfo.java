package com.example.por.project_test;

/**
 * Created by Por on 6/1/2560.
 */

public class GroupMessageInfo {
    String message, filename, type, tmpLat, tmpLon, time;
    int group_message_id, message_sender_id, message_status, group_id;
    double latitude, longtitude;

    GroupMessageInfo(int group_message_id, String message, int message_status, int message_sender_id, int group_id, String filename, String latitude, String longtitude, String time, String type) {
        this.group_message_id = group_message_id;
        this.message = message;
        this.message_status = message_status;
        this.message_sender_id = message_sender_id;
        this.group_id = group_id;
        this.filename = filename;
        this.time = time;
        this.type = type;

        if ((!latitude.equals("null") && !longtitude.equals("null")) && (!latitude.isEmpty() && !longtitude.isEmpty())) {
            this.tmpLat = latitude;
            this.tmpLon = longtitude;

        }
    }
}
