package com.example.por.project_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements HttpRequestCallback {

    Button bt_login;
    EditText et_login_username, et_login_password;
    TextView tv_register;
    CheckBox chk_keepme;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        if (!sp.getString("user_id_current", "-1").equals("-1") && sp.getBoolean("re_login", true) == false) {
            Intent i = new Intent(MainActivity.this, ContactActivity.class);
            startActivity(i);
        }

        bt_login = (Button) findViewById(R.id.bt_login);
        et_login_username = (EditText) findViewById(R.id.et_login_username);
        et_login_password = (EditText) findViewById(R.id.et_login_password);
        tv_register = (TextView) findViewById(R.id.tv_register);
        chk_keepme = (CheckBox) findViewById(R.id.chk_keepme);

        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String type = "login";
                String str_username = et_login_username.getText().toString().trim();
                String str_password = et_login_password.getText().toString().trim();

                if ((str_username.isEmpty() || str_username.length() == 0 || str_username.equals("") || str_username == null)) {
                    et_login_username.setError("Username must be filled");
                } else if (str_password.isEmpty() || str_password.length() == 0 || str_password.equals("") || str_password == null) {
                    et_login_password.setError("Password must be filled");
                } else {
                    BackgoundWorker backgoundWorker = new BackgoundWorker(MainActivity.this);
                    backgoundWorker.execute(type, str_username, str_password);
                }

            }
        });

        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });
    }


    @Override
    public void onResult(String[] result, ArrayList<Object> userList) {
        Toast.makeText(MainActivity.this, result[1], Toast.LENGTH_SHORT).show();
        if (result[0].equals(BackgoundWorker.TRUE)) {
            Intent i = new Intent(MainActivity.this, ContactActivity.class);
            startActivity(i);

//            new RegisterActivity().getRSAKey();

            if (chk_keepme.isChecked()) {
                //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                //SharedPreferences.Editor editor = sp.edit();

                SharedPreferences.Editor editor = getSharedPreferences("MySetting", MODE_PRIVATE).edit();
                editor.putString("user_id_current", result[2]);
                editor.putBoolean("re_login", false);

                editor.putString("token", result[3]);
                editor.putString("username", result[4]);
                editor.commit();
            } else {
                //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                //SharedPreferences.Editor editor = sp.edit();

                SharedPreferences.Editor editor = getSharedPreferences("MySetting", MODE_PRIVATE).edit();
                editor.putString("user_id_current", result[2]);
                editor.putBoolean("re_login", true);

                editor.putString("token", result[3]);
                editor.putString("username", result[4]);
                editor.commit();

            }
        }
    }


}