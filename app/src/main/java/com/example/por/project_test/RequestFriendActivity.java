package com.example.por.project_test;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.example.por.project_test.R.id.lv_contact;

public class RequestFriendActivity extends AppCompatActivity implements HttpRequestCallback {
    ListView lv_requestFriend;
    private ArrayList<RequestFriendInfo> requestFriendInfos;
    private RequestFriendAdapter requestFriendAdapter;
    String id, token;
    ImageView request_add_friend;
    TextView tv_request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_friend);
        lv_requestFriend = (ListView) findViewById(R.id.lv_requestFriend);
        request_add_friend = (ImageView) findViewById(R.id.request_add_friend);
        tv_request = (TextView) findViewById(R.id.tv_request);

        requestFriendInfos = new ArrayList<>();


        requestFriendAdapter = new RequestFriendAdapter(this, R.layout.request_friend, requestFriendInfos);
        lv_requestFriend.setAdapter(requestFriendAdapter);


        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        id = sp.getString("user_id_current", "-1");
        token = sp.getString("token", "-1");
        setTitle("Request friend");


        new BackgoundWorker(this).execute("requestfriend", id, token);

    }

    @Override
    public void onResult(String[] result, ArrayList<Object> objectses) {
        requestFriendInfos = new ArrayList<>();
        if (objectses==null) {
            tv_request.setVisibility(View.VISIBLE);
            lv_requestFriend.setVisibility(View.GONE);
            return;
        }
        for (Object o : objectses) {
            if (o instanceof RequestFriendInfo)//เข็คoใช่objectของclassหรือไม่
                requestFriendInfos.add((RequestFriendInfo) o);
        }

        requestFriendAdapter = new RequestFriendAdapter(this, R.layout.request_friend, requestFriendInfos);
        lv_requestFriend.setAdapter(requestFriendAdapter);
    }
}
