package com.example.por.project_test;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Por on 9/13/2016.
 */
public class BackgoundWorker extends AsyncTask<String, String, String> {
    public static final String TRUE = "true", FALSE = "false";
    HttpRequestCallback callback;
    String status, type;
    public static final String url_server = "http://122.155.16.121/";

    public BackgoundWorker(HttpRequestCallback callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params) {
        String type = params[0];
        //String url_server = "http://192.168.137.2/";

        this.type = type;
        if (type.equals("register")) {
            HashMap<String, String> param = new HashMap<>();
            param.put("username", params[1]);
            param.put("password", params[2]);
            param.put("email", params[3]);
            param.put("publickey", params[4]);
            httpRequest(url_server + "register.php", param);
        } else if (type.equals("login")) {
            HashMap<String, String> param = new HashMap<>();
            param.put("username", params[1]);
            param.put("password", params[2]);
            httpRequest(url_server + "login.php", param);
        } else if (type.equals("searchfriend")) {
            HashMap<String, String> param = new HashMap<>();
            param.put("userid", params[1]);
            param.put("username", params[2]);
            param.put("token", params[3]);
            httpRequest(url_server + "searchfriend.php", param);
        } else if (type.equals("addfriend")) {
            HashMap<String, String> param = new HashMap<>();
            param.put("username", params[1]);
            param.put("userid", params[2]);
            param.put("token", params[3]);
            httpRequest(url_server + "addfriend.php", param);
        } else if (type.equals("listfriend")) {
            HashMap<String, String> param = new HashMap<>();
            param.put("userid", params[1]);
            param.put("token", params[2]);
            httpRequest(url_server + "listfriend.php", param);
        } else if (type.equals("sendmessage")) {
            HashMap<String, String> param = new HashMap<>();
            param.put("userid", params[1]);
            param.put("friendid", params[2]);
            param.put("message", params[3]);
            param.put("type", params[4]);
            param.put("filename", params[5]);
            param.put("latitude", params[6]);
            param.put("longitude", params[7]);
            param.put("token", params[8]);
            httpRequest(url_server + "message.php", param);
        } else if (type.equals("readmessage")) {
            HashMap<String, String> param = new HashMap<>();
            param.put("userid", params[1]);
            param.put("friendid", params[2]);
            param.put("lastmessageid", params[3]);
            param.put("token", params[4]);
            httpRequest(url_server + "messagestatus.php", param);
        }else if(type.equals("notification")){
            HashMap<String, String> param = new HashMap<>();
            param.put("userid", params[1]);
            param.put("token_noti", params[2]);
            param.put("token", params[3]);
            httpRequest(url_server + "tokennotification.php", param);
        }
        return status;
    }

    private void httpRequest(String urlLink, HashMap<String, String> param) {
        String post_data = "";
        try {
            URL url = new URL(urlLink);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);//ส่งข้อมูลให้serverไหม
            OutputStream outputStream = httpURLConnection.getOutputStream();//สร้างท่อ
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            for (String key : param.keySet()) {
                post_data += URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(param.get(key), "UTF-8") + "&";
            }
            bufferedWriter.write(post_data);//เขียนใส่ท่อ
            bufferedWriter.flush();//flushไม่ต้องรอส่งเลย
            bufferedWriter.close();//ปิดท่อ
            outputStream.close();

            httpURLConnection.connect();
            int resPonseStatus = httpURLConnection.getResponseCode();

            InputStream inputStream = httpURLConnection.getInputStream();//ท่อเราดูดมา

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            status = br.readLine();//ดูดข้อมูลมา1บรรทัด

            inputStream.close();
            httpURLConnection.disconnect();

//            if (resPonseStatus == 403) {
//                status = "403";
//            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onPostExecute(String result) {
//        if (result.equals("403")) {
//            return;
//        }

        JSONObject resource = null;
        try {
            resource = new JSONObject(result);
        } catch (JSONException e) {
            Log.e("JSON error", "Cann't convert to json object");
            e.printStackTrace();
            return;
        }

        if (type.equals("login")) {
            try {

                if (resource.getString("status").equals("success")) {
                    callback.onResult(new String[]{TRUE, resource.getString("message"),
                            resource.getString("userid"), resource.getString("token"),
                            resource.getString("username")}, null);
                } else {
                    callback.onResult(new String[]{FALSE, resource.getString("message"), null}, null);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (type.equals("register")) {
            try {

                if (resource.getString("status").equals("success")) {
                    callback.onResult(new String[]{TRUE, resource.getString("message"),
                            resource.getString("userid"),
                            resource.getString("token"),
                            resource.getString("username")}, null);
                } else {
                    callback.onResult(new String[]{FALSE, resource.getString("message"), null}, null);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (type.equals("searchfriend")) {

            try {
                if (resource.getString("status").equals("success")) {
                    callback.onResult(new String[]{TRUE, resource.getString("message"), FALSE}, null);
                } else {
                    callback.onResult(new String[]{FALSE, resource.getString("message"), FALSE}, null);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (type.equals("addfriend")) {

            try {
                if (resource.getString("status").equals("success")) {
                    callback.onResult(new String[]{resource.getString("message"), "addfriend"}, null);
                } else {
                    callback.onResult(new String[]{FALSE, resource.getString("message")}, null);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (type.equals("listfriend")) {
            ArrayList<Object> temp = new ArrayList<>();
            try {
                JSONArray jsonfriendlist = resource.getJSONArray("message");
                for (int i = 0; i < jsonfriendlist.length(); i++) {//ทำparsingแปลงjsonarray
                    UserInfo userInfo = new UserInfo(jsonfriendlist.getJSONObject(i).getInt("user_id"),
                            jsonfriendlist.getJSONObject(i).getString("user_username"),
                            jsonfriendlist.getJSONObject(i).getString("publickey"));
                    temp.add(userInfo);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            callback.onResult(null, temp);
        } else if (type.equals("sendmessage")) {

            try {
                if (resource.getString("status").equals("success")) {
                    callback.onResult(new String[]{resource.getString("message"),TRUE}, null);
                } else {
                    callback.onResult(new String[]{resource.getString("message"),FALSE}, null);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (type.equals("readmessage")) {

            try {
                ArrayList<Object> temp = new ArrayList<>();
                JSONArray jsonmessage = resource.getJSONArray("message");
                for (int i = 0; i < jsonmessage.length(); i++) {//ทำparsingแปลงjsonarray
                    MessageInfo messageInfo = new MessageInfo(jsonmessage.getJSONObject(i).getInt("message_id"),
                            jsonmessage.getJSONObject(i).getString("text_body"),
                            jsonmessage.getJSONObject(i).getInt("message_status"),
                            jsonmessage.getJSONObject(i).getInt("message_sender_id"),
                            jsonmessage.getJSONObject(i).getString("file_filename"),
                            jsonmessage.getJSONObject(i).getString("map_latitude"),
                            jsonmessage.getJSONObject(i).getString("map_longitude"),
                            jsonmessage.getJSONObject(i).getString("time"),
                            jsonmessage.getJSONObject(i).getString("message_type"));

                    temp.add(messageInfo);
                }
                callback.onResult(null, temp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        //super.onPostExecute(result);

    }
}
