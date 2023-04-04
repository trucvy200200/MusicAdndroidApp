package com.hcmute.finalproject.musicApp_demo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
//import com.spotify.sdk.android.player.PlaybackState;
//import com.spotify.sdk.android.player.SpotifyPlayer;
//
//import kaaes.spotify.webapi.android.SpotifyService;
public class MainActivity extends AppCompatActivity
    //implements ConnectionStateCallback
{

    private static final String TAG = "Spotify MainActivity";
//    public static SpotifyPlayer mPlayer;
//    public static PlaybackState mCurrentPlaybackState;
//    private Toast mToast;
//    private String AUTH_TOKEN;
//    public static SpotifyService spotifyService;

    TabLayout tabLayout;
    ViewPager2 viewPager2;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);

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
        /*FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(R.id.fragment_container, new MainFragment()).commit();

        AUTH_TOKEN = getIntent().getStringExtra(SpotifyLoginActivity.AUTH_TOKEN);

        onAuthenticationComplete(AUTH_TOKEN);*/

    }


}

