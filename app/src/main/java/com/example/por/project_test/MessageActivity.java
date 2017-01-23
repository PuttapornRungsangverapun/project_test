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
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
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

public class MessageActivity extends AppCompatActivity implements HttpRequestCallback {
    ListView listView_message;
    Button bt_send, bt_file;
    EditText et_message;
    static String id, token, shareedkey;
    static int REQUEST_FILE = 1;
    String friendid, publickey, privatekey;
    int lastMessageId;
    ArrayList<MessageInfo> messageInfos;
    MessageAdapter messageAdapter;
    boolean isRequesting;
    Timer t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        lastMessageId = 0;
        listView_message = (ListView) findViewById(R.id.listview_message);
        et_message = (EditText) findViewById(R.id.et_message);
        bt_send = (Button) findViewById(R.id.bt_send_message);
        bt_file = (Button) findViewById(R.id.bt_file);

        Intent i = getIntent();
        friendid = i.getStringExtra("friendid");//เพราะว่าเป็นinnerclassทำงานตอนกด
        publickey = i.getStringExtra("publickey");
        shareedkey = checkhashkey();

        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        id = sp.getString("user_id_current", "-1");
        token = sp.getString("token", "-1");
        privatekey = sp.getString("privatekey", "-1");

        messageInfos = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, R.layout.message, R.id.tv_message_adapter, messageInfos, token, id);
        listView_message.setAdapter(messageAdapter);

        setTitle(i.getStringExtra("frienduser"));

        listView_message.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MessageInfo message = messageInfos.get(i);
                String url = BackgoundWorker.url_server + "downloadfile.php?messageid=" + message.message_id + "&token=" + token + "&userid=" + id;
                String filename = message.filename;
                if (message.type.equals("file")) {
                    Intent intent = new Intent(MessageActivity.this, DownloadFileService.class);
                    intent.putExtra("url", url);
                    intent.putExtra("filename", filename);
                    startService(intent);
                } else if (message.type.equals("map")) {
                    Intent intent = new Intent(MessageActivity.this, MapsActivity.class);
                    intent.putExtra("lat", message.latitude);
                    intent.putExtra("lon", message.longtitude);
                    startActivity(intent);
                }
            }
        });
        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String type2 = "sendmessage";

                String str_message = et_message.getText().toString().trim();
                if (str_message.isEmpty() || str_message.length() == 0 || str_message.equals("") || str_message == null) {
                    return;
                } else {

                    if (shareedkey == null) {

                        genSharedKey(type2);
                    }

                    //secure
                    str_message = encrypt(str_message);
                    Log.d("str message", str_message);

                    BackgoundWorker backgoundWorker = new BackgoundWorker(MessageActivity.this);
                    backgoundWorker.execute("sendmessage", id, friendid, str_message, "text", "", "", "", token, "");
                    et_message.setText("");
                }


            }
        });
        bt_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String type3 = "sendmessage";
                if (shareedkey == null) {


                    genSharedKey(type3);
                }

                CharSequence colors[] = new CharSequence[]{"FIle", "Share Location"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
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
                            Intent intent = new Intent(MessageActivity.this, MapsActivity.class);
                            intent.putExtra("friendid", friendid);
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
                    String type1 = "readmessage";
                    BackgoundWorker backgoundWorker = new BackgoundWorker(MessageActivity.this);
                    backgoundWorker.execute(type1, id, friendid, lastMessageId + "", token);
                }
            }
        }, 500, 500);
    }

    private void genSharedKey(String type2) {
        while ((shareedkey == null) || (shareedkey.length() != 32)) {
            shareedkey = new BigInteger(160, new SecureRandom()).toString(32);
        }

        SharedPreferences.Editor editor = getSharedPreferences("MySetting", MODE_PRIVATE).edit();
        editor.putString("SHARED_KEY:" + friendid, shareedkey);
        editor.commit();

        String sharedKeyMessage = RSAEncrypt(shareedkey);

        BackgoundWorker backgoundWorker = new BackgoundWorker(MessageActivity.this);
        backgoundWorker.execute(type2, id, friendid, sharedKeyMessage, "authen", "", "", "", token, "");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//data สิ่งที่activityกลับมาคืนเรา
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


            BackgoundWorker backgoundWorker2 = new BackgoundWorker(MessageActivity.this);
            backgoundWorker2.execute("sendmessage", id, friendid, encryptFile, "file", filename, "", "", token, md5);


        }
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
        ArrayList<MessageInfo> messageInfos = new ArrayList<>();
        for (Object o : mesObjects) {

            if (o instanceof MessageInfo) {//เข็คoใช่objectของclassหรือไม่
                MessageInfo mo = (MessageInfo) o;
                //secure
                if (mo.type.equals("authen")) {
                    if (shareedkey == null) {
                        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
                        privatekey = sp.getString("privatekey", "-1");
                        shareedkey = RSADecrypt(mo.message);
                    }
                } else if (mo.type.equals("map")) {
                    try {
                        mo.latitude = Double.parseDouble(decrypt(mo.tmpLat));
                        mo.longtitude = Double.parseDouble(decrypt(mo.tmpLon));
                        messageInfos.add(mo);
                    } catch (Exception e) {
                    }
                } else if (mo.type.equals("text")) {
                    try {
                        mo.message = decrypt(mo.message);
                        messageInfos.add(mo);
                    } catch (Exception e) {
                        e.printStackTrace();
                        mo.message = "failed to decrypt...";
                        messageInfos.add(mo);
                    }
                } else {
                    messageInfos.add(mo);
                }


            }

        }

        if (messageInfos.size() > 0) {
            this.messageInfos.addAll(messageInfos);
            lastMessageId = messageInfos.get(messageInfos.size() - 1).message_id;//ขนาดของตัวมัน-1 ถ้ามี20 ได้19
            messageAdapter.notifyDataSetChanged();
        }

        isRequesting = false;
        bt_send.setEnabled(true);
    }

    //Read file fromuri
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
//                System.out.println("encrypted string: " + Base64.encodeToString(encrypted, Base64.DEFAULT));
            } else {
                //ByteArrayOutputStream bos = new ByteArrayOutputStream();
                //CipherOutputStream cios = new CipherOutputStream(bos, cipher);
                //cios.write(data);
                //encrypted = bos.toByteArray();

                encrypted = cipher.doFinal(data);
//                System.out.println("encrypted string: " + Base64.encodeToString(encrypted, Base64.DEFAULT));
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
            Log.e("secret", shareedkey);

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

    public String checkhashkey() {
        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
//        return sp.getString("SHARED_KEY:" + friendid, "1234567890asdfgh1234567890asdfgh");
        return sp.getString("SHARED_KEY:" + friendid, null);
    }

    //    public String gensharesecretkey(){
//        String initVector = new BigInteger(160, new SecureRandom()).toString(32);
//    }
    private String RSAEncrypt(String myMessage) {
        RSAPublicKey pbKey = null;

        byte[] keyBytes = null;
        try {
            keyBytes = Base64.decode(publickey.getBytes("utf-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            pbKey = (RSAPublicKey) keyFactory.generatePublic(spec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

///

        // Get an instance of the Cipher for RSA encryption/decryption
        Cipher c = null;
        try {
            c = Cipher.getInstance("RSA");
            // Initiate the Cipher, telling it that it is going to Encrypt, giving it the public key
            c.init(Cipher.ENCRYPT_MODE, pbKey);

            // Encrypt that message using a new SealedObject and the Cipher we created before
            String msg = Base64.encodeToString(c.doFinal(myMessage.getBytes("UTF-8")), Base64.DEFAULT);

            return msg;
        } catch (Exception e) {
            e.printStackTrace();
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
//        String md5 = new BigInteger(1, mdEnc.digest()).toString(16);
//        while (md5.length() < 32) {
//            md5 = "0" + md5;
//        }
        byte messageDigest[] = mdEnc.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte aMessageDigest : messageDigest) {
            String h = Integer.toHexString(0xFF & aMessageDigest);
            while (h.length() < 2)
                h = "0" + h;
            hexString.append(h);
        }
        return hexString.toString();
//        return md5;
    }
}

