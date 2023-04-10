package com.hcmute.finalproject.musicApp_demo.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.Query;
import com.hcmute.finalproject.musicApp_demo.PlayerActivity;
import com.hcmute.finalproject.musicApp_demo.PlayerStreamActivity;
import com.hcmute.finalproject.musicApp_demo.databinding.CustomRowBinding;
import com.hcmute.finalproject.musicApp_demo.databinding.MusicBinding;
import com.hcmute.finalproject.musicApp_demo.model.Music;
import com.hcmute.finalproject.musicApp_demo.model.Song;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {
    public Context context;

    MusicBinding binding;
    private Query query;
    private List<Music> songs;

    public CustomAdapter(Query query, List<Music> songs) {
        this.songs = songs;
        this.query = query;
    }

    public CustomAdapter(List<Music> songs) {
        this.songs = songs;
    }

    public CustomAdapter(Context context, List<Music> songs) {
        this.context = context;
        this.songs = songs;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = MusicBinding.inflate(LayoutInflater.from(context), parent, false);
        View view = binding.getRoot();
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Music song = songs.get(position);

//        holder.song_id_txt.setText(String.valueOf(position + 1));
        holder.song_title_txt.setText(String.valueOf(song.getTitle()));
        holder.song_singer_txt.setText(String.valueOf(song.getArtist()));
//        holder.song_stream_txt.setText(String.valueOf(song.getSongDuration()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayerStreamActivity.class);
            intent.putExtra("position", position);
            intent.putExtra("songs", (Serializable) songs);
            context.startActivity(intent);
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView song_id_txt, song_title_txt, song_singer_txt, song_stream_txt;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
//            song_id_txt = binding.songId;
            song_title_txt = binding.title;
            song_singer_txt = binding.artist;
//            song_stream_txt = binding.songStream;
        }
    }

    @Override
    public int getItemCount() {
        if (songs != null) {
            return songs.size();
        }
        return 0;
    }

    public void updateList(List<Music> newList) {
        songs = newList;
        notifyDataSetChanged();
    }

}