package com.example.por.project_test;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class AddfriendActivity extends AppCompatActivity implements HttpRequestCallback {

    TextView tv_searchfriend;
    Button bt_add;
    String id, token;
    SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriend);

        tv_searchfriend = (TextView) findViewById(R.id.tv_searchfriend);

        bt_add = (Button) findViewById(R.id.bt_addfriend);
        bt_add.setVisibility(View.GONE);
        searchView = (SearchView) findViewById(R.id.search_friend);
        ImageView searchClose = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        setTitle("Add friend");

        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        id = sp.getString("user_id_current", "");
        token = sp.getString("token", "");


        searchView.setQueryHint("Search friend");
        searchView.onActionViewExpanded();
        InputMethodManager imm = (InputMethodManager) getSystemService(AddfriendActivity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        searchClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setQuery("", false);
                searchView.clearFocus();
                tv_searchfriend.setVisibility(View.GONE);
                bt_add.setVisibility(View.GONE);
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!(query.isEmpty() || query.length() == 0 || query.equals(""))) {
                    new BackgoundWorker(AddfriendActivity.this).execute("searchfriend", id, query, token);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!(newText.isEmpty() || newText.length() == 0 || newText.equals(""))) {
                    new BackgoundWorker(AddfriendActivity.this).execute("searchfriend", id, newText, token);
                }
                return false;
            }
        });


    }


    @Override
    public void onResult(final String[] result, ArrayList<Object> userList) {
        if (result[0].equals(BackgoundWorker.FALSE)) {
            tv_searchfriend.setText(result[1]);
            tv_searchfriend.setVisibility(View.VISIBLE);
            bt_add.setVisibility(View.GONE);
        } else {
            tv_searchfriend.setText(result[1]);
            tv_searchfriend.setVisibility(View.VISIBLE);
            bt_add.setVisibility(View.VISIBLE);
            bt_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new BackgoundWorker(AddfriendActivity.this).execute("addfriend", result[1], id, token);
                }
            });
            if (result[1].equals("addfriend")) {
                tv_searchfriend.setText(result[0]);
                finish();
            }
        }
    }


}

