package com.example.por.project_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class GroupMessageActivity extends AppCompatActivity {
    String groupId,groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message);
        Intent i = getIntent();
        groupId = i.getStringExtra("groupid");

        setTitle(groupName=i.getStringExtra("groupname"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_groupchat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.invite_friend:
                Intent i = new Intent(GroupMessageActivity.this, InviteGroupActivity.class);
                i.putExtra("groupid", groupId);
                i.putExtra("groupname", groupName);
                startActivity(i);
                return true;
            case R.id.member:
                Intent i2 = new Intent(GroupMessageActivity.this, MemberGroupActivity.class);
                i2.putExtra("groupid", groupId);
                i2.putExtra("groupname", groupName);
                startActivity(i2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
