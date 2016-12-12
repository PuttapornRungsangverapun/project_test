package com.example.por.project_test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by por on 11/27/2016.
 */

public class ImageCacheUtils {

    public static void save(Context context, String id, Bitmap image){
        File target = new File(context.getCacheDir(),"image_cache_" + id);
        try {
            FileOutputStream fos = new FileOutputStream(target);
            image.compress(Bitmap.CompressFormat.PNG,100,fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap load(Context context, String id){
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

        return bm;
    }

    public static boolean hasCache(Context context, String id){
        File target = new File(context.getCacheDir(),"image_cache_" + id);
        return target.exists();
    }
}
