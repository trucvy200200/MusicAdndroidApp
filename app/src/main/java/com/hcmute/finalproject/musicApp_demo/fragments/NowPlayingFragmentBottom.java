package com.hcmute.finalproject.musicApp_demo.fragments;

import static android.content.Context.MODE_PRIVATE;

import static com.hcmute.finalproject.musicApp_demo.SongActivity.ARTIST_TO_FRAG;
import static com.hcmute.finalproject.musicApp_demo.SongActivity.PATH_TO_FRAG;
import static com.hcmute.finalproject.musicApp_demo.SongActivity.SHOW_MINI_PLAYER;
import static com.hcmute.finalproject.musicApp_demo.SongActivity.SONG_NAME_TO_FRAG;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hcmute.finalproject.musicApp_demo.MusicService;
import com.hcmute.finalproject.musicApp_demo.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class NowPlayingFragmentBottom extends Fragment implements ServiceConnection {

    ImageView nextBtn,albumArt, prevBtn;
    TextView artist,songName;
    FloatingActionButton playPauseBtn;
    View view;
    MusicService musicService;
    public  static  final String MUSIC_LAST_PLAYED="LAST_PLAYED";
    public static final String MUSIC_FILE="STORED_MUSIC";
    public static final String ARTIST_NAME="ARTIST NAME";
    public static final String SONG_NAME="SONG NAME";
    public NowPlayingFragmentBottom() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view =inflater.inflate(R.layout.fragment_now_playing_bottom,container,false);
        artist=view.findViewById(R.id.song_artist_miniPlayer);
        songName=view.findViewById(R.id.song_name_miniPlayer);
        albumArt=view.findViewById(R.id.bottom_album_art);
        nextBtn=view.findViewById(R.id.skip_next_bottom);
        playPauseBtn=view.findViewById(R.id.play_pause_miniPlayer);
        prevBtn=view.findViewById(R.id.skip_prev_bottom);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(musicService!=null)
                {
                    musicService.nextBtnClicked();
                    if(getActivity()!=null)
                    {
                        SharedPreferences.Editor editor=getActivity().getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE).edit();
                        editor.putString(MUSIC_FILE,musicService.musicFiles.get(musicService.position).getPath());
                        //editor.apply();
                        editor.putString(ARTIST_NAME,musicService.musicFiles.get(musicService.position).getArtist());
                        // editor.apply();
                        editor.putString(SONG_NAME,musicService.musicFiles.get(musicService.position).getTitle());
                        editor.apply();
                        SharedPreferences preferences=getActivity()
                                .getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE);
                        String path=preferences.getString(MUSIC_FILE,null);
                        String artistName =preferences.getString(ARTIST_NAME,null);
                        String song_name =preferences.getString(SONG_NAME,null);
                        if(path!=null)
                        {
                            SHOW_MINI_PLAYER=true;
                            PATH_TO_FRAG=path;
                            ARTIST_TO_FRAG=artistName;
                            SONG_NAME_TO_FRAG=song_name;
                        }
                        else {
                            SHOW_MINI_PLAYER=false;
                            PATH_TO_FRAG=null;
                            ARTIST_TO_FRAG=null;
                            SONG_NAME_TO_FRAG=null;
                        }
                        if(SHOW_MINI_PLAYER)
                        {
                            if(PATH_TO_FRAG!=null)
                            {
                                byte[] art=getAlbumArt(PATH_TO_FRAG);
                                if(art!=null)
                                {
                                    Glide.with(getContext()).load(art).into(albumArt);
                                }else
                                {
                                    Glide.with(getContext()).load(R.drawable.ic_default).into(albumArt);
                                }

                                songName.setText(SONG_NAME_TO_FRAG);
                                artist.setText(ARTIST_TO_FRAG);

                            }

                        }

                    }

                }
            }
        });
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(musicService!=null)
                {
                    musicService.previousBtnClicked();
                    if(getActivity()!=null)
                    {
                        SharedPreferences.Editor editor=getActivity().getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE).edit();
                        editor.putString(MUSIC_FILE,musicService.musicFiles.get(musicService.position).getPath());
                        //editor.apply();
                        editor.putString(ARTIST_NAME,musicService.musicFiles.get(musicService.position).getArtist());
                        // editor.apply();
                        editor.putString(SONG_NAME,musicService.musicFiles.get(musicService.position).getTitle());
                        editor.apply();
                        SharedPreferences preferences=getActivity()
                                .getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE);
                        String path=preferences.getString(MUSIC_FILE,null);
                        String artistName =preferences.getString(ARTIST_NAME,null);
                        String song_name =preferences.getString(SONG_NAME,null);

                        if(path!=null)
                        {
                            SHOW_MINI_PLAYER=true;
                            PATH_TO_FRAG=path;
                            ARTIST_TO_FRAG=artistName;
                            SONG_NAME_TO_FRAG=song_name;

                        }
                        else {
                            SHOW_MINI_PLAYER=false;
                            PATH_TO_FRAG=null;
                            ARTIST_TO_FRAG=null;
                            SONG_NAME_TO_FRAG=null;
                        }
                        if(SHOW_MINI_PLAYER)
                        {
                            if(PATH_TO_FRAG!=null)
                            {
                                byte[] art=getAlbumArt(PATH_TO_FRAG);
                                if(art!=null)
                                {
                                    Glide.with(getContext()).load(art).into(albumArt);
                                }else
                                {
                                    Glide.with(getContext()).load(R.drawable.ic_default).into(albumArt);
                                }

                                songName.setText(SONG_NAME_TO_FRAG);
                                artist.setText(ARTIST_TO_FRAG);

                            }

                        }

                    }

                }
            }
        });
        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(musicService!=null)
                {
                    musicService.playPauseBtnClicked();
                    if(musicService.isPlaying())
                    {
                        playPauseBtn.setImageResource(R.drawable.ic_pause);
                    }
                    else
                    {
                        playPauseBtn.setImageResource(R.drawable.ic_play);
                    }
                }
            }
        });
        return  view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(SHOW_MINI_PLAYER)
        {
            if(PATH_TO_FRAG!=null)
            {
                byte[] art=getAlbumArt(PATH_TO_FRAG);
                if(art!=null)
                {
                    Glide.with(getContext()).load(art).into(albumArt);
                }else
                {
                    Glide.with(getContext()).load(R.drawable.ic_default).into(albumArt);
                }
                if(musicService !=null )
                {
                    if ( musicService.isPlaying()){
                        playPauseBtn.setImageResource(R.drawable.ic_pause);
                    }
                    else
                    {
                        playPauseBtn.setImageResource(R.drawable.ic_play);
                    }
                }

                songName.setText(SONG_NAME_TO_FRAG);
                artist.setText(ARTIST_TO_FRAG);
                Intent intent=new Intent(getContext(), MusicService.class);
                if(getContext()!=null)
                {
                    getContext().bindService(intent,this, Context.BIND_AUTO_CREATE);
                }
            }

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(getContext()!=null)
        {
            getContext().unbindService(this);
        }
    }

    private  byte[]getAlbumArt(String uri) {
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art=retriever.getEmbeddedPicture();

        return art;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        MusicService.MyBinder binder=(MusicService.MyBinder)service;
        musicService =binder.getService();

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService=null;

    }
}