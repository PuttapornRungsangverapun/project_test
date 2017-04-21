package com.example.por.project_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class InviteGroupActivity extends AppCompatActivity implements HttpRequestCallback {

    private ArrayList<InviteGroupInfo> inviteGroupInfos;
    private InviteGroupAdapter inviteGroupAdapter;
    private ListView lv_invitegroup;
    private static String id, token, shareedkey;
    private String groupId;
    Button bt_invite;
    RSAEncryption rsaEncryption;
    private List<Integer> groupMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_group);

        lv_invitegroup = (ListView) findViewById(R.id.lv_invitegroup);
        bt_invite = (Button) findViewById(R.id.bt_invite);

        rsaEncryption = new RSAEncryption(this);

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

        new BackgoundWorker(this).execute("listinvitefriend", id, token, groupId);

        bt_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < inviteGroupAdapter.value.size(); i++) {
                    if (inviteGroupAdapter.mCheckStates.get(i)) {
                        String friendid = inviteGroupInfos.get(i).userid + "";
                      new BackgoundWorker(InviteGroupActivity.this).execute("invitefriend", id, token, friendid, groupId);
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
//            genSharedKey(groupId);
            SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
            shareedkey = sp.getString("SHARED_KEY_GROUP:" + groupId, "-1");

            for (int friendid : groupMember) {
                new BackgoundWorker(this).execute("getpublickey", id, friendid + "", token);

            }

            finish();
        }
        if ((result != null) && (result[0].equals("getpublickey"))) {
            String friendid = result[1];
            String publickey = result[2];
            String sharedKeyMessage = rsaEncryption.RSAEncrypt(publickey, shareedkey);
            new BackgoundWorker(this).execute("sendmessagegroup", id, groupId, sharedKeyMessage, "authen", "", "", "", token, friendid);
        }
        if ((userList == null) && (result == null)) {
            return;
        } else if (userList == null) {
            return;
        } else if (result == null) {
            inviteGroupInfos = new ArrayList<>();
            for (Object o : userList) {
                if (o instanceof InviteGroupInfo)//เข็คoใช่objectของclassหรือไม่
                    inviteGroupInfos.add((InviteGroupInfo) o);

            }
            inviteGroupAdapter = new InviteGroupAdapter(this, R.layout.invitegroup, inviteGroupInfos);
            lv_invitegroup.setAdapter(inviteGroupAdapter);
        }
    }
}
