package com.hcmute.finalproject.musicApp_demo;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hcmute.finalproject.musicApp_demo.Service.MyDownloadService;
import com.hcmute.finalproject.musicApp_demo.Service.MyUploadService;
import com.hcmute.finalproject.musicApp_demo.databinding.FragmentMainBinding;
import com.hcmute.finalproject.musicApp_demo.model.Music;

import java.util.ArrayList;
import java.util.Locale;

public class SongActivity extends AppCompatActivity {
    public static ArrayList<Music> songs=new ArrayList<>();
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    ViewPagerAdapter viewPagerAdapter;
    static boolean shuffleBoolean=false, repeatBoolean=false;
    private static final int MY_PERMISSION_REQUEST=1;
    public  static  final String MUSIC_LAST_PLAYED="LAST_PLAYED";
    public static final String MUSIC_FILE="STORED_MUSIC";
    public static final String ARTIST_NAME="ARTIST NAME";
    public static final String SONG_NAME="SONG NAME";
    public  static String PATH_TO_FRAG=null;
    public  static String ARTIST_TO_FRAG=null;
    public  static String SONG_NAME_TO_FRAG=null;
    public static boolean SHOW_MINI_PLAYER =false ;
    FragmentMainBinding binding;
    private BroadcastReceiver mBroadcastReceiver;
    public FirebaseAuth mAuth;
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
//        setContentView(R.layout.fragment_main);
        binding = FragmentMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        permission();
        initViewPager();

        mAuth = FirebaseAuth.getInstance();
        signIn();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                hideProgressBar();

            Log.e(TAG, "onReceive: " + intent.getAction());

            switch (intent.getAction()) {
                case MyDownloadService.DOWNLOAD_COMPLETED:
                    // Get number of bytes downloaded
//                    long numBytes = intent.getLongExtra(MyDownloadService.EXTRA_BYTES_DOWNLOADED, 0);

                    // Alert success
//                    showMessageDialog(getString(R.string.success), String.format(Locale.getDefault(),
//                            "%d bytes downloaded from %s",
//                            numBytes,
//                            intent.getStringExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH)));
                    break;
                case MyDownloadService.DOWNLOAD_ERROR:
                    // Alert failure
                    showMessageDialog("Error", String.format(Locale.getDefault(),
                            "Failed to download from %s",
                            intent.getStringExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH)));
                    break;
                case MyUploadService.UPLOAD_COMPLETED:

//                    showMessageDialog(getString(R.string.success), "File uploaded to server");

                    break;
                case MyUploadService.UPLOAD_ERROR:
//                        onUploadResultIntent(intent);
                    showMessageDialog("Error", String.format(Locale.getDefault(),
                            "Failed to upload from %s",
                            intent.getStringExtra(MyUploadService.EXTRA_FILE_URI)));
                    break;
            }
            }
        };

        askNotificationPermission();
    }
    private void initViewPager(){
//        tabLayout=findViewById(R.id.tab_layout);
//        viewPager2=findViewById(R.id.view_pager);
//        viewPagerAdapter=new ViewPagerAdapter(this);
//        viewPager2.setAdapter(viewPagerAdapter);

        tabLayout=binding.tabLayout;
        viewPager2=binding.viewPager;
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
//                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
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
        try {
            if (songCursor != null && songCursor.moveToFirst()) {
                songs = new ArrayList<>();
                int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int songDuraion = songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int songAlbum = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int songPath = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                do {
                    String album = songCursor.getString(songAlbum);
                    String title = songCursor.getString(songTitle);
                    String artist = songCursor.getString(songArtist);
                    int duration = songCursor.getInt(songDuraion);
                    String path = songCursor.getString(songPath);

                    Music song = new Music(path, title, duration, artist, album);
                    songs.add(song);
                } while (songCursor.moveToNext());
                Toast.makeText(getApplicationContext(), "Number songs: " + songs.size(), Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e){

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

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences=getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE);
        String path=preferences.getString(MUSIC_FILE,null);
        String artist =preferences.getString(ARTIST_NAME,null);
        String song_name =preferences.getString(SONG_NAME,null);

        if(path!=null)
        {
            SHOW_MINI_PLAYER=true;
            PATH_TO_FRAG=path;
            ARTIST_TO_FRAG=artist;
            SONG_NAME_TO_FRAG=song_name;

        }
        else {
            SHOW_MINI_PLAYER=false;
            PATH_TO_FRAG=null;
            ARTIST_TO_FRAG=null;
            SONG_NAME_TO_FRAG=null;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.getCurrentUser();

        // Register receiver for uploads and downloads
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mBroadcastReceiver, MyDownloadService.getIntentFilter());
        manager.registerReceiver(mBroadcastReceiver, MyUploadService.getIntentFilter());
    }

    private void showMessageDialog(String title, String message) {
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .create();
        ad.show();
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            // Your app can post notifications.
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        // ask READ_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

    }

    private void signIn() {
        if (mAuth.getCurrentUser() == null) {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
//                                FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(SongActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    });
        } else {
            Log.d(TAG, "signInAnonymously:already signed in");
        }

    }

}



