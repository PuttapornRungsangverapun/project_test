package com.example.por.project_test;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by User on 30/4/2560.
 */

public class RequestFriendAdapter extends ArrayAdapter<RequestFriendInfo> {
    private final Context ctx;
    List<RequestFriendInfo> requestFriendInfos;
    private String token,id;


    public RequestFriendAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<RequestFriendInfo> objects) {
        super(context, resource, objects);
        this.ctx = context;
        this.requestFriendInfos = objects;
        SharedPreferences sp = context.getSharedPreferences("MySetting", MODE_PRIVATE);
        token = sp.getString("token", "-1");
        id = sp.getString("user_id_current", "-1");
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.request_friend, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.request_tv_friend);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.request_img_profile);
        ImageView img_addFriend = (ImageView) rowView.findViewById(R.id.request_add_friend);


        img_addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BackgoundWorker((HttpRequestCallback) ctx).execute("addfriend", requestFriendInfos.get(position).userName,id, token);
                requestFriendInfos.remove(position);
                updateRequestList(requestFriendInfos);
            }
        });


        textView.setText(requestFriendInfos.get(position).userName);
        imageView.setImageResource(R.drawable.person);
        return rowView;
    }

    private void updateRequestList(List<RequestFriendInfo> newlist) {
        requestFriendInfos = newlist;
        this.notifyDataSetChanged();
    }

}
