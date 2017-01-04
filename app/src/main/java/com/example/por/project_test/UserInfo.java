package com.example.por.project_test;

/**
 * Created by Por on 10/1/2016.
 */

public class UserInfo {

    String username, publickey, groupname;
    int userid, groupid;

    UserInfo(int userid, String username, String publickey,  int groupid,String groupname) {
        this.userid = userid;
        this.username = username;
        this.publickey = publickey;
        this.groupname = groupname;
        this.groupid = groupid;
    }
}
