package com.example.por.project_test;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class AddfriendActivity extends AppCompatActivity implements TextWatcher, HttpRequestCallback {
    EditText et_searchfriend;
    TextView tv_searchfriend;
    Button bt_add;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriend);
        tv_searchfriend = (TextView) findViewById(R.id.tv_searchfriend);
        et_searchfriend = (EditText) findViewById(R.id.et_searchfriend);
        bt_add = (Button) findViewById(R.id.bt_addfriend);
        setTitle("Add friend");
        et_searchfriend.addTextChangedListener(this);


        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String type = "addfriend";
                String str_username = et_searchfriend.getText().toString().trim();

//                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AddfriendActivity.this);
                SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
                String id = sp.getString("user_id_current", "");
                String token = sp.getString("token", "");

                if ((str_username.isEmpty() || str_username.length() == 0 || str_username.equals("") || str_username == null)) {
                    et_searchfriend.setError("Not found");

                } else {
                    BackgoundWorker backgoundWorker = new BackgoundWorker(AddfriendActivity.this);
                    backgoundWorker.execute(type, str_username, id + "", token);
//                    Log.i("Addfriend", "Click");
                }
            }
        });

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String type = "searchfriend";

        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        String id = sp.getString("user_id_current", "");
        String token = sp.getString("token", "");

        BackgoundWorker backgoundWorker = new BackgoundWorker(this);
        backgoundWorker.execute(type, id, editable.toString(), token);
        //Log.i("Searchfriend", id,editable.toString(),token);


    }


    @Override
    public void onResult(String[] result, ArrayList<Object> userList) {
        if (result[0].equals(BackgoundWorker.FALSE)) {
            tv_searchfriend.setText(result[1]);
        } else {
            tv_searchfriend.setText(result[1]);
            if (result[1].equals("addfriend")) {
                tv_searchfriend.setText(result[0]);
                finish();
            }
        }
    }
}

