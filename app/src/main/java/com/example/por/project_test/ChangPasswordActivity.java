package com.example.por.project_test;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

public class ChangPasswordActivity extends AppCompatActivity implements HttpRequestCallback {
    EditText password, chang_password, confim_newpassword;
    Button bt_submit;
    String privateKey, token, id, encryptPK;
    AESEncryption aesEncryption;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chang_password);
        password = (EditText) findViewById(R.id.chang_password);
        chang_password = (EditText) findViewById(R.id.new_password);
        confim_newpassword = (EditText) findViewById(R.id.new_cofirm_password);
        bt_submit = (Button) findViewById(R.id.submit_changpassword);


        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        id = sp.getString("user_id_current", "-1");
        token = sp.getString("token", "-1");
        privateKey = sp.getString("privatekey", "-1");

        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String str_password = password.getText().toString().trim(), str_chang_password = chang_password.getText().toString().trim(), str_confirm = confim_newpassword.getText().toString().trim();

                if (str_password.isEmpty() || str_chang_password.length() == 0 || str_password.equals("")) {
                    password.setError("Password must be filled");
                } else if (str_chang_password.isEmpty() || str_chang_password.length() == 0 || str_chang_password.equals("")) {
                    chang_password.setError("New Password must be filled");
                } else if (str_confirm.isEmpty() || str_confirm.length() == 0 || str_confirm.equals("")) {
                    confim_newpassword.setError("New Password must be filled");
                } else if (str_password.length() < 7) {
                    chang_password.setError("Password must be more than 8");
                } else if (confim_newpassword.length() < 7) {
                    chang_password.setError("Password must be more than 8");
                    confim_newpassword.setError("Password must be more than 8");
                } else {

                    new BackgoundWorker(ChangPasswordActivity.this).execute("changpassword", id, token, str_password);
                }
            }
        });


    }

    @Override
    public void onResult(String[] result, ArrayList<Object> objectses) {

        if (result != null) {
            if (result[0].equals(BackgoundWorker.TRUE)) {
                byte[] key = new byte[0];
                try {
                    key = confim_newpassword.getText().toString().getBytes("UTF-8");
                    key = Arrays.copyOf(key, 32);
                    String str_key = new String(key, "UTF-8");
                    AESEncryption aesEncryption = new AESEncryption(str_key);
                    encryptPK = aesEncryption.encrypt(privateKey);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                new BackgoundWorker(ChangPasswordActivity.this).execute("changpassword", id, token, password.getText().toString(), confim_newpassword.getText().toString(), encryptPK);
                finish();
            } else {
                return;
            }
        }

    }
}
