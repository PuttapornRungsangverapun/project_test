package com.example.por.project_test;

/**
 * Created by Por on 10/29/2016.
 */

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadFileService extends Service {
    AESEncryption aesEncryption;
    private NotificationCompat.Builder notification;

    public DownloadFileService() {

    }

    @Override
    public IBinder onBind(Intent intent) {//โชวตัวยพนเพำหห
        return null;//ไม่อนุญาตactivityอื่นมาbind
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {//เใื่อserviceถูกเรียกทำงาน ถูกสร้างแล้วถูกเรียกด้วย

        String url = intent.getStringExtra("url");
        String filename = intent.getStringExtra("filename");
        String type = intent.getStringExtra("type");
        String sharedKey = intent.getStringExtra("sharedkey");
        new DownloadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, filename, type, sharedKey);

        notification = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.arrow_down_float)
                .setContentTitle(filename)
                .setAutoCancel(true);//สั่งให้หยุดให้notiหายไปด้วย
        //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        //.setVibrate(new long[]{ 100, 100, 100, 100, 100, 100, 100, 100, 100})สั่น,หยุด

        startForeground(1919, notification.build());

        return Service.START_STICKY;//ถ้าถูกkillเริ่มใหม่อัตโนมัต
    }

    class DownloadTask extends AsyncTask<String, Integer, Integer> {

        File target;

        @Override
        protected Integer doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                String filename = strings[1];
                String type = strings[2];
                String sharedKey = strings[3];
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);

                int size = Integer.parseInt(connection.getHeaderField("Content-length"));//byte
                target = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
                FileOutputStream fos = new FileOutputStream(target);
                InputStream is = connection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                byte[] bytes = new byte[512];//โหลดทีละ512
                byte[] bytesdecrypt = new byte[size];
                int read, count = 0;
                byte[] original = new byte[0];
                float update = 0f;
                while ((read = bis.read(bytes)) != -1) {
                    //  fos.write(bytes, 0, read);
                    System.arraycopy(bytes, 0, bytesdecrypt, count, read);//sourceกอปจากไหน,เริ่มต้นของsource,destinationเริ่มต้น,กอปทั้งหทดกี่ตัว
                    count += read;//countอ่านมาแล้วกี่ไบ
                    if (count > size * update) {//sizeขนาดทั้งหมด ถ้าดาวน์โลหดมากกว่ากี่เปอเซ็นแล้วให้โชว์ ถ้ามมาก่า5%อ++ัพเดท
                        update += 0.05f;//อัพโลดทีละ5%
                        publishProgress((int) (((float) count * 100) / size));
                    }
                }
                aesEncryption = new AESEncryption(sharedKey);
                if (type.equals("single")) {
                    original = aesEncryption.decrypt(bytesdecrypt);
                } else if (type.equals("group")) {
                    original = aesEncryption.decrypt(bytesdecrypt);
                }
                fos.write(original, 0, original.length);
//                fos.write(bytesdecrypt, 0, bytesdecrypt.length);//no encryptjx
                fos.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress = values[0];
            notification.setProgress(100, progress, false);
            startForeground(1919, notification.build());//idต้องตรงกัน
        }

        @Override
        protected void onPostExecute(Integer integer) {
            Toast.makeText(DownloadFileService.this, "saved at " + target.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            stopSelf();//หำพอรแำนี้คาย
        }
    }
}
