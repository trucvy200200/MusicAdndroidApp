package com.hcmute.finalproject.musicApp_demo;

import static com.hcmute.finalproject.musicApp_demo.SongActivity.repeatBoolean;
import static com.hcmute.finalproject.musicApp_demo.SongActivity.shuffleBoolean;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
//import com.google.android.exoplayer2.ExoPlayer;
//import com.google.android.exoplayer2.MediaItem;
//import com.google.android.exoplayer2.source.MediaSource;
//import com.google.android.exoplayer2.source.ProgressiveMediaSource;
//import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hcmute.finalproject.musicApp_demo.Service.MyDownloadService;
import com.hcmute.finalproject.musicApp_demo.databinding.PlayMusicBinding;
import com.hcmute.finalproject.musicApp_demo.model.Music;

import java.util.ArrayList;
import java.util.Random;

public class PlayerStreamActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    TextView song_name, artist_name, duration_played, duration_total, album;
    ImageView cover_art, nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn;
    ImageButton downloadBtn;
    StorageReference storageReference;

//    Context context;
    SeekBar seekBar;
    FloatingActionButton playPauseBtn;
    PlayMusicBinding binding;
    int position=-1;

    static ArrayList<Music> listSongs;
    static Music song;
    static Uri uri;
    static MediaPlayer mediaPlayer;
//    static ExoPlayer exoPlayer;
    private final Handler handler=new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestPermission();
        binding = PlayMusicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // init db
        storageReference = FirebaseStorage.getInstance().getReference();


        initViews();
        getIntentMethod();
//        song_name.setText(listSongs.get(position).getTitle());
//        artist_name.setText(listSongs.get(position).getArtist());
//        album.setText(listSongs.get(position).getAlbum());

        song_name.setText(song.getTitle());
        artist_name.setText(song.getArtist());
        album.setText(song.getAlbum());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer!=null && fromUser){
                    mediaPlayer.seekTo(progress*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerStreamActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer!=null){
                    int mCurrentPosition=mediaPlayer.getCurrentPosition()/1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this,1000);
            }
        });
        shuffleBtn.setOnClickListener(v -> {
            if (shuffleBoolean){
                shuffleBoolean=false;
                shuffleBtn.setImageResource(R.drawable.ic_shuffle);
            }
            else{
                shuffleBoolean=true;
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
            }
        });
        repeatBtn.setOnClickListener(v -> {
            if (repeatBoolean){
                repeatBoolean=false;
                repeatBtn.setImageResource(R.drawable.ic_repeat);
            }
            else{
                repeatBoolean=true;
                repeatBtn.setImageResource(R.drawable.ic_repeat_on);
            }
        });
        backBtn.setOnClickListener(v -> onBackPressed());

        downloadBtn.setOnClickListener(v -> download());

    }

    @Override
    protected void onResume() {
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();
    }

    private void nextThreadBtn() {
        Thread nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(v -> nextBtnClicked());
            }
        };
        nextThread.start();
    }

    private void nextBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (shuffleBoolean && !repeatBoolean){
                position=getRandom(listSongs.size()-1);
            }
            else if (!shuffleBoolean && !repeatBoolean){
                position=((position+1)%listSongs.size());
            }
            uri = Uri.parse(listSongs.get(position).getPath());

            Log.e("TAG", "nextBtnClicked: "+uri);

            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            album.setText(listSongs.get(position).getAlbum());
            seekBar.setMax(mediaPlayer.getDuration()/1000);
            PlayerStreamActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer!=null){
                        int mCurrentPosition=mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            mediaPlayer.start();
        }
        else{
            mediaPlayer.stop();
            mediaPlayer.release();
            if (shuffleBoolean && !repeatBoolean){
                playPauseBtn.setImageResource(R.drawable.ic_pause);
                position=getRandom(listSongs.size()-1);
            }
            else if (!shuffleBoolean && !repeatBoolean){
                position=((position+1)%listSongs.size());
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            album.setText(listSongs.get(position).getAlbum());
            seekBar.setMax(mediaPlayer.getDuration()/1000);
            PlayerStreamActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer!=null){
                        int mCurrentPosition=mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);
        }
    }

    private int getRandom(int i) {
        Random random=new Random();
        return random.nextInt(i+1);
    }

    private void prevThreadBtn() {
        Thread prevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(v -> prevBtnClicked());
            }
        };
        prevThread.start();
    }

    private void prevBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (shuffleBoolean && !repeatBoolean){
                position=getRandom(listSongs.size()-1);
            }
            else if (!shuffleBoolean && !repeatBoolean){
                position = ((position - 1)<0 ? (listSongs.size()-1):(position-1));
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            album.setText(listSongs.get(position).getAlbum());
            seekBar.setMax(mediaPlayer.getDuration()/1000);
            PlayerStreamActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer!=null){
                        int mCurrentPosition=mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            mediaPlayer.start();
        }
        else{
            mediaPlayer.stop();
            mediaPlayer.release();
            if (shuffleBoolean && !repeatBoolean){
                position=getRandom(listSongs.size()-1);
            }
            else if (!shuffleBoolean && !repeatBoolean){
                position = ((position - 1)<0 ? (listSongs.size()-1):(position-1));
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            album.setText(listSongs.get(position).getAlbum());
            seekBar.setMax(mediaPlayer.getDuration()/1000);
            PlayerStreamActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer!=null){
                        int mCurrentPosition=mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);
        }
    }

    private void playThreadBtn() {
        Thread playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(v -> playPauseBtnClick());
            }
        };
        playThread.start();
    }

    public void playPauseBtnClick() {
        if (mediaPlayer.isPlaying()){
            playPauseBtn.setImageResource(R.drawable.ic_play);
            mediaPlayer.pause();
            seekBar.setMax(mediaPlayer.getDuration()/1000);
            PlayerStreamActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer!=null){
                        int mCurrentPosition=mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });

        }
        else{
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration()/1000);
            PlayerStreamActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer!=null){
                        int mCurrentPosition=mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }
    }

    public void download() {
//        SongActivity activity = (SongActivity) getApplicationContext();

        Intent intent = new Intent(this, MyDownloadService.class);
        intent.putExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH, listSongs.get(position).getPath());
        intent.setAction(MyDownloadService.ACTION_DOWNLOAD);
        this.startService(intent);
    }

    private String formattedTime(int mCurrentPosition) {
        String totalout;
        String totalNew;
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
//        listSongs = getIntent().getIntExtra("songs")
        listSongs = (ArrayList<Music>) getIntent().getSerializableExtra("songs");
        song = (Music) getIntent().getSerializableExtra("song");

        if (song == null) {
            song = listSongs.get(position);
        }
        else {
            Log.e("SONG", "song is not null");
        }

        if (song != null){
            Log.e("song", song.getTitle());
            playPauseBtn.setImageResource(R.drawable.ic_pause);

            // get download url from firebase storage
            StorageReference ref = storageReference.child(song.getPath());
            ref.getDownloadUrl().addOnSuccessListener(downloadURI -> {
                uri = downloadURI;
                Log.e("downloadURI", downloadURI.toString());
                if (mediaPlayer!=null)
                {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
                    mediaPlayer.start();
                }
                else {
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                    mediaPlayer.start();
                }
                mediaPlayer.setOnCompletionListener(this);
                seekBar.setMax(mediaPlayer.getDuration()/1000);
                metaData(uri);

            }).addOnFailureListener(e -> {
                Log.e("Download URI", e.getMessage());
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void initViews() {

        song_name = binding.detailTrackTitleField;
        song_name.setSelected(true);
        artist_name = binding.detailArtistField;
        duration_played = binding.textviewSongTime;
        duration_total = binding.textviewTotalTime;
        cover_art = binding.imageviewCircle;
        nextBtn = binding.imgaebuttonNext;
        prevBtn = binding.imgaebuttonPre;
        backBtn = binding.imgbuttonBack;
        shuffleBtn = binding.imgaebuttonShuffle;
        seekBar = binding.seekbarSong;
        playPauseBtn = binding.playPause;
        album = binding.detailAlbumField;
        album.setSelected(true);
        repeatBtn = binding.imgaebuttonRepeat;
        downloadBtn = binding.downloadBtn;
        downloadBtn.setVisibility(View.VISIBLE);
    }

    private void metaData(Uri uri){
//        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
//        retriever.setDataSource(uri.toString());
        int durationTotal = song.getDuration()/1000;
        duration_total.setText(formattedTime(durationTotal));
//        byte[] art=retriever.getEmbeddedPicture();
        byte[] art = null;
        if (art!=null){
            Glide.with(this).asBitmap().load(art).into(cover_art);
        }
        else{
            Glide.with(this).asBitmap().load(R.drawable.ic_default).into(cover_art);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        nextBtnClicked();
        if (mediaPlayer!=null){

            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
        }
    }

    private void requestPermission() {
        // ask WRITE_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            // Your app can post notifications.
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // ask READ_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
    }
}