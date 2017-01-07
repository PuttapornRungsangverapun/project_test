package com.example.por.project_test;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Por on 9/18/2016.
 */
public class InviteGroupAdapter extends ArrayAdapter<InviteGroupInfo> implements CompoundButton.OnCheckedChangeListener {
    SparseBooleanArray mCheckStates;
    private final Context ctx;
    List<InviteGroupInfo> value;

    public InviteGroupAdapter(Context ctx, int resource, List<InviteGroupInfo> value) {
        super(ctx, resource, value);//สร้างarrayเปล่าที่มีขนาดเท่ากับlist ในadapterมีทั้งหมดกี่บรรทัด
        this.ctx = ctx;
        this.value = value;
        mCheckStates = new SparseBooleanArray(value.size());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.invitegroup, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.tv_invitegroup_friend);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img_invitegroup_profile);
        CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.chk_invitegroup);

        textView.setText(value.get(position).username);
        imageView.setImageResource(R.drawable.person);
        checkBox.setTag(position);
        checkBox.setChecked(mCheckStates.get(position, false));
        checkBox.setOnCheckedChangeListener(this);

        //textView.setText([position]);
        // Change icon based on name
        //String s = values[position];
        //System.out.println(s);
        return rowView;

    }

    @Nullable
    @Override
    public InviteGroupInfo getItem(int position) {
        return value.get(position);
    }

    public boolean isChecked(int position) {
        return mCheckStates.get(position, false);
    }

    public void setChecked(int position, boolean isChecked) {
        mCheckStates.put(position, isChecked);

    }

    public void toggle(int position) {
        setChecked(position, !isChecked(position));

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mCheckStates.put((Integer) buttonView.getTag(), isChecked);
    }
}
