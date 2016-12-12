package com.example.por.project_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

public class ContactActivity extends AppCompatActivity implements HttpRequestCallback {

    ListView lv_contact;
    FloatingActionButton add_friend;
    String id;
    String token;
    ContactAdapter contactAdapter;
    ArrayList<UserInfo> userInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);


        lv_contact = (ListView) findViewById(R.id.lv_contact);
        add_friend = (FloatingActionButton) findViewById(R.id.add_friend);

        userInfos = new ArrayList<>();

        contactAdapter = new ContactAdapter(this, R.layout.contact, userInfos);
        lv_contact.setAdapter(contactAdapter);

        //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        id = sp.getString("user_id_current", "-1");
        token = sp.getString("token", "-1");

        lv_contact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent ii = new Intent(ContactActivity.this, MessageActivity.class);
                ii.putExtra("friendid", userInfos.get(i).userid + "");//มันส่งobjectธรรมดามาเลยcast
                ii.putExtra("frienduser", userInfos.get(i).username + "");
                ii.putExtra("publickey", userInfos.get(i).publickey + "");
                startActivity(ii);
                //Toast.makeText(ContactActivity.this, adapterView.getAdapter().getItem(i).toString(), Toast.LENGTH_SHORT).show();


            }
        });

        add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ContactActivity.this, AddfriendActivity.class);
                startActivityForResult(i, 1);
            }
        });


        String type = "listfriend";
        BackgoundWorker backgoundWorker = new BackgoundWorker(this);
        backgoundWorker.execute(type, id + "", token);

        String token_noti = FirebaseInstanceId.getInstance().getToken();
        String type2 = "notification";
        backgoundWorker = new BackgoundWorker(this);
        backgoundWorker.execute(type2, id + "", token_noti, token);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String type = "listfriend";
        BackgoundWorker backgoundWorker = new BackgoundWorker(this);
        backgoundWorker.execute(type, id + "", token);
    }

    @Override
    public void onResult(String[] result, ArrayList<Object> userList) {
        userInfos = new ArrayList<>();
        for (Object o : userList) {
            if (o instanceof UserInfo)//เข็คoใช่objectของclassหรือไม่
                userInfos.add((UserInfo) o);

        }
        contactAdapter = new ContactAdapter(this, R.layout.contact, userInfos);
        lv_contact.setAdapter(contactAdapter);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                SharedPreferences.Editor editor = getSharedPreferences("MySetting", MODE_PRIVATE).edit();
                editor.clear();
                editor.commit();
                Intent i = new Intent(ContactActivity.this, MainActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
