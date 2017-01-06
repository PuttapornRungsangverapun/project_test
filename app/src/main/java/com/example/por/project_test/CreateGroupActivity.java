package com.example.por.project_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class CreateGroupActivity extends AppCompatActivity implements HttpRequestCallback {
    EditText et_namegroup;
    Button bt_creategroup_submit;
    ListView lv_addgroup;
    String id, token, type;
    CreateGrouptAdapter createGrouptAdapter;
    ArrayList<AddUserGroupInfo> addUserGroupInfos;
    CheckBox chk_addgroup;

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

        type = "listaddgroup";
        BackgoundWorker backgoundWorker = new BackgoundWorker(this);
        backgoundWorker.execute(type, id + "", token);


        bt_creategroup_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = et_namegroup.getText().toString().trim();
                if (groupName.isEmpty() || groupName.length() == 0 || groupName.equals("") || groupName == null) {
                    et_namegroup.setError("Group name must be filled");
                    return;
                } else {
                    for (int i = 0; i < createGrouptAdapter.mCheckStates.size(); i++) {
                        if (createGrouptAdapter.mCheckStates.get(i) == true) {
                            type = "crategroup";
                            String friendid = addUserGroupInfos.get(i).userid + "";
                            BackgoundWorker backgoundWorker = new BackgoundWorker(CreateGroupActivity.this);
                            backgoundWorker.execute(type, token, friendid, groupName, id);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onResult(String[] result, ArrayList<Object> userList) {
        if ((result == null) && (userList == null)) {
            return;
        } if (userList == null) {
            return;
        } if ((result != null) && (result[0].equals(BackgoundWorker.TRUE))) {
            Toast.makeText(this, result[1], Toast.LENGTH_SHORT).show();
            finish();
        } if ((userList != null) && (result == null)) {
            addUserGroupInfos = new ArrayList<>();
            for (Object o : userList) {
                if (o instanceof AddUserGroupInfo)//เข็คoใช่objectของclassหรือไม่
                    addUserGroupInfos.add((AddUserGroupInfo) o);

            }
            createGrouptAdapter = new CreateGrouptAdapter(this, R.layout.contact, addUserGroupInfos);
            lv_addgroup.setAdapter(createGrouptAdapter);


        }
    }


}
