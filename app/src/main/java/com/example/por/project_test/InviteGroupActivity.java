package com.example.por.project_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.por.project_test.R.id.lv_addgroup;

public class InviteGroupActivity extends AppCompatActivity implements HttpRequestCallback {
    ArrayList<InviteGroupInfo> inviteGroupInfos;
    InviteGroupAdapter inviteGroupAdapter;
    ListView lv_invitegroup;
    static String id, token;
    String  type, groupId, groupName;
    Button bt_invite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_group);

        lv_invitegroup = (ListView) findViewById(R.id.lv_invitegroup);
        bt_invite = (Button) findViewById(R.id.bt_invite);

        inviteGroupInfos = new ArrayList<>();
        inviteGroupAdapter = new InviteGroupAdapter(this, R.layout.invitegroup, inviteGroupInfos);
        lv_invitegroup.setAdapter(inviteGroupAdapter);

        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        id = sp.getString("user_id_current", "-1");
        token = sp.getString("token", "-1");

        Intent i = getIntent();
        groupId = i.getStringExtra("groupid");
        setTitle("Group : " + i.getStringExtra("groupname"));

        type = "listinvitefriend";
        BackgoundWorker backgoundWorker = new BackgoundWorker(InviteGroupActivity.this);
        backgoundWorker.execute(type, id, token, groupId);

        bt_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BackgoundWorker backgoundWorker = new BackgoundWorker(InviteGroupActivity.this);
                for (int i = 0; i < inviteGroupAdapter.value.size(); i++) {
                    if (inviteGroupAdapter.mCheckStates.get(i) == true) {
                        type = "invitefriend";
                        String friendid = inviteGroupInfos.get(i).userid + "";
                        backgoundWorker.execute(type, id, token, friendid, groupId);
                    }
                }

            }
        });
    }

    @Override
    public void onResult(String[] result, ArrayList<Object> userList) {
        if ((result != null) && (result[0].equals(BackgoundWorker.TRUE))) {
            Toast.makeText(this, result[1], Toast.LENGTH_SHORT).show();
            finish();
        }
        if ((userList == null) && (result == null)) {
            return;
        } else if (userList == null) {
            return;
        } else if ((result == null) && userList != null) {
            inviteGroupInfos = new ArrayList<>();
            for (Object o : userList) {
                if (o instanceof InviteGroupInfo)//เข็คoใช่objectของclassหรือไม่
                    inviteGroupInfos.add((InviteGroupInfo) o);

            }
            inviteGroupAdapter = new InviteGroupAdapter(this, R.layout.invitegroup, inviteGroupInfos);
            lv_invitegroup.setAdapter(inviteGroupAdapter);
        }
    }
}
