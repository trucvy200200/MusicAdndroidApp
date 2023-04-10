package com.hcmute.finalproject.musicApp_demo.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by jun on 12/29/17.
 */

public class Music implements Serializable {
    private String path;
    private String title;
    private int duration;
    private String artist;
    private String album;
    private String image = null;

    public Music() {

    }

    public Music(String path, String title, int duration, String artist, String album) {
        this.path = path;
        this.title = title;
        this.duration = duration;
        this.artist = artist;
        this.album = album;
    }

    public Music(String path, String title, int duration, String artist, String album, String image) {
        this.path = path;
        this.title = title;
        this.duration = duration;
        this.artist = artist;
        this.album = album;
        this.image = image;
    }

    public String getPath() {
        return path;
    }

    public String getTitle() {
        return title;
    }

    public int getDuration() {
        return duration;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }
}
