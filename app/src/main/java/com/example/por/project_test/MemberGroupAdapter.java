package com.example.por.project_test;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Por on 9/18/2016.
 */
class MemberGroupAdapter extends ArrayAdapter<MemberGroupInfo> {

    private final Context ctx;
    List<MemberGroupInfo> value;

    MemberGroupAdapter(Context ctx, int resource, List<MemberGroupInfo> value) {
        super(ctx, resource, value);//สร้างarrayเปล่าที่มีขนาดเท่ากับlist ในadapterมีทั้งหมดกี่บรรทัด
        this.ctx = ctx;
        this.value = value;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.membergroup, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.tv__membergroup);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img_membergroup);

        textView.setText(value.get(position).username);
        imageView.setImageResource(R.drawable.person);

        //textView.setText([position]);
        // Change icon based on name
        //String s = values[position];
        //System.out.println(s);
        return rowView;

    }

    @Nullable
    @Override
    public MemberGroupInfo getItem(int position) {
        return value.get(position);
    }


}
