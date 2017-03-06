package com.example.por.project_test;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;

public class CreateGroupActivity extends AppCompatActivity implements HttpRequestCallback {
    EditText et_namegroup;
    Button bt_creategroup_submit;
    ListView lv_addgroup;
    static String id, token, shareedkey;
    String groupId;
    CreateGrouptAdapter createGrouptAdapter;
    ArrayList<AddUserGroupInfo> addUserGroupInfos;
    CheckBox chk_addgroup;
    List<Integer> groupMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        et_namegroup = (EditText) findViewById(R.id.et_namegroup);
        bt_creategroup_submit = (Button) findViewById(R.id.bt_creategroup_submit);
        lv_addgroup = (ListView) findViewById(R.id.lv_addgroup);
        chk_addgroup = (CheckBox) findViewById(R.id.chk_addgroup);

        addUserGroupInfos = new ArrayList<>();
        createGrouptAdapter = new CreateGrouptAdapter(this, R.layout.contact_creategroup, addUserGroupInfos);
        lv_addgroup.setAdapter(createGrouptAdapter);

        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        id = sp.getString("user_id_current", "-1");
        token = sp.getString("token", "-1");


        BackgoundWorker backgoundWorker = new BackgoundWorker(this);
        backgoundWorker.execute("listaddgroup", id + "", token);
        groupMember = new ArrayList<>();

        bt_creategroup_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = et_namegroup.getText().toString().trim();
                if (groupName.isEmpty() || groupName.length() == 0 || groupName.equals("") || groupName == null) {
                    et_namegroup.setError("Group name must be filled");
                    return;
                } else {

                    for (int i = 0; i < createGrouptAdapter.value.size(); i++) {
                        if (createGrouptAdapter.mCheckStates.get(i) == true) {
                            String friendid = addUserGroupInfos.get(i).userid + "";
                            BackgoundWorker backgoundWorker = new BackgoundWorker(CreateGroupActivity.this);
                            backgoundWorker.execute("crategroup", id, token, friendid, groupName);
                            groupMember.add(addUserGroupInfos.get(i).userid);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onResult(String[] result, ArrayList<Object> userList) {
        if ((result != null) && (result[0].equals(BackgoundWorker.TRUE))) {
            Toast.makeText(this, result[1], Toast.LENGTH_SHORT).show();
            groupId = result[2];
            genSharedKey(result[2]);
            finish();
        }
        if ((result != null) && (result[0].equals("getpublickey"))) {
            String friendid = result[1];
            String publickey = result[2];
            String sharedKeyMessage = RSAEncrypt(publickey, shareedkey);
            BackgoundWorker backgoundWorker = new BackgoundWorker(CreateGroupActivity.this);
            backgoundWorker.execute("sendmessagegroup", id, groupId, sharedKeyMessage, "authen", "", "", "", token, friendid);
        }
        if ((userList == null) && (result == null)) {
            return;
        } else if (userList == null) {
            return;
        } else if ((result == null) && userList != null) {
            addUserGroupInfos = new ArrayList<>();
            for (Object o : userList) {
                if (o instanceof AddUserGroupInfo)//เข็คoใช่objectของclassหรือไม่
                    addUserGroupInfos.add((AddUserGroupInfo) o);

            }
            createGrouptAdapter = new CreateGrouptAdapter(this, R.layout.contact, addUserGroupInfos);
            lv_addgroup.setAdapter(createGrouptAdapter);
        }
    }

    private void genSharedKey(String groupid) {
        while ((shareedkey == null) || (shareedkey.length() != 32)) {
            shareedkey = new BigInteger(160, new SecureRandom()).toString(32);
        }

        SharedPreferences.Editor editor = getSharedPreferences("MySetting", MODE_PRIVATE).edit();
        editor.putString("SHARED_KEY_GROUP:" + groupid, shareedkey);
        editor.commit();

        //for loop
        for (int friendid : groupMember) {
            BackgoundWorker backgoundWorker = new BackgoundWorker(CreateGroupActivity.this);
            backgoundWorker.execute("getpublickey", id, friendid + "", token);

        }

    }

    private String RSAEncrypt(String publickey, String myMessage) {
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
}
