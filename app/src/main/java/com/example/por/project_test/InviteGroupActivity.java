package com.example.por.project_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
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

import static com.example.por.project_test.R.id.lv_addgroup;

public class InviteGroupActivity extends AppCompatActivity implements HttpRequestCallback {

    private ArrayList<InviteGroupInfo> inviteGroupInfos;
    private InviteGroupAdapter inviteGroupAdapter;
    private ListView lv_invitegroup;
    private static String id, token, shareedkey;
    private String groupId;
    Button bt_invite;
    private List<Integer> groupMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_group);

        lv_invitegroup = (ListView) findViewById(R.id.lv_invitegroup);
        bt_invite = (Button) findViewById(R.id.bt_invite);

        inviteGroupInfos = new ArrayList<>();
        groupMember = new ArrayList<>();
        inviteGroupAdapter = new InviteGroupAdapter(this, R.layout.invitegroup, inviteGroupInfos);
        lv_invitegroup.setAdapter(inviteGroupAdapter);

        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        id = sp.getString("user_id_current", "-1");
        token = sp.getString("token", "-1");

        Intent i = getIntent();
        groupId = i.getStringExtra("groupid");
        setTitle("Group : " + i.getStringExtra("groupname"));


        BackgoundWorker backgoundWorker = new BackgoundWorker(InviteGroupActivity.this);
        backgoundWorker.execute("listinvitefriend", id, token, groupId);

        bt_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < inviteGroupAdapter.value.size(); i++) {
                    if (inviteGroupAdapter.mCheckStates.get(i) == true) {
                        String friendid = inviteGroupInfos.get(i).userid + "";
                        BackgoundWorker backgoundWorker = new BackgoundWorker(InviteGroupActivity.this);
                        backgoundWorker.execute("invitefriend", id, token, friendid, groupId);
                        groupMember.add(inviteGroupInfos.get(i).userid);
                    }
                }

            }
        });
    }

    @Override
    public void onResult(String[] result, ArrayList<Object> userList) {
        if ((result != null) && (result[0].equals(BackgoundWorker.TRUE))) {
            Toast.makeText(this, result[1], Toast.LENGTH_SHORT).show();
            genSharedKey(groupId);
            finish();
        }
        if ((result != null) && (result[0].equals("getpublickey"))) {
            String friendid = result[1];
            String publickey = result[2];
            String sharedKeyMessage = RSAEncrypt(publickey, shareedkey);
            BackgoundWorker backgoundWorker = new BackgoundWorker(InviteGroupActivity.this);
            backgoundWorker.execute("sendmessagegroup", id, groupId, sharedKeyMessage, "authen", "", "", "", token, friendid);
        }
        if ((userList == null) && (result == null)) {
            return;
        } else if (userList == null) {
            return;
        } else if ((result == null) && userList != null) {
            inviteGroupInfos = new ArrayList<>();
            for (Object o : userList) {
                if (o instanceof InviteGroupInfo)//เข็คoใช่objectของclassหรือไม่
                    inviteGroupInfos.add((InviteGroupInfo) o);

            }
            inviteGroupAdapter = new InviteGroupAdapter(this, R.layout.invitegroup, inviteGroupInfos);
            lv_invitegroup.setAdapter(inviteGroupAdapter);
        }
    }

    private void genSharedKey(String groupid) {
        while ((shareedkey == null) || (shareedkey.length() != 32)) {
            shareedkey = new BigInteger(160, new SecureRandom()).toString(32);
        }

        SharedPreferences.Editor editor = getSharedPreferences("MySetting", MODE_PRIVATE).edit();
        editor.putString("SHARED_KEY_GROUP:" + groupid, shareedkey);
        editor.commit();


        for (int friendid : groupMember) {
            BackgoundWorker backgoundWorker = new BackgoundWorker(InviteGroupActivity.this);
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
