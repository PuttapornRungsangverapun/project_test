package com.example.por.project_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ContactActivity extends AppCompatActivity implements HttpRequestCallback {

    private ListView lv_contact;
    com.github.clans.fab.FloatingActionButton fb_searchFriend, fb_createGroup, fb_requestFriend;
    private String id, token;
    private ContactAdapter contactAdapter;
    private ArrayList<UserInfo> userInfos;
    private boolean back = true;
    FloatingActionMenu materialDesignFAM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        lv_contact = (ListView) findViewById(R.id.lv_contact);
//        add_friend = (FloatingActionButton) findViewById(R.id.add_friend);

        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        fb_searchFriend = (FloatingActionButton) findViewById(R.id.fb_searchFriend);
        fb_createGroup = (FloatingActionButton) findViewById(R.id.fb_createGroup);
        fb_requestFriend = (FloatingActionButton) findViewById(R.id.fb_requestFriend);


        userInfos = new ArrayList<>();

        contactAdapter = new ContactAdapter(this, R.layout.contact, userInfos);
        lv_contact.setAdapter(contactAdapter);


        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        id = sp.getString("user_id_current", "-1");
        token = sp.getString("token", "-1");
        setTitle(sp.getString("username", "-1"));

        lv_contact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (userInfos.get(i).userid > 0) {
                    Intent ii = new Intent(ContactActivity.this, MessageActivity.class);
                    ii.putExtra("friendid", userInfos.get(i).userid + "");//มันส่งobjectธรรมดามาเลยcast
                    ii.putExtra("frienduser", userInfos.get(i).username + "");
                    ii.putExtra("publickey", userInfos.get(i).publickey + "");
                    startActivity(ii);
                    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                } else {
                    Intent ii = new Intent(ContactActivity.this, GroupMessageActivity.class);
                    ii.putExtra("groupid", userInfos.get(i).groupid + "");
                    ii.putExtra("groupname", userInfos.get(i).groupname + "");
                    ii.putExtra("userid", id + "");
                    startActivity(ii);
                    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                }
                //Toast.makeText(ContactActivity.this, adapterView.getAdapter().getItem(i).toString(), Toast.LENGTH_SHORT).show();


            }
        });

        fb_createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ContactActivity.this, CreateGroupActivity.class);
                startActivityForResult(i, 1);
            }
        });
        fb_searchFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ContactActivity.this, AddfriendActivity.class);
                startActivityForResult(i, 1);
            }
        });
        fb_requestFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ContactActivity.this, RequestFriendActivity.class);
                startActivityForResult(i, 1);
            }
        });

        new BackgoundWorker(this).execute("listfriend", id + "", token);

        String token_noti = FirebaseInstanceId.getInstance().getToken();
        new BackgoundWorker(this).execute("notification", id + "", token_noti, token);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        new BackgoundWorker(this).execute("listfriend", id + "", token);
    }

    @Override
    public void onResult(String[] result, ArrayList<Object> userList) {
        userInfos = new ArrayList<>();
        for (Object o : userList) {
            if (o instanceof UserInfo)//เข็คoใช่objectของclassหรือไม่
                userInfos.add((UserInfo) o);
        }

        Collections.sort(userInfos, new Comparator<UserInfo>() {
            @Override
            public int compare(UserInfo o1, UserInfo o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });


        contactAdapter = new ContactAdapter(this, R.layout.contact, userInfos);
        lv_contact.setAdapter(contactAdapter);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (back) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        if (searchMenuItem == null) {
            return true;
        }

        final SearchView searchView = (SearchView) searchMenuItem.getActionView();

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                back = false;
                // Set styles for expanded state here
                if (getSupportActionBar() != null) {
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {

                            ContactActivity.this.contactAdapter.getFilter().filter(query);
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            ContactActivity.this.contactAdapter.getFilter().filter(newText);
                            return false;
                        }
                    });
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                back = true;
                // Set styles for collapsed state here
                if (getSupportActionBar() != null) {

                }
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:
                SharedPreferences.Editor editor = getSharedPreferences("MySetting", MODE_PRIVATE).edit();
                editor.clear();
                editor.apply();
                Intent i = new Intent(ContactActivity.this, MainActivity.class);
                startActivity(i);
                return true;
            case R.id.create_group:
                Intent i2 = new Intent(ContactActivity.this, CreateGroupActivity.class);
                startActivityForResult(i2, 1);
                return true;
            case R.id.setting:
                Intent i3 = new Intent(ContactActivity.this, SettingsActivity.class);
                startActivityForResult(i3, 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
