package com.example.por.project_test;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by User on 31/3/2560.
 */

public class GetMD5 {
    public static String getMD5EncryptedString(String encTarget) {
        MessageDigest mdEnc = null;
        byte[] data = Base64.decode(encTarget, Base64.DEFAULT);
        try {
            mdEnc = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception while encrypting to md5");
            e.printStackTrace();
        } // Encryption algorithm
        if (mdEnc != null) {
            mdEnc.update(data);//encTarget.getBytes()
        }
        byte messageDigest[] = new byte[0];
        if (mdEnc != null) {
            messageDigest = mdEnc.digest();
        }
        StringBuilder hexString = new StringBuilder();
        for (byte aMessageDigest : messageDigest) {
            String h = Integer.toHexString(0xFF & aMessageDigest);
            while (h.length() < 2)
                h = "0" + h;
            hexString.append(h);
        }
        return hexString.toString();
    }
}
