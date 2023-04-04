package com.hcmute.finalproject.musicApp_demo.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.finalproject.musicApp_demo.databinding.CustomRowBinding;
import com.hcmute.finalproject.musicApp_demo.model.Music;
import com.hcmute.finalproject.musicApp_demo.model.Song;

import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {
    public Context context;

    CustomRowBinding binding;
    private List<Song> songs;

    public CustomAdapter(List<Song> songs) {
        this.songs = songs;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = CustomRowBinding.inflate(LayoutInflater.from(context), parent, false);
        View view = binding.getRoot();
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Song song = songs.get(position);

//        holder.song_id_txt.setText(String.valueOf(position + 1));
        holder.song_title_txt.setText(String.valueOf(song.getSongName()));
        holder.song_singer_txt.setText(String.valueOf(song.getSongArtist()));
        holder.song_stream_txt.setText(String.valueOf(song.getSongDuration()));

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView song_id_txt, song_title_txt, song_singer_txt, song_stream_txt;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
//            song_id_txt = binding.songId;
            song_title_txt = binding.songTitle;
            song_singer_txt = binding.songSinger;
            song_stream_txt = binding.songStream;
        }
    }

    @Override
    public int getItemCount() {
        if (songs != null) {
            return songs.size();
        }
        return 0;
    }

}