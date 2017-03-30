package com.example.por.project_test;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class LogoutActivity extends AppCompatActivity implements HttpRequestCallback {
    EditText et_password_logout;
    Button bt_lgout;
    String privateKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        et_password_logout = (EditText) findViewById(R.id.et_password_logout);
        bt_lgout = (Button) findViewById(R.id.bt_logout);

        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        privateKey = sp.getString("privatekey", "-1");

        bt_lgout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_password = et_password_logout.getText().toString().trim();
                if ((str_password.isEmpty() || str_password.length() == 0 || str_password.equals("") || str_password == null)) {
                    et_password_logout.setError("Password must be filled");
                    return;
                } else {
                    try {
                        byte[] key = str_password.getBytes("UTF-8");
//                        MessageDigest sha = null;
//                        sha = MessageDigest.getInstance("SHA-1");
//                        key = sha.digest(key);
                        key = Arrays.copyOf(key, 32);
                        String encryptPrivateKey = encrypt(key, privateKey);

                        String iv = encryptPrivateKey.substring(0, 16);
                        String cyphertext = encryptPrivateKey.substring(16);
                        String de = decrypt(key, iv, cyphertext);


                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }


    @Override
    public void onResult(String[] result, ArrayList<Object> objectses) {

    }

    public String encrypt(byte[] key, String value) {
        String initVector = new BigInteger(80, new SecureRandom()).toString(32);

        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);


            byte[] encrypted = cipher.doFinal(value.getBytes("UTF-8"));

            return initVector+Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private static String decrypt(byte[] key, String initVector, String encrypted) {

        try {

            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);


            byte[] de = cipher.doFinal(Base64.decode(encrypted,Base64.DEFAULT));

            return Base64.encodeToString(de,Base64.DEFAULT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
