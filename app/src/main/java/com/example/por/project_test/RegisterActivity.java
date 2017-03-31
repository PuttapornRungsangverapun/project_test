package com.example.por.project_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;

public class RegisterActivity extends AppCompatActivity implements HttpRequestCallback {

    EditText et_username, et_password, et_email, et_confirmpassword;
    Button bt_submit;
    TextView tv_result;
    String publicKey, privateKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        et_email = (EditText) findViewById(R.id.et_email);
        bt_submit = (Button) findViewById(R.id.bt_submit);
        tv_result = (TextView) findViewById(R.id.tv_result);
        et_confirmpassword = (EditText) findViewById(R.id.et_confirmpassword);


        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_username = et_username.getText().toString().trim();
                String str_password = et_password.getText().toString().trim();
                String str_email = et_email.getText().toString().trim();
                String str_confirmpassword = et_confirmpassword.getText().toString().trim();
                String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

                if (!str_password.equals(str_confirmpassword)) {
                    et_password.setError("Passwords do not match");
                    et_confirmpassword.setError("Passwords do not match");
                } else if (str_username.isEmpty() || str_username.length() == 0 || str_username.equals("")) {
                    et_username.setError("Username must be filled");
                } else if (str_password.isEmpty() || str_password.length() == 0 || str_password.equals("")) {
                    et_password.setError("Password must be filled");
                } else if (str_password.length() < 7) {
                    et_password.setError("Password must be more than 8");
                } else if (!str_email.matches(EMAIL_PATTERN)) {
                    et_email.setError("Invalid email address");
                } else {
                    new BackgoundWorker(RegisterActivity.this).execute("register", str_username, str_password, str_email);
                }
            }
        });
    }

    @Override
    public void onResult(String[] result, ArrayList<Object> userList) {
        Toast.makeText(RegisterActivity.this, result[1], Toast.LENGTH_SHORT).show();
        if (result[0].equals(BackgoundWorker.TRUE)) {
            // auto login
            Toast.makeText(this, result[1], Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor editor = getSharedPreferences("MySetting", MODE_PRIVATE).edit();
            editor.putString("user_id_current", result[2]);
            editor.putString("token", result[3]);
            editor.putString("username", result[4]);
            editor.putBoolean("re_login", false);
            editor.apply();
            getRSAKey(result[2], result[3]);
            Intent i = new Intent(RegisterActivity.this, ContactActivity.class);
            startActivity(i);
        }
    }

    public void getRSAKey(String user_id, String token) {
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Generate the keys — might take sometime on slow computers
        KeyPair myPair = kpg.generateKeyPair();

        RSAPublicKey pbKey = (RSAPublicKey) myPair.getPublic();
        RSAPrivateKey pvKey = (RSAPrivateKey) myPair.getPrivate();

        publicKey = Base64.encodeToString(pbKey.getEncoded(), Base64.DEFAULT);
//        Log.d("xxx", publicKey);

        privateKey = Base64.encodeToString(pvKey.getEncoded(), Base64.DEFAULT);
//        Log.d("xxx", privateKey);

        SharedPreferences.Editor editor = getSharedPreferences("MySetting", MODE_PRIVATE).edit();
        editor.putString("publickey", publicKey);
        editor.putString("privatekey", privateKey);
        editor.apply();

        try {
            byte[] key = et_password.getText().toString().getBytes("UTF-8");
            key = Arrays.copyOf(key, 32);
            String str_key = new String(key, "UTF-8");
            AESEncryption aesEncryption = new AESEncryption(str_key);
            String encryptPK = aesEncryption.encrypt(privateKey);
            new BackgoundWorker(this).execute("storekey", encryptPK, publicKey, token, user_id);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }


}
