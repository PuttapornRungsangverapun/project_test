package com.example.por.project_test;

/**
 * Created by Por on 6/1/2560.
 */

public class GroupMessageInfo {
    String message, filename, type, tmpLat, tmpLon, time, username;
    int group_message_id, message_sender_id, message_status, target_id;
    double latitude, longtitude;

    GroupMessageInfo(int group_message_id, String message, int message_status, int message_sender_id, String username, String filename, String latitude, String longtitude, String time, String type, int target_id) {
        this.group_message_id = group_message_id;
        this.message = message;
        this.message_status = message_status;
        this.message_sender_id = message_sender_id;
        this.username = username;
        this.filename = filename;
        this.time = time;
        this.type = type;
        this.target_id = target_id;
        if ((!latitude.equals("null") && !longtitude.equals("null")) && (!latitude.isEmpty() && !longtitude.isEmpty())) {
            this.tmpLat = latitude;
            this.tmpLon = longtitude;

        }
    }
}
