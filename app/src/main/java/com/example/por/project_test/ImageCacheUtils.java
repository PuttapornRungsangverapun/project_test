package com.example.por.project_test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

/**
 * Created by por on 11/27/2016.
 */

class ImageCacheUtils {

    private static HashMap<String,Bitmap> hash = new HashMap<>();

    static void save(Context context, String id, Bitmap image){
        File target = new File(context.getCacheDir(),"image_cache_" + id);
        try {
            FileOutputStream fos = new FileOutputStream(target);
            image.compress(Bitmap.CompressFormat.JPEG,90,fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Bitmap load(Context context, String id){
        if(hash.containsKey(id)){
            return hash.get(id);
        }

        File target = new File(context.getCacheDir(),"image_cache_" + id);
        if(!target.exists()){
            return null;
        }

        Bitmap bm = null;
        try {
            FileInputStream fis = new FileInputStream(target);
            bm = BitmapFactory.decodeStream(fis);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        hash.put(id,bm);

        return bm;
    }

    static boolean hasCache(Context context, String id){
        File target = new File(context.getCacheDir(),"image_cache_" + id);
        return target.exists();
    }
}
