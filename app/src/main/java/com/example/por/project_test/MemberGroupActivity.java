package com.example.por.project_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class MemberGroupActivity extends AppCompatActivity implements HttpRequestCallback {
    ArrayList<MemberGroupInfo> memberGroupInfos;
    MemberGroupAdapter memberGroupAdapter;
    ListView lv_membergroup;
    String  id, token, groupId, groupName;
    Button bt_invite_friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_group);
        lv_membergroup = (ListView) findViewById(R.id.lv_membergroup);
        bt_invite_friend = (Button) findViewById(R.id.bt_invite_friend);
        Intent i = getIntent();
        groupId = i.getStringExtra("groupid");
        groupName = i.getStringExtra("groupname");
        setTitle("Member : " + groupName);

        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        id = sp.getString("user_id_current", "-1");
        token = sp.getString("token", "-1");

        BackgoundWorker backgoundWorker = new BackgoundWorker(MemberGroupActivity.this);
        backgoundWorker.execute("membergroup", id, token, groupId);

        memberGroupInfos = new ArrayList<>();
        memberGroupAdapter = new MemberGroupAdapter(this, R.layout.membergroup, memberGroupInfos);
        lv_membergroup.setAdapter(memberGroupAdapter);

        bt_invite_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MemberGroupActivity.this, InviteGroupActivity.class);
                i.putExtra("groupid", groupId);
                i.putExtra("groupname", groupName);
                startActivity(i);
            }
        });
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
