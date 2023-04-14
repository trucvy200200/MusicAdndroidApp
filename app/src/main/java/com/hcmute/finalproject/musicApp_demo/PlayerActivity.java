package com.hcmute.finalproject.musicApp_demo;

import static com.hcmute.finalproject.musicApp_demo.ApplicationClass.ACTION_NEXT;
import static com.hcmute.finalproject.musicApp_demo.ApplicationClass.ACTION_PLAY;
import static com.hcmute.finalproject.musicApp_demo.ApplicationClass.ACTION_PREVIOUS;
import static com.hcmute.finalproject.musicApp_demo.ApplicationClass.CHANNEL_ID_2;
import static com.hcmute.finalproject.musicApp_demo.SongActivity.repeatBoolean;
import static com.hcmute.finalproject.musicApp_demo.SongActivity.shuffleBoolean;
import static com.hcmute.finalproject.musicApp_demo.SongActivity.songs;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hcmute.finalproject.musicApp_demo.fragments.NowPlayingFragmentBottom;
import com.hcmute.finalproject.musicApp_demo.model.Music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity implements  ActionPlaying, ServiceConnection{
    TextView song_name, artist_name, duration_played, duration_total, album;
    ImageView cover_art, nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn;
    SeekBar seekBar;
    FloatingActionButton playPauseBtn;
    int position=-1;

    static ArrayList<Music> listSongs;
    static Uri uri;
   // static MediaPlayer mediaPlayer;
    private Handler handler=new Handler();
    private Thread playThread,prevThread, nextThread;
    MusicService musicService;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
//                    Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT)
//                            .show();
                } else {
                }
            });
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.play_music);
        askNotificationPermission();
//        getSupportActionBar().hide();
        initViews();
        getIntentMethod();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService!=null && fromUser){
                    musicService.seekTo(progress*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService!=null){
                    int mCurrentPosition=musicService.getCurrentPosition()/1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));

                }
                handler.postDelayed(this,1000);
            }
        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shuffleBoolean){
                    shuffleBoolean=false;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle);
                }
                else{
                    shuffleBoolean=true;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (repeatBoolean){
                    repeatBoolean=false;
                    repeatBtn.setImageResource(R.drawable.ic_repeat);
                }
                else{
                    repeatBoolean=true;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_on);
                }
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               onBackPressed();
            }
        });
    }


    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    @Override
    protected void onResume() {
        Intent intent=new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private void nextThreadBtn() {
        nextThread=new Thread(){
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    public void nextBtnClicked() {
        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();

            if (shuffleBoolean && !repeatBoolean){
                position=getRandom(listSongs.size()-1);
            }
            else if (!shuffleBoolean && !repeatBoolean){
                position=((position+1)%listSongs.size());
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            album.setText(listSongs.get(position).getAlbum());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService!=null){
                        int mCurrentPosition=musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_pause);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            musicService.start();
        }
        else{
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !repeatBoolean){
                playPauseBtn.setImageResource(R.drawable.ic_pause);
                position=getRandom(listSongs.size()-1);
            }
            else if (!shuffleBoolean && !repeatBoolean){
                position=((position+1)%listSongs.size());
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            album.setText(listSongs.get(position).getAlbum());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService!=null){
                        int mCurrentPosition=musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_play);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);
        }
    }

    private int getRandom(int i) {
        Random random=new Random();
        return random.nextInt(i+1);
    }

    private void prevThreadBtn() {
        prevThread=new Thread(){
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    public void prevBtnClicked() {
        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();
            position = ((position - 1)<0 ? (listSongs.size()-1):(position-1));
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            album.setText(listSongs.get(position).getAlbum());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService!=null){
                        int mCurrentPosition=musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_pause);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            musicService.start();
        }
        else{
            musicService.stop();
            musicService.release();
            position = ((position - 1)<0 ? (listSongs.size()-1):(position-1));
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            album.setText(listSongs.get(position).getAlbum());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService!=null){
                        int mCurrentPosition=musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_play);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);
        }
    }

    private void playThreadBtn() {
        playThread=new Thread(){
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClick();
                    }
                });
            }
        };
        playThread.start();
    }

    public void playPauseBtnClick() {
        if (musicService.isPlaying()){
            playPauseBtn.setImageResource(R.drawable.ic_play);
            musicService.showNotification(R.drawable.ic_play);
            musicService.pause();
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService!=null){
                        int mCurrentPosition=musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });

        }
        else{
            musicService.showNotification(R.drawable.ic_pause);
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            musicService.start();
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService!=null){
                        int mCurrentPosition=musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }
    }

    private String formattedTime(int mCurrentPosition) {
        String totalout="";
        String totalNew="";
        String seconds=String.valueOf(mCurrentPosition%60);
        String minutes=String.valueOf(mCurrentPosition/60);
        totalout=minutes+":"+seconds;
        totalNew=minutes+":"+"0"+seconds;
        if (seconds.length()==1){
            return totalNew;
        }
        else{
            return totalout;
        }
    }

    private void getIntentMethod() {
        position=getIntent().getIntExtra("position", -1);
        listSongs=songs;
        if (listSongs!=null){
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            uri=Uri.parse(listSongs.get(position).getPath());
        }

        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("servicePosition", position);
        startService(intent);

    }

    private void initViews() {
        song_name=findViewById(R.id.detail_track_title_field);
        song_name.setSelected(true);
        artist_name=findViewById(R.id.detail_artist_field);
        duration_played=findViewById(R.id.textview_SongTime);
        duration_total=findViewById(R.id.textview_TotalTime);
        cover_art=findViewById(R.id.imageview_circle);
        nextBtn=findViewById(R.id.imgaebutton_next);
        prevBtn=findViewById(R.id.imgaebutton_pre);
        backBtn=findViewById(R.id.imgbutton_back);
        shuffleBtn=findViewById(R.id.imgaebutton_shuffle);
        seekBar=findViewById(R.id.seekbarSong);
        playPauseBtn=findViewById(R.id.play_pause);
        album=findViewById(R.id.detail_album_field);
        album.setSelected(true);
        repeatBtn=findViewById(R.id.imgaebutton_repeat);
    }

    private void metaData(Uri uri){
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal=listSongs.get(position).getDuration()/1000;
        duration_total.setText(formattedTime(durationTotal));
        byte[] art=retriever.getEmbeddedPicture();
        if (art!=null){
            Glide.with(getApplicationContext()).asBitmap().load(art).into(cover_art);
        }
        else{
            Glide.with(getApplicationContext()).asBitmap().load(R.drawable.ic_default).into(cover_art);
        }
    }



    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder= (MusicService.MyBinder) service;
        musicService=myBinder.getService();
        musicService.setCallBack(this);
        Toast.makeText(this,"Connected"+musicService,Toast.LENGTH_SHORT).show();
        seekBar.setMax(musicService.getDuration()/1000);
        metaData(uri);

        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        album.setText(listSongs.get(position).getAlbum());
        musicService.OnCompleted();
        musicService.showNotification(R.drawable.ic_pause);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService=null;
    }
    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // Your app can post notifications.
                return;
            } else{
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // ask WRITE_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            // Your app can post notifications.
            requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        // ask READ_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

    }

}
