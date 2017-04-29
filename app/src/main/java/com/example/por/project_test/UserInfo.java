package com.example.por.project_test;

/**
 * Created by Por on 10/1/2016.
 */

class UserInfo {

    String username, publickey, groupname;
    int userid, groupid, count;


    UserInfo(int userid, String username, String publickey, int groupid, String groupname, int count) {
        this.userid = userid;
        this.username = username;
        this.publickey = publickey;
        this.groupname = groupname;
        this.groupid = groupid;
        this.count = count;
    }

    String getName() {
        if (groupname != null) {
            return groupname;
        }
        return username;
    }


}
