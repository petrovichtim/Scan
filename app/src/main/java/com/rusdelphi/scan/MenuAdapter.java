package com.rusdelphi.scan;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class MenuAdapter extends BaseAdapter {
    private final int layoutId;
    private final String[] text;
    private final Drawable[] icons;
    public MenuAdapter(Context c, int layoutId, int textsResId, int iconsResId) {
        text = c.getResources().getStringArray(textsResId);
        this.layoutId = layoutId;
        TypedArray ta = c.getResources().obtainTypedArray(iconsResId);
        icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            icons[i] = ta.getDrawable(i);
        }
        ta.recycle();
    }

    @Override
    public int getCount() {
        return text.length;
    }

    @Override
    public Object getItem(int position) {
        return text[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        }
        ((TextView) view.findViewById(android.R.id.text1)).setText(text[position]);
        ((ImageView) view.findViewById(android.R.id.icon1)).setImageDrawable(icons[position]);
        return view;
    }
}
