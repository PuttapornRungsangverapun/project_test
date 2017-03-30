package com.example.por.project_test;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * Created by User on 29/3/2560.
 */

public class RSAEncryption {
    private static String  privateKey;

    RSAEncryption(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences("MySetting", Context.MODE_PRIVATE);
        privateKey = sp.getString("privatekey", "-1");

    }

    public String RSAEncrypt(String publicKey,String myMessage) {


        RSAPublicKey pbKey = null;
        byte[] keyBytes = null;
        try {
            keyBytes = Base64.decode(publicKey.getBytes("utf-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            pbKey = (RSAPublicKey) keyFactory.generatePublic(spec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

///

        // Get an instance of the Cipher for RSA encryption/decryption
        Cipher c = null;
        try {
            c = Cipher.getInstance("RSA");
            // Initiate the Cipher, telling it that it is going to Encrypt, giving it the public key
            c.init(Cipher.ENCRYPT_MODE, pbKey);

            // Encrypt that message using a new SealedObject and the Cipher we created before
            String msg = Base64.encodeToString(c.doFinal(myMessage.getBytes("UTF-8")), Base64.DEFAULT);

            return msg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String RSADecrypt(String myMessage) {
        RSAPrivateKey pvKey = null;

        byte[] keyBytes = null;
        try {
            keyBytes = Base64.decode(privateKey.getBytes("utf-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            pvKey = (RSAPrivateKey) keyFactory.generatePrivate(spec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

///

        // Get an instance of the Cipher for RSA encryption/decryption
        Cipher c = null;
        try {
            c = Cipher.getInstance("RSA");
            // Initiate the Cipher, telling it that it is going to Encrypt, giving it the public key
            c.init(Cipher.DECRYPT_MODE, pvKey);

            // Encrypt that message using a new SealedObject and the Cipher we created before
            String msg = new String(c.doFinal(Base64.decode(myMessage, Base64.DEFAULT)));

            return msg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
