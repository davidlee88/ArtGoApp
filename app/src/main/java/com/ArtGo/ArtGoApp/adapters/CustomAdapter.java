package com.ArtGo.ArtGoApp.adapters;

/**
 * Created by MoaoranLi on 2016/10/3.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ArtGo.ArtGoApp.R;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    Context context;
    ArrayList<Integer> images;
    ArrayList<String> catagoryNames;
    LayoutInflater inflater;

    public CustomAdapter(Context applicationContext,ArrayList<Integer> images, ArrayList<String> catagoryNames) {
        this.context = applicationContext;
        this.images = images;
        this.catagoryNames = catagoryNames;
        inflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.layout_spinner, null);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView names = (TextView) view.findViewById(R.id.spinnerTarget);
        icon.setImageResource(images.get(i));
        names.setText(catagoryNames.get(i));
        return view;
    }
}