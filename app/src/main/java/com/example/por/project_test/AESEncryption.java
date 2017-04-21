package com.example.por.project_test;

import android.util.Base64;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by User on 29/3/2560.
 */

class AESEncryption {
    private String sharedKey;

    AESEncryption(String sharedKey) {
        this.sharedKey = sharedKey;
    }

    private String encrypt(String msg, byte[] data) {
//        String key = sharedKey; // 256 bit key
        String initVector = new BigInteger(80, new SecureRandom()).toString(32); // 80/5=16 bytes IV   80bitแบบrandom tostringเป็นbase32 ตัวหนังสือ1ตัวเท่ากับ32bit ได้ 16 ตัว
        initVector = "0000000000000000".substring(initVector.length()) + initVector;
        return initVector + encrypt(sharedKey, initVector, msg, data);
    }

    public String encrypt(String msg) {
        return encrypt(msg, null);
    }

    public String encrypt(byte[] data) {
        return encrypt(null, data);
    }


    private String encrypt(String key, String initVector, String value, byte[] data) {
        byte[] encrypted;
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            if (value != null) {
                encrypted = cipher.doFinal(value.getBytes("UTF-8"));
            } else {
                encrypted = cipher.doFinal(data);
            }
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    String decrypt(String encrypted) {
        return new String(decrypt(encrypted, null));
    }

    byte[] decrypt(byte[] data) {
        return decrypt(null, data);
    }

    private byte[] decrypt(String encrypted, byte[] data) {
        String iv = null;
        String cyphertext = null;


        if (encrypted == null) {
            byte[] iv2 = new byte[16];
            byte[] data2 = new byte[data.length - 16];
            System.arraycopy(data, 0, iv2, 0, 16);
            System.arraycopy(data, 16, data2, 0, data2.length);
            return decrypt(sharedKey, new String(iv2), null, data2);

        } else {
            iv = encrypted.substring(0, 16);
            cyphertext = encrypted.substring(16);//begin index 16
            return decrypt(sharedKey, iv, cyphertext, data);
        }
    }


    private byte[] decrypt(String key, String initVector, String encrypted, byte[] data) {
        byte[] original;
        try {
//            Log.e("secret", shareedkey);

            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);


            if (encrypted != null) {
                original = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT));
            } else {
                original = cipher.doFinal(data);
            }
            return original;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }


}
