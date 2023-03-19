package com.hcmute.finalproject.musicApp_demo.manager;

import android.os.Parcelable;

import com.hcmute.finalproject.musicApp_demo.model.Music;

/**
 * Created by jun on 12/31/17.
 */

public class PlaybackManager {

    private static PlaybackManager manager;

    private Music music;
    private Parcelable state;
    private boolean SearchResultFragmentAdded = false;

    public static PlaybackManager getInstance(){
        if(manager == null){
            manager = new PlaybackManager();
        }

        return manager;
    }

    public Music getMusic() {
        return music;
    }

    public void setMusic(Music music) {
        this.music = music;
    }

    public Parcelable getState() {
        return state;
    }

    public void setState(Parcelable state) {
        this.state = state;
    }

    public boolean isSearchResultFragmentAdded() {
        return SearchResultFragmentAdded;
    }

    public void setSearchResultFragmentAdded(boolean searchResultFragmentAdded) {
        SearchResultFragmentAdded = searchResultFragmentAdded;
    }
}
