package com.example.por.project_test;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class GroupMessageActivity extends AppCompatActivity implements HttpRequestCallback {

    private static String id, token, shareedkey;
    private String groupId, groupName, privatekey;
    private boolean isRequesting;
    private ArrayList<GroupMessageInfo> groupMessageInfos;
    private GroupMessageAdapter groupMessageAdapter;
    private int lastMessageId;
    private static int REQUEST_FILE = 1;
    private Button bt_group_file, bt_group_send_message;
    private EditText et_group_message;
    private ListView listView_group_message;
    private Timer t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message);

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

        groupMessageInfos = new ArrayList<>();
        groupMessageAdapter = new GroupMessageAdapter(this, R.layout.message, R.id.tv_message_adapter, groupMessageInfos, token, id);
        listView_group_message.setAdapter(groupMessageAdapter);

        listView_group_message.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id2) {
                GroupMessageInfo groupMessageInfo = groupMessageInfos.get(position);

                String url = BackgoundWorker.url_server + "downloadfilegroup.php?messageid=" + groupMessageInfo.group_message_id + "&token=" + token + "&userid=" + id;
                String filename = groupMessageInfo.filename;
                if (groupMessageInfo.type.equals("file")) {
                    Intent intent = new Intent(GroupMessageActivity.this, DownloadFileService.class);
                    intent.putExtra("url", url);
                    intent.putExtra("filename", filename);
                    intent.putExtra("type", "group");
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
                if (str_message.isEmpty() || str_message.length() == 0 || str_message.equals("") || str_message == null) {
                    return;
                } else {
                    str_message = encrypt(str_message);
                    BackgoundWorker backgoundWorker = new BackgoundWorker(GroupMessageActivity.this);
                    backgoundWorker.execute("sendmessagegroup", id, groupId, str_message, "text", "", "", "", token);
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
                            if (checkFilePermission() == false) {
                                return;
                            }
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);//แสดงไฟล์เฉพาะactivityเปิดได้
                            intent.setType("*/*");//(image/jpg)
                            startActivityForResult(intent, REQUEST_FILE);
                        } else if (which == 1) {
                            Intent intent = new Intent(GroupMessageActivity.this, MapsActivity.class);
                            intent.putExtra("groupid", groupId);
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
                    BackgoundWorker backgoundWorker = new BackgoundWorker(GroupMessageActivity.this);
                    backgoundWorker.execute("readmessagegroup", id, groupId, lastMessageId + "", token);
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
                if (mo.type.equals("authen")) {
                    if ((mo.target_id + "").equals(id)) {
                        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
                        privatekey = sp.getString("privatekey", "-1");
                        shareedkey = RSADecrypt(mo.message);
                    }
                } else if (mo.type.equals("map")) {
                    try {
                        mo.latitude = Double.parseDouble(decrypt(mo.tmpLat));
                        mo.longtitude = Double.parseDouble(decrypt(mo.tmpLon));
                        groupMessageInfos.add(mo);
                    } catch (Exception e) {
                    }
                } else if (mo.type.equals("text")) {
                    try {
                        mo.message = decrypt(mo.message);
                        groupMessageInfos.add(mo);
                    } catch (Exception e) {
                        e.printStackTrace();
                        mo.message = "failed to decrypt...";
                        groupMessageInfos.add(mo);
                    }
                } else {
                    groupMessageInfos.add(mo);
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
            String filename = getFileName(uri);
            byte[] filedata = getData(uri);

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

            String md5 = getMD5EncryptedString(Base64.encodeToString(filedata, Base64.DEFAULT));
            String encryptFile = encrypt(filedata);
//            String encryptFile = Base64.encodeToString(filedata,Base64.DEFAULT);//no encrypt

            BackgoundWorker backgoundWorker = new BackgoundWorker(GroupMessageActivity.this);
            backgoundWorker.execute("sendmessagegroup", id, groupId, encryptFile, "file", filename, "", "", token, md5);


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

    private byte[] getData(Uri uri) {//อ่านไฟล์โดยให้pathไปreturnเป็นbyte binaryกลับมา
        byte[] result = null;
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);

            if (inputStream.available() > 10e6) {
                Toast.makeText(this, "File size must be 10mb", Toast.LENGTH_SHORT).show();
                inputStream.close();
                return null;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];//อ่านทั้ฝหมด16kb ในเgoogleบอกเร็วสุด
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();//เขียนข้อไปให้หมด

            result = buffer.toByteArray();
            inputStream.close();//ถ้าไม่ปิดแสดงว่ามีคนใช้อยู่จะลบไม่ได้


        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public String encrypt(String msg, byte[] data) {
        String key = shareedkey; // 256 bit key
        String initVector = new BigInteger(80, new SecureRandom()).toString(32); // 80/5=16 bytes IV   80bitแบบrandom tostringเป็นbase32 ตัวหนังสือ1ตัวเท่ากับ32bit ได้ 16 ตัว
        return initVector + encrypt(key, initVector, msg, data);
    }

    public String encrypt(String msg) {
        return encrypt(msg, null);
    }

    public String encrypt(byte[] data) {
        return encrypt(null, data);
    }


    public String encrypt(String key, String initVector, String value, byte[] data) {
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

    private static byte[] decrypt(String encrypted, byte[] data) {
        String iv = null;
        String cyphertext = null;
        String key = shareedkey;

        if (encrypted == null) {
            byte[] iv2 = new byte[16];
            byte[] data2 = new byte[data.length - 16];
            System.arraycopy(data, 0, iv2, 0, 16);
            System.arraycopy(data, 16, data2, 0, data2.length);
            return decrypt(key, new String(iv2), null, data2);

        } else {
            iv = encrypted.substring(0, 16);
            cyphertext = encrypted.substring(16);//begin index 16
            return decrypt(key, iv, cyphertext, data);
        }
    }

    public static String decrypt(String encrypted) {

        return new String(decrypt(encrypted, null));

    }

    public static byte[] decrypt(byte[] data) {

        return decrypt(null, data);

    }

    private static byte[] decrypt(String key, String initVector, String encrypted, byte[] data) {
        byte[] original;
        try {
//            Log.e("secret", shareedkey);

            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
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


    private String RSADecrypt(String myMessage) {
        RSAPrivateKey pvKey = null;

        byte[] keyBytes = null;
        try {
            keyBytes = Base64.decode(privatekey.getBytes("utf-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            pvKey = (RSAPrivateKey) keyFactory.generatePrivate(spec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

///

        // Get an instance of the Cipher for RSA encryption/decryption
        Cipher c = null;
        try {
            c = Cipher.getInstance("RSA");
            // Initiate the Cipher, telling it that it is going to Encrypt, giving it the public key
            c.init(Cipher.DECRYPT_MODE, pvKey);

            // Encrypt that message using a new SealedObject and the Cipher we created before
            String msg = new String(c.doFinal(Base64.decode(myMessage, Base64.DEFAULT)));

            return msg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static String getMD5EncryptedString(String encTarget) {
        MessageDigest mdEnc = null;
        byte[] data = Base64.decode(encTarget, Base64.DEFAULT);
        try {
            mdEnc = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception while encrypting to md5");
            e.printStackTrace();
        } // Encryption algorithm
        mdEnc.update(data);//encTarget.getBytes()
        byte messageDigest[] = mdEnc.digest();
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
