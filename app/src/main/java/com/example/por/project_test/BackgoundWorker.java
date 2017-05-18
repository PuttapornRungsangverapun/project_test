package com.example.por.project_test;

import android.os.AsyncTask;
import android.util.Log;

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
    private HttpRequestCallback callback;
    private String status, type;
    public static final String url_server = "https://vps145.vpshispeed.net/";

    public BackgoundWorker(HttpRequestCallback callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params) {

        //String url_server = "http://192.168.137.2/";

        this.type = params[0];
        switch (type) {
            case "register": {
                HashMap<String, String> param = new HashMap<>();
                param.put("username", params[1]);
                param.put("password", params[2]);
                param.put("email", params[3]);
                httpRequest(url_server + "register.php", param);
                break;
            }
            case "login": {
                HashMap<String, String> param = new HashMap<>();
                param.put("username", params[1]);
                param.put("password", params[2]);
                httpRequest(url_server + "login.php", param);
                break;
            }
            case "searchfriend": {
                HashMap<String, String> param = new HashMap<>();
                param.put("userid", params[1]);
                param.put("username", params[2]);
                param.put("token", params[3]);
                httpRequest(url_server + "search_friend.php", param);
                break;
            }
            case "addfriend": {
                HashMap<String, String> param = new HashMap<>();
                param.put("username", params[1]);
                param.put("userid", params[2]);
                param.put("token", params[3]);
                httpRequest(url_server + "add_friend.php", param);
                break;
            }
            case "listfriend": {
                HashMap<String, String> param = new HashMap<>();
                param.put("userid", params[1]);
                param.put("token", params[2]);
                httpRequest(url_server + "list_friend.php", param);
                break;
            }
            case "sendmessage": {
                HashMap<String, String> param = new HashMap<>();
                param.put("userid", params[1]);
                param.put("friendid", params[2]);
                param.put("message", params[3]);
                param.put("type", params[4]);
                param.put("filename", params[5]);
                param.put("latitude", params[6]);
                param.put("longitude", params[7]);
                param.put("token", params[8]);
                if (params[4].equals("file")) {
                    param.put("md5", params[9]);
                } else if (params.length >= 10) {
                    param.put("targetid", params[9]);
                }
                httpRequest(url_server + "send_message.php", param);
                break;
            }
            case "readmessage": {
                HashMap<String, String> param = new HashMap<>();
                param.put("userid", params[1]);
                param.put("friendid", params[2]);
                param.put("lastmessageid", params[3]);
                param.put("token", params[4]);
                httpRequest(url_server + "read_message.php", param);
                break;
            }
            case "notification": {
                HashMap<String, String> param = new HashMap<>();
                param.put("userid", params[1]);
                param.put("token_noti", params[2]);
                param.put("token", params[3]);
                httpRequest(url_server + "token_notification.php", param);
                break;
            }
            case "listaddgroup": {
                HashMap<String, String> param = new HashMap<>();
                param.put("userid", params[1]);
                param.put("token", params[2]);
                httpRequest(url_server + "list_groupcreate.php", param);
                break;
            }
            case "crategroup": {
                HashMap<String, String> param = new HashMap<>();
                param.put("userid", params[1]);
                param.put("token", params[2]);
                param.put("frienid", params[3]);
                param.put("groupname", params[4]);
                httpRequest(url_server + "create_group.php", param);
                break;
            }
            case "membergroup": {
                HashMap<String, String> param = new HashMap<>();
                param.put("userid", params[1]);
                param.put("token", params[2]);
                param.put("groupid", params[3]);
                httpRequest(url_server + "member_group.php", param);
                break;
            }
            case "listinvitefriend": {
                HashMap<String, String> param = new HashMap<>();
                param.put("userid", params[1]);
                param.put("token", params[2]);
                param.put("groupid", params[3]);
                httpRequest(url_server + "list_invitegroup.php", param);
                break;
            }
            case "invitefriend": {
                HashMap<String, String> param = new HashMap<>();
                param.put("userid", params[1]);
                param.put("token", params[2]);
                param.put("friendid", params[3]);
                param.put("groupid", params[4]);
                httpRequest(url_server + "invite_group.php", param);
                break;
            }
            case "readmessagegroup": {
                HashMap<String, String> param = new HashMap<>();
                param.put("userid", params[1]);
                param.put("groupid", params[2]);
                param.put("lastmessageid", params[3]);
                param.put("token", params[4]);
                httpRequest(url_server + "read_messagegroup.php", param);
                break;
            }
            case "sendmessagegroup": {
                HashMap<String, String> param = new HashMap<>();
                param.put("userid", params[1]);
                param.put("groupid", params[2]);
                param.put("message", params[3]);
                param.put("type", params[4]);
                param.put("filename", params[5]);
                param.put("latitude", params[6]);
                param.put("longitude", params[7]);
                param.put("token", params[8]);
                if (params[4].equals("file")) {
                    param.put("md5", params[9]);
                } else if (params.length >= 10) {
                    param.put("targetid", params[9]);
                }
                httpRequest(url_server + "send_messagegroup.php", param);
                break;
            }
            case "getpublickey": {
                HashMap<String, String> param = new HashMap<>();
                param.put("userid", params[1]);
                param.put("friendid", params[2]);
                param.put("token", params[3]);
                httpRequest(url_server + "get_publickey.php", param);
                break;
            }
            case "storekey": {
                HashMap<String, String> param = new HashMap<>();
                param.put("encryptpk", params[1]);
                param.put("publickey", params[2]);
                param.put("token", params[3]);
                param.put("userid", params[4]);
                httpRequest(url_server + "store_key.php", param);
                break;
            }
            case "leavegroup": {
                HashMap<String, String> param = new HashMap<>();
                param.put("userid", params[1]);
                param.put("token", params[2]);
                param.put("groupid", params[3]);
                httpRequest(url_server + "leave_group.php", param);
                break;
            }
            case "requestfriend": {
                HashMap<String, String> param = new HashMap<>();
                param.put("userid", params[1]);
                param.put("token", params[2]);
                httpRequest(url_server + "request_friend.php", param);
                break;
            }
            case "logout": {
                HashMap<String, String> param = new HashMap<>();
                param.put("userid", params[1]);
                param.put("token", params[2]);
                httpRequest(url_server + "logout.php", param);
                break;
            }
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
        } catch (Exception e) {
            Log.e("JSON error", "Cann't convert to json object");
            e.printStackTrace();
            return;
        }
        switch (type) {
            case "login":
                try {
                    if (resource.getString("status").equals("success")) {
                        callback.onResult(new String[]{TRUE, resource.getString("message"),
                                resource.getString("userid"), resource.getString("token"),
                                resource.getString("username"),
                                resource.getString("publickey"),
                                resource.getString("privatekey")}, null);
                    } else {
                        callback.onResult(new String[]{FALSE, resource.getString("message"), null}, null);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "register":
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
                break;
            case "searchfriend":

                try {
                    if (resource.getString("status").equals("success")) {
                        callback.onResult(new String[]{TRUE, resource.getString("message"), FALSE}, null);
                    } else {
                        callback.onResult(new String[]{FALSE, resource.getString("message"), FALSE}, null);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "addfriend":

                try {
                    if (resource.getString("status").equals("success")) {
                        callback.onResult(new String[]{resource.getString("message"), "addfriend"}, null);
                    } else {
                        callback.onResult(new String[]{FALSE, resource.getString("message")}, null);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "listfriend": {
                ArrayList<Object> temp = new ArrayList<>();
                try {
                    JSONArray jsonfriendlist = resource.getJSONArray("message");
                    for (int i = 0; i < jsonfriendlist.length(); i++) {//ทำparsingแปลงjsonarray
                        UserInfo userInfo = new UserInfo(jsonfriendlist.getJSONObject(i).getInt("user_id"),
                                jsonfriendlist.getJSONObject(i).getString("user_username"),
                                jsonfriendlist.getJSONObject(i).getString("publickey"),
                                jsonfriendlist.getJSONObject(i).getInt("group_id"),
                                jsonfriendlist.getJSONObject(i).getString("group_name"),
                                jsonfriendlist.getJSONObject(i).getInt("n"));
                        temp.add(userInfo);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.onResult(null, temp);
                break;
            }
            case "sendmessage":

                try {
                    if (resource.getString("status").equals("success")) {
                        callback.onResult(new String[]{resource.getString("message"), TRUE}, null);
                    } else {
                        callback.onResult(new String[]{resource.getString("message"), FALSE}, null);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "readmessage":

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
                                jsonmessage.getJSONObject(i).getString("authen_body"),
                                jsonmessage.getJSONObject(i).getString("time"),
                                jsonmessage.getJSONObject(i).getString("message_type"));

                        temp.add(messageInfo);
                    }
                    callback.onResult(null, temp);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "listaddgroup": {
                ArrayList<Object> temp = new ArrayList<>();
                try {
                    JSONArray jsonfriendlist = resource.getJSONArray("message");
                    for (int i = 0; i < jsonfriendlist.length(); i++) {//ทำparsingแปลงjsonarray
                        AddUserGroupInfo addUserGroupInfo = new AddUserGroupInfo(
                                jsonfriendlist.getJSONObject(i).getInt("friend_id"),
                                jsonfriendlist.getJSONObject(i).getString("user_username"));

                        temp.add(addUserGroupInfo);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.onResult(null, temp);
                break;
            }
            case "crategroup":

                try {
                    if (resource.getString("status").equals("success")) {
                        if (resource.has("message")) {
                            callback.onResult(new String[]{TRUE, resource.getString("message"), resource.getString("groupid")}, null);
                        }
                    } else {
                        callback.onResult(new String[]{FALSE, resource.getString("message")}, null);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            case "membergroup": {
                ArrayList<Object> temp = new ArrayList<>();
                try {
                    JSONArray jsonfriendlist = resource.getJSONArray("message");
                    for (int i = 0; i < jsonfriendlist.length(); i++) {//ทำparsingแปลงjsonarray
                        MemberGroupInfo memberGroupInfo = new MemberGroupInfo(
                                jsonfriendlist.getJSONObject(i).getString("user_username"));
                        temp.add(memberGroupInfo);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.onResult(null, temp);
                break;
            }
            case "listinvitefriend": {
                ArrayList<Object> temp = new ArrayList<>();
                try {
                    JSONArray jsonfriendlist = resource.getJSONArray("message");
                    for (int i = 0; i < jsonfriendlist.length(); i++) {//ทำparsingแปลงjsonarray
                        InviteGroupInfo inviteGroupInfo = new InviteGroupInfo(
                                jsonfriendlist.getJSONObject(i).getInt("fid"),
                                jsonfriendlist.getJSONObject(i).getString("user_username"));
                        temp.add(inviteGroupInfo);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.onResult(null, temp);
                break;
            }
            case "invitefriend":

                try {
                    if (resource.getString("status").equals("success")) {
                        callback.onResult(new String[]{TRUE, resource.getString("message")}, null);
                    } else {
                        callback.onResult(new String[]{FALSE, resource.getString("message")}, null);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "readmessagegroup":

                try {
                    ArrayList<Object> temp = new ArrayList<>();
                    JSONArray jsonmessage = resource.getJSONArray("message");
                    for (int i = 0; i < jsonmessage.length(); i++) {//ทำparsingแปลงjsonarray
                        GroupMessageInfo groupMessageInfo = new GroupMessageInfo(jsonmessage.getJSONObject(i).getInt("group_message_id"),
                                jsonmessage.getJSONObject(i).getString("text_body"),
                                jsonmessage.getJSONObject(i).getInt("group_message_status"),
                                jsonmessage.getJSONObject(i).getInt("group_message_sender_id"),
                                jsonmessage.getJSONObject(i).getString("user_username"),
                                jsonmessage.getJSONObject(i).getString("file_filename"),
                                jsonmessage.getJSONObject(i).getString("map_latitude"),
                                jsonmessage.getJSONObject(i).getString("map_longitude"),
                                jsonmessage.getJSONObject(i).getString("authen_body"),
                                jsonmessage.getJSONObject(i).getString("time"),
                                jsonmessage.getJSONObject(i).getString("group_message_type"),
                                jsonmessage.getJSONObject(i).getString("target_userid"));

                        temp.add(groupMessageInfo);
                    }
                    callback.onResult(null, temp);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "sendmessagegroup":

                try {
                    if (resource.getString("status").equals("success")) {
                        callback.onResult(new String[]{resource.getString("message"), TRUE}, null);
                    } else {
                        callback.onResult(new String[]{resource.getString("message"), FALSE}, null);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "getpublickey":

                try {
                    if (resource.getString("status").equals("success")) {
                        callback.onResult(new String[]{type, resource.getJSONObject("message").getString("user_id"),
                                resource.getJSONObject("message").getString("publickey"), TRUE}, null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "requestfriend":

                try {
                    ArrayList<Object> temp = new ArrayList<>();
                    JSONArray jsonmessage = resource.getJSONArray("message");
                    for (int i = 0; i < jsonmessage.length(); i++) {//ทำparsingแปลงjsonarray
                        RequestFriendInfo requestFriendInfo = new RequestFriendInfo(
                                jsonmessage.getJSONObject(i).getString("user_id"),
                                jsonmessage.getJSONObject(i).getString("user_username"));
                        temp.add(requestFriendInfo);
                    }
                    callback.onResult(null, temp);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }

        //super.onPostExecute(result);

    }
}
