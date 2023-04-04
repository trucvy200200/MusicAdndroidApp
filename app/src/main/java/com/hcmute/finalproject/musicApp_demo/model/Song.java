package com.hcmute.finalproject.musicApp_demo.model;
import java.util.concurrent.TimeUnit;

public class Song {

    private String songName,songUrl;
    private String imageUrl, songArtist, songDuration;

    public Song() {
    }

    public Song(String songName, String songUrl, String imageUrl, String songArtist, String songDuration) {
        this.songName = songName;
        this.songUrl = songUrl;
        this.imageUrl = imageUrl;
        this.songArtist = songArtist;
        this.songDuration = castDuration(songDuration);
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
    }

    public String getSongDuration() {
        return songDuration;
    }

    public void setSongDuration(String songDuration) {
        this.songDuration = songDuration;
    }

    public String castDuration(String songDuration) {
        Long duration = Long.parseLong(songDuration);
        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(duration);
        int totalSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(duration);
        int seconds = totalSeconds-(minutes*60);
        return minutes + ":" + seconds;
    }

}
