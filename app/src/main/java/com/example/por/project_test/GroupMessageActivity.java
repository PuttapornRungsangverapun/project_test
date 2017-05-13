package com.example.por.project_test;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class GroupMessageActivity extends AppCompatActivity implements HttpRequestCallback {

    private static String id, token, shareedkey;
    private String groupId, groupName;
    private boolean isRequesting;
    private ArrayList<GroupMessageInfo> groupMessageInfos;
    private GroupMessageAdapter groupMessageAdapter;
    private int lastMessageId;
    private static int REQUEST_FILE = 1;
    Button bt_group_file, bt_group_send_message;
    private EditText et_group_message;
    ListView listView_group_message;
    private Timer t;
    RSAEncryption rsaEncryption;
    AESEncryption aesEncryption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        bt_group_file = (Button) findViewById(R.id.bt_group_file);
        et_group_message = (EditText) findViewById(R.id.et_group_message);
        bt_group_send_message = (Button) findViewById(R.id.bt_group_send_message);
        listView_group_message = (ListView) findViewById(R.id.listview_group_message);

        Intent i = getIntent();
        groupId = i.getStringExtra("groupid");
        setTitle(groupName = i.getStringExtra("groupname"));

        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        id = sp.getString("user_id_current", "-1");
        token = sp.getString("token", "-1");

        shareedkey = checkhashkey();
        aesEncryption = new AESEncryption(shareedkey);

        groupMessageInfos = new ArrayList<>();
        groupMessageAdapter = new GroupMessageAdapter(this, R.layout.message, R.id.tv_message_adapter, groupMessageInfos, token, id);
        listView_group_message.setAdapter(groupMessageAdapter);

        listView_group_message.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id2) {
                GroupMessageInfo groupMessageInfo = groupMessageInfos.get(position);

                String url = BackgoundWorker.url_server + "download_filegroup.php?messageid=" + groupMessageInfo.group_message_id + "&token=" + token + "&userid=" + id;
                String filename = groupMessageInfo.filename;
                if (groupMessageInfo.type.equals("file")) {
                    Intent intent = new Intent(GroupMessageActivity.this, DownloadFileService.class);
                    intent.putExtra("url", url);
                    intent.putExtra("filename", filename);
                    intent.putExtra("sharedkey", shareedkey);
                    startService(intent);
                } else if (groupMessageInfo.type.equals("map")) {
                    Intent intent = new Intent(GroupMessageActivity.this, MapsActivity.class);
                    intent.putExtra("lat", groupMessageInfo.latitude);
                    intent.putExtra("lon", groupMessageInfo.longtitude);
                    startActivity(intent);
                }
            }
        });

        bt_group_send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_message = et_group_message.getText().toString().trim();
                if (str_message.isEmpty() || str_message.length() == 0 || str_message.equals("")) {
                } else {
                    str_message = aesEncryption.encrypt(str_message);
                    new BackgoundWorker(GroupMessageActivity.this).execute("sendmessagegroup", id, groupId, str_message, "text", "", "", "", token);
                    et_group_message.setText("");
                }
            }
        });

        bt_group_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence colors[] = new CharSequence[]{"FIle", "Share Location"};

                AlertDialog.Builder builder = new AlertDialog.Builder(GroupMessageActivity.this);
                builder.setTitle("Share");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            if (!checkFilePermission()) {
                                return;
                            }
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);//แสดงไฟล์เฉพาะactivityเปิดได้
                            intent.setType("*/*");//(image/jpg)
                            startActivityForResult(intent, REQUEST_FILE);
                        } else if (which == 1) {
                            Intent intent = new Intent(GroupMessageActivity.this, MapsActivity.class);
                            intent.putExtra("groupid", groupId);
                            intent.putExtra("sharedkey", shareedkey);
                            startActivity(intent);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    @Override
    protected void onResume() {
        startFetch();
        super.onResume();
    }

    @Override
    protected void onPause() {
        t.cancel();
        super.onPause();
    }

    private void startFetch() {
        t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isRequesting) {
                    return;
                } else {
                    isRequesting = true;
                    new BackgoundWorker(GroupMessageActivity.this).execute("readmessagegroup", id, groupId, lastMessageId + "", token);
                }
            }
        }, 500, 500);
    }

    @Override
    public void onResult(String[] result, ArrayList<Object> mesObjects) {
        if (mesObjects == null && result == null) {
            isRequesting = false;
            return;
        } else if ((result != null) && (result[1].equals(BackgoundWorker.FALSE))) {

            Toast.makeText(this, result[0], Toast.LENGTH_SHORT).show();
            return;

        } else if (mesObjects == null) {
            return;
        }
        ArrayList<GroupMessageInfo> groupMessageInfos = new ArrayList<>();
        for (Object o : mesObjects) {

            if (o instanceof GroupMessageInfo) {//เข็คoใช่objectของclassหรือไม่
                GroupMessageInfo mo = (GroupMessageInfo) o;
                switch (mo.type) {
                    case "authen":
                        if (shareedkey == null) {
                            rsaEncryption = new RSAEncryption(this);
                            shareedkey = rsaEncryption.RSADecrypt(mo.authen);
                            aesEncryption = new AESEncryption(shareedkey);

                            SharedPreferences.Editor editor = getSharedPreferences("MySetting", MODE_PRIVATE).edit();
                            editor.putString("SHARED_KEY_GROUP:" + groupId, shareedkey);
                            editor.apply();
                        }
                        break;
                    case "map":
                        try {
                            mo.latitude = Double.parseDouble(aesEncryption.decrypt(mo.tmpLat));
                            mo.longtitude = Double.parseDouble(aesEncryption.decrypt(mo.tmpLon));
                            groupMessageInfos.add(mo);
                        } catch (Exception e) {
                            e.printStackTrace();
                            mo.message = "failed to decrypt...";
                            groupMessageInfos.add(mo);
                        }
                        break;
                    case "text":
                        try {
                            mo.message = aesEncryption.decrypt(mo.message);
                            groupMessageInfos.add(mo);
                        } catch (Exception e) {
                            e.printStackTrace();
                            mo.message = "failed to decrypt...";
                            groupMessageInfos.add(mo);
                        }
                        break;
                    default:
                        groupMessageInfos.add(mo);
                        break;
                }
            }
        }
        if (groupMessageInfos.size() > 0) {
            this.groupMessageInfos.addAll(groupMessageInfos);
            lastMessageId = groupMessageInfos.get(groupMessageInfos.size() - 1).group_message_id;//ขนาดของตัวมัน-1 ถ้ามี20 ได้19
            groupMessageAdapter.notifyDataSetChanged();
        }

        isRequesting = false;
        bt_group_send_message.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();

            GetFile getFile = new GetFile(this);
            String filename = getFile.getFileName(uri);
            byte[] filedata = getFile.getData(uri);

            if (filedata == null) {
                return;
            }
            if (filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                Bitmap bm = BitmapFactory.decodeByteArray(filedata, 0, filedata.length);
                Bitmap resized = null;

                if (bm.getWidth() > bm.getHeight()) {
                    resized = Bitmap.createScaledBitmap(bm, 800, (int) (800 * ((float) bm.getHeight() / bm.getWidth())), true);
                } else {
                    resized = Bitmap.createScaledBitmap(bm, (int) (800 * ((float) bm.getWidth() / bm.getHeight())), 800, true);
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                filedata = baos.toByteArray();
            }

            String md5 = GetMD5.getMD5EncryptedString(Base64.encodeToString(filedata, Base64.DEFAULT));
            String encryptFile = aesEncryption.encrypt(filedata);
//            String encryptFile = Base64.encodeToString(filedata,Base64.DEFAULT);//no encrypt
            new BackgoundWorker(GroupMessageActivity.this).execute("sendmessagegroup", id, groupId, encryptFile, "file", filename, "", "", token, md5);


        }
    }

    private boolean checkFilePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {//เแอพนี้ช็คว่ามีสิทธอ่านไฟล์รึยัง

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);//ถ้ายังก็ขอ
            return false;
        }
        return true;
    }

    public String checkhashkey() {
        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
//        return sp.getString("SHARED_KEY:" + friendid, "1234567890asdfgh1234567890asdfgh");
        return sp.getString("SHARED_KEY_GROUP:" + groupId, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_groupchat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(GroupMessageActivity.this, ContactActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.call_group:
                Intent i3 = new Intent(GroupMessageActivity.this, CallGroupActivity.class);
                i3.putExtra("groupid", groupId);
                i3.putExtra("groupname", groupName);
                startActivity(i3);
                return true;
            case R.id.invite_friend:
                Intent i = new Intent(GroupMessageActivity.this, InviteGroupActivity.class);
                i.putExtra("groupid", groupId);
                i.putExtra("groupname", groupName);
                startActivity(i);
                return true;
            case R.id.member:
                Intent i2 = new Intent(GroupMessageActivity.this, MemberGroupActivity.class);
                i2.putExtra("groupid", groupId);
                i2.putExtra("groupname", groupName);
                startActivity(i2);
                return true;
            case R.id.leave:
                new AlertDialog.Builder(this)
                        .setTitle("Leave group")
                        .setMessage("Are you sure you want to leave this group?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                Intent i4 = new Intent(GroupMessageActivity.this, ContactActivity.class);
                                new BackgoundWorker(GroupMessageActivity.this).execute("leavegroup", id, token, groupId);
                                SharedPreferences.Editor editor = getSharedPreferences("MySetting", MODE_PRIVATE).edit();
                                editor.remove("SHARED_KEY_GROUP:" + groupId);
                                editor.apply();
                                startActivityForResult(i4, 1);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
