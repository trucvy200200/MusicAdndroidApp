package com.hcmute.finalproject.musicApp_demo.helper;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hcmute.finalproject.musicApp_demo.Adapter.CustomAdapter;
import com.hcmute.finalproject.musicApp_demo.MainActivity;
import com.hcmute.finalproject.musicApp_demo.SongActivity;
import com.hcmute.finalproject.musicApp_demo.model.Music;
import com.hcmute.finalproject.musicApp_demo.model.Song;

import java.util.ArrayList;

public class FirebaseAPI {
    private final SongActivity songActivity;
    private CustomAdapter customAdapter;
    ArrayList<Music> songs;

    public FirebaseAPI(SongActivity activity) {
        this.songActivity = activity;
    }

    public void retrieveSongs(helper helper) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Songs");
        databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                songs = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Music song = ds.getValue(Music.class);
                    songs.add(song);
                }
                customAdapter = new CustomAdapter(songs);
                customAdapter.context = songActivity;
                helper.onCallback(customAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(songActivity, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            };
        });

    }

}

