package com.hcmute.finalproject.musicApp_demo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.hcmute.finalproject.musicApp_demo.model.Music;

import java.util.ArrayList;

public class SongActivity extends AppCompatActivity {
    public static ArrayList<Music> songs=new ArrayList<>();
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    ViewPagerAdapter viewPagerAdapter;
    static boolean shuffleBoolean=false, repeatBoolean=false;
    private static final int MY_PERMISSION_REQUEST=1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        permission();
        initViewPager();
    }
    private void initViewPager(){
        tabLayout=findViewById(R.id.tab_layout);
        viewPager2=findViewById(R.id.view_pager);
        viewPagerAdapter=new ViewPagerAdapter(this);
        viewPager2.setAdapter(viewPagerAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });;
    }
    private void permission(){
        if (ContextCompat.checkSelfPermission(SongActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(SongActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
            else{
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                getMusic();
            }
    }
    private void getMusic() {

        ContentResolver contentResolver=getContentResolver();
        Uri songUri=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection={
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
        };
        String selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?";

        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3");

        String[] selectionArgsMp3 = new String[]{ mimeType };
        Cursor songCursor=contentResolver.query(songUri,projection,selectionMimeType,selectionArgsMp3, null);
        if (songCursor!=null && songCursor.moveToFirst()) {
            songs= new ArrayList<>();
            int songTitle=songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist=songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songDuraion=songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int songAlbum=songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int songPath=songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            do{
                String album=songCursor.getString(songAlbum);
                String title=songCursor.getString(songTitle);
                String artist=songCursor.getString(songArtist);
                int duration=songCursor.getInt(songDuraion);
                String path=songCursor.getString(songPath);

                Music song=new Music(path,title,duration,artist,album);
                songs.add(song);
            }while (songCursor.moveToNext());
            Toast.makeText(getApplicationContext(),"Number songs: "+songs.size(), Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==MY_PERMISSION_REQUEST) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(SongActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ActivityCompat.requestPermissions(SongActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);

                }

            }
        }
    }

