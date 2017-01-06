package com.example.por.project_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class MemberGroupActivity extends AppCompatActivity implements HttpRequestCallback {
    ArrayList<MemberGroupInfo> memberGroupInfos;
    MemberGroupAdapter memberGroupAdapter;
    ListView lv_membergroup;
    String type, id, token, groupId, groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_group);
        lv_membergroup = (ListView) findViewById(R.id.lv_membergroup);

        Intent i = getIntent();
        groupId = i.getStringExtra("groupid");
        groupName = i.getStringExtra("groupname");
        setTitle("Member : " + groupName);

        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        id = sp.getString("user_id_current", "-1");
        token = sp.getString("token", "-1");
        type = "membergroup";
        BackgoundWorker backgoundWorker = new BackgoundWorker(MemberGroupActivity.this);
        backgoundWorker.execute(type, id, token, groupId);


        memberGroupInfos = new ArrayList<>();

        memberGroupAdapter = new MemberGroupAdapter(this, R.layout.membergroup, memberGroupInfos);
        lv_membergroup.setAdapter(memberGroupAdapter);
    }

    @Override
    public void onResult(String[] result, ArrayList<Object> userList) {
        memberGroupInfos = new ArrayList<>();
        for (Object o : userList) {
            if (o instanceof MemberGroupInfo)//เข็คoใช่objectของclassหรือไม่
                memberGroupInfos.add((MemberGroupInfo) o);

        }
        memberGroupAdapter = new MemberGroupAdapter(this, R.layout.membergroup, memberGroupInfos);
        lv_membergroup.setAdapter(memberGroupAdapter);
    }
}
