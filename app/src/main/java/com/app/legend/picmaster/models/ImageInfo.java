package com.app.legend.picmaster.models;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.io.Serializable;

public class ImageInfo implements Serializable,Comparable{

    private String path;
    private String name;
    private int id;
    private int sel=-1;
    private String longs;

    public String getLongs() {
        return longs;
    }

    public void setLongs(String longs) {
        this.longs = longs;
    }

    public int getSel() {
        return sel;
    }

    public void setSel(int sel) {
        this.sel = sel;
    }

    public ImageInfo() {
    }

    public String getPath() {

        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(@NonNull Object o) {


//        return this.getOrder().compareTo(arg0.getOrder());
        return 0;
    }
}
