package com.example.por.project_test;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Por on 10/9/2016.
 */

public class GroupMessageAdapter extends ArrayAdapter<GroupMessageInfo> {
    private final Context ctx;
    List<GroupMessageInfo> value;
    String user_id_current;
    ImageView img_file;
    String token, id;

    public GroupMessageAdapter(Context ctx, int resource, int textViewResourceId, ArrayList<GroupMessageInfo> value, String token, String id) {
        super(ctx, resource, textViewResourceId, value);
        this.ctx = ctx;
        this.value = value;
        SharedPreferences sp = ctx.getSharedPreferences("MySetting", ctx.MODE_PRIVATE);
        user_id_current = sp.getString("user_id_current", "-1");
        this.token = token;
        this.id = id;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView textView, tv_time_sender, tv_time_receiver;
        Drawable img;

        String filename;
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.groupmessage, parent, false);
        LinearLayout linearLayout = (LinearLayout) rowView.findViewById(R.id.ln_group_reciever);
        LinearLayout linearLayout2 = (LinearLayout) rowView.findViewById(R.id.ln_group_sender);

        tv_time_sender = (TextView) rowView.findViewById(R.id.tv_group_sender_time);
        tv_time_sender.setText(value.get(position).time);
        tv_time_receiver = (TextView) rowView.findViewById(R.id.tv_group_reciver_time);
        tv_time_receiver.setText(value.get(position).time);

        if (user_id_current.equals(value.get(position).message_sender_id + "")) {
            linearLayout2.setVisibility(View.VISIBLE);
            textView = (TextView) rowView.findViewById(R.id.tv_group_message_adapter);
            img_file = (ImageView) rowView.findViewById(R.id.img_group_upload);

        } else {
            linearLayout.setVisibility(View.VISIBLE);
            textView = (TextView) rowView.findViewById(R.id.tv_groupmessage_left);

            TextView tv_user_from = (TextView) rowView.findViewById(R.id.tv_group_from);

            tv_user_from.setText(value.get(position).username);
            img_file = (ImageView) rowView.findViewById(R.id.img_group_download);
        }
        if (value.get(position).message_status == 4) {
            TextView tv_read = (TextView) rowView.findViewById(R.id.tv_read);
            tv_read.setVisibility(View.VISIBLE);
        }

        if (value.get(position).type.equals("file")) {
            filename = value.get(position).filename;


            if (filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                String url = BackgoundWorker.url_server + "downloadfilegroup.php?messageid=" + value.get(position).group_message_id + "&token=" + token + "&userid=" + id;
                img_file.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);

                String msgId = value.get(position).group_message_id + "";

                if (ImageCacheUtils.hasCache(ctx, msgId)) {
                    Bitmap bm = ImageCacheUtils.load(ctx, msgId);
                    if (bm != null) {
                        img_file.setImageBitmap(bm);
                    }
                } else {
                    new ImgTask(msgId, img_file).execute(url);
                }
            } else {
                textView.setText(value.get(position).filename);
                img = rowView.getResources().getDrawable(R.drawable.file);
                img.setBounds(0, 0, 70, 70);
                textView.setCompoundDrawables(img, null, null, null);
            }
        } else if (value.get(position).type.equals("text")) {
            textView.setText(value.get(position).message);
        } else if (value.get(position).type.equals("map")) {
            textView.setText("Location");
            img = rowView.getResources().getDrawable(R.drawable.makermap);
            img.setBounds(0, 0, 70, 70);
            textView.setCompoundDrawables(img, null, null, null);

        }


        return rowView;
    }

    class ImgTask extends AsyncTask<String, Integer, Integer> {

        Bitmap bitmap;
        ImageView iv;
        String msgId;

        ImgTask(String msgId, ImageView iv) {
            this.msgId = msgId;
            this.iv = iv;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);

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

                byte[] original = ((GroupMessageActivity) ctx).aesEncryption.decrypt(bytesdecrypt);
                bitmap = BitmapFactory.decodeByteArray(original, 0, original.length);
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


}
