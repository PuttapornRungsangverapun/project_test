package com.example.por.project_test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.net.wifi.WifiConfiguration.Status.strings;

/**
 * Created by User on 31/3/2560.
 */

class LoadImageTask extends AsyncTask<String, Integer, Integer> {

    private Bitmap bitmap;
    private ImageView iv;
    private String msgId;
    private Context ctx;

    LoadImageTask(Context ctx, String msgId, ImageView iv) {
        this.ctx = ctx;
        this.msgId = msgId;
        this.iv = iv;
    }


    @Override
    protected Integer doInBackground(String... params) {
        try {
            URL url = new URL(params[0]);
            String type = params[1];

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);

            int size = Integer.parseInt(connection.getHeaderField("Content-length"));//ขนาดข้อมูลเท่าไหร่ หน่วยbyte
            InputStream is = connection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            byte[] bytes = new byte[512];//โหลดทีละ512
            byte[] bytesdecrypt = new byte[size];
            int read, count = 0;

            float update = 0f;
            while ((read = bis.read(bytes)) != -1) {
                //  fos.write(bytes, 0, read);
                System.arraycopy(bytes, 0, bytesdecrypt, count, read);//sourceกอปจากไหน,เริ่มต้นของsource,destinationเริ่มต้น,กอปทั้งหทดกี่ตัว
                count += read;//countอ่านมาแล้วกี่ไบ
            }
            if (type.equals("single")) {
                byte[] original = ((MessageActivity) ctx).aesEncryption.decrypt(bytesdecrypt);
                bitmap = BitmapFactory.decodeByteArray(original, 0, original.length);
            }
            else if(type.equals("group")){
                byte[] original = ((GroupMessageActivity) ctx).aesEncryption.decrypt(bytesdecrypt);
                bitmap = BitmapFactory.decodeByteArray(original, 0, original.length);
            }
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        iv.setImageBitmap(bitmap);
        ImageCacheUtils.save(ctx, msgId, bitmap);
    }
}
