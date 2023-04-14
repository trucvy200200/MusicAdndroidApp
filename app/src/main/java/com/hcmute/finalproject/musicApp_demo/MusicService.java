package com.hcmute.finalproject.musicApp_demo;

import static com.hcmute.finalproject.musicApp_demo.ApplicationClass.ACTION_NEXT;
import static com.hcmute.finalproject.musicApp_demo.ApplicationClass.ACTION_PLAY;
import static com.hcmute.finalproject.musicApp_demo.ApplicationClass.ACTION_PREVIOUS;
import static com.hcmute.finalproject.musicApp_demo.ApplicationClass.CHANNEL_ID_2;
import static com.hcmute.finalproject.musicApp_demo.PlayerActivity.listSongs;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.hcmute.finalproject.musicApp_demo.model.Music;


import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    MyBinder mBinder=new MyBinder();
    MediaPlayer mediaPlayer;
    public ArrayList<Music> musicFiles=new ArrayList<>();
    Uri uri;
    public int position=-1;
    MediaSessionCompat mediaSessionCompat;
    ActionPlaying actionPlaying;
    public  static  final String MUSIC_LAST_PLAYED="LAST_PLAYED";
    public static final String MUSIC_FILE="STORED_MUSIC";
    public static final String ARTIST_NAME="ARTIST NAME";
    public static final String SONG_NAME="SONG NAME";

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat=new MediaSessionCompat(getBaseContext(),"My Audio");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Bind", "Method");
        return mBinder;
    }


    public class MyBinder extends Binder {
        public MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition", -1);
        String actionName=intent.getStringExtra("ActionName");
        if(myPosition!=-1){
            playMedia(myPosition);
        }
        if (actionName!=null)
        {
            switch(actionName)
            {
                case "playPause":
                    //  Toast.makeText(this,"PlayPause",Toast.LENGTH_SHORT).show();
                    playPauseBtnClicked();
                    break;
                case "next":
                    //Toast.makeText(this,"Next",Toast.LENGTH_SHORT).show();
                    nextBtnClicked();
                    break;
                case "previous":
                    //Toast.makeText(this,"Previous",Toast.LENGTH_SHORT).show();
                    previousBtnClicked();
                    break;

            }
        }
        return START_STICKY;
    }

    private void playMedia(int StartPosition) {
        musicFiles=listSongs;
        position=StartPosition;
        if(mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            if(musicFiles!=null)
            {
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }
        else
        {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    void start(){
        mediaPlayer.start();
    }
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    void stop(){
        mediaPlayer.stop();
    }
    void release(){
        mediaPlayer.release();
    }

    int getDuration(){
        return mediaPlayer.getDuration();
    }
    void seekTo(int position){
        mediaPlayer.seekTo(position);
    }
    int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }
    void createMediaPlayer(int positionInner){
        position=positionInner;
        uri=Uri.parse(musicFiles.get(position).getPath());
        SharedPreferences.Editor editor=getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE,uri.toString());
        //editor.apply();
        editor.putString(ARTIST_NAME,musicFiles.get(position).getArtist());
        // editor.apply();
        editor.putString(SONG_NAME,musicFiles.get(position).getTitle());
        editor.apply();
        mediaPlayer=MediaPlayer.create(getBaseContext(),uri);
    }
    void pause(){
        mediaPlayer.pause();
    }
    void OnCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (actionPlaying!=null){
            actionPlaying.nextBtnClicked();

        }
        if(mediaPlayer!=null)
        {
            createMediaPlayer(position);
            mediaPlayer.start();
            OnCompleted();
        }
    }
    void setCallBack(ActionPlaying actionPlaying)
    {
        this.actionPlaying=actionPlaying;
    }
    public void playPauseBtnClicked()
    {
        if(actionPlaying!=null)
        {
            actionPlaying.playPauseBtnClick();
        }
    }
    void previousBtnClicked()
    {
        if(actionPlaying!=null)
        {
            actionPlaying.prevBtnClicked();
        }
    }
    public void nextBtnClicked() {
        if(actionPlaying!=null)
        {
            actionPlaying.nextBtnClicked();
        }
    }
    private byte[] getAlbumArt(String uri) throws IOException {
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        Log.d("Check Uri", uri);
        retriever.setDataSource(uri);
        byte[] art=retriever.getEmbeddedPicture();
        return art;
    }
    void showNotification(int playPauseBtn){

        Intent intent=new Intent(this,PlayerActivity.class);
        PendingIntent contentIntent=PendingIntent.getActivity(this,0,intent,0);
       //PendingIntent contentIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_MUTABLE);
        Intent preIntent=new Intent(this,NotificationReceiver.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending=PendingIntent.getBroadcast(this,0,preIntent,PendingIntent.FLAG_UPDATE_CURRENT);
       // PendingIntent prevPending=PendingIntent.getBroadcast(this,0,preIntent,PendingIntent.FLAG_MUTABLE);
        Intent pauseIntent=new Intent(this,NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent pausePending=PendingIntent.getBroadcast(this,0,pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent pausePending=PendingIntent.getBroadcast(this,0,pauseIntent,PendingIntent.FLAG_MUTABLE);
        Intent nextIntent=new Intent(this,NotificationReceiver.class).setAction(ACTION_NEXT);
         PendingIntent nextPending=PendingIntent.getBroadcast(this,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT);
       // PendingIntent nextPending=PendingIntent.getBroadcast(this,0,nextIntent,PendingIntent.FLAG_MUTABLE);
        byte[] picture=null;
        try {
            picture= getAlbumArt(musicFiles.get(position).getPath());
        } catch (IOException e) {

        }
        Bitmap thumb=null;
        if(picture!=null)
        {
            thumb= BitmapFactory.decodeByteArray(picture,0,picture.length);

        }else
        {
            thumb=BitmapFactory.decodeResource(getResources(),R.drawable.ic_default);

        }
        Notification notification=new NotificationCompat.Builder(this,CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_skippre,"Previous",prevPending)
                .addAction(playPauseBtn,"Pause",pausePending)
                .addAction(R.drawable.ic_skipnext,"Next",nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        startForeground(1,notification);/////////
    }
}
