package com.ArtGo.ArtGoApp.utils;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by MoaoranLi on 2016/9/2.
 */

public class MyItem implements ClusterItem {
    private String id;
    private final LatLng mPosition;
    private BitmapDescriptor icon;
    private String snippet;
    private String title;

    public MyItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    public MyItem(String id, LatLng mPosition, String title, String snippet, BitmapDescriptor icon) {
        this.id =id;
        this.mPosition = mPosition;
        this.title = title;
        this.snippet = snippet;
        this.icon = icon;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public BitmapDescriptor getIcon() {
        return icon;
    }

    public void setIcon(BitmapDescriptor icon) {
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
