package com.example.por.project_test;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class CreateGroupActivity extends AppCompatActivity implements HttpRequestCallback {
    EditText et_namegroup;
    Button bt_creategroup_submit;
    ListView lv_addgroup;
    static String id, token, shareedkey;
    String groupId, publicKey;
    RSAEncryption rsaEncryption;
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

        rsaEncryption = new RSAEncryption(this);

        addUserGroupInfos = new ArrayList<>();
        createGrouptAdapter = new CreateGrouptAdapter(this, R.layout.contact_creategroup, addUserGroupInfos);
        lv_addgroup.setAdapter(createGrouptAdapter);

        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        id = sp.getString("user_id_current", "-1");
        token = sp.getString("token", "-1");
        publicKey = sp.getString("publickey", "-1");

        new BackgoundWorker(this).execute("listaddgroup", id + "", token);
        groupMember = new ArrayList<>();

        bt_creategroup_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = et_namegroup.getText().toString().trim();
                if (groupName.isEmpty() || groupName.length() == 0 || groupName.equals("")) {
                    et_namegroup.setError("Group name must be filled");
                } else {

                    for (int i = 0; i < createGrouptAdapter.value.size(); i++) {
                        if (createGrouptAdapter.mCheckStates.get(i)) {
                            String friendid = addUserGroupInfos.get(i).userid + "";
                            new BackgoundWorker(CreateGroupActivity.this).execute("crategroup", id, token, friendid, groupName);
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
            String sharedKeyMessage = rsaEncryption.RSAEncrypt(publickey, shareedkey);
            String mysSharedKeyMessage = rsaEncryption.RSAEncrypt(this.publicKey, shareedkey);

            new BackgoundWorker(CreateGroupActivity.this).execute("sendmessagegroup", id, groupId, sharedKeyMessage, "authen", "", "", "", token, friendid);

            new BackgoundWorker(CreateGroupActivity.this).execute("sendmessagegroup", id, groupId, mysSharedKeyMessage, "authen", "", "", "", token, id);
        }
        if ((userList == null) && (result == null)) {
            return;
        } else if (userList == null) {
            return;
        } else if (result == null) {
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
            shareedkey = "00000000000000000000000000000000".substring(shareedkey.length()) + shareedkey;
        }

        SharedPreferences.Editor editor = getSharedPreferences("MySetting", MODE_PRIVATE).edit();
        editor.putString("SHARED_KEY_GROUP:" + groupid, shareedkey);
        editor.apply();

        //for loop
        for (int friendid : groupMember) {
            new BackgoundWorker(this).execute("getpublickey", id, friendid + "", token);
        }

    }
}
