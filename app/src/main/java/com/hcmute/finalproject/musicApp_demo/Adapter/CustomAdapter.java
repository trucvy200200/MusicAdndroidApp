package com.hcmute.finalproject.musicApp_demo.Adapter;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.Query;
import com.hcmute.finalproject.musicApp_demo.PlayerActivity;
import com.hcmute.finalproject.musicApp_demo.PlayerStreamActivity;
import com.hcmute.finalproject.musicApp_demo.R;
import com.hcmute.finalproject.musicApp_demo.databinding.CustomRowBinding;
import com.hcmute.finalproject.musicApp_demo.databinding.MusicBinding;
import com.hcmute.finalproject.musicApp_demo.model.Music;
import com.hcmute.finalproject.musicApp_demo.model.Song;

import java.io.IOException;
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
        holder.song_title_txt.setText(String.valueOf(song.getTitle()));
        holder.song_singer_txt.setText(String.valueOf(song.getArtist()));

        try {
                Glide.with(context).load(R.drawable.ic_default)
                        .into(holder.album_art);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayerStreamActivity.class);
            intent.putExtra("position", position);
            intent.putExtra("songs", (Serializable) songs);
            intent.putExtra("song", song);
            context.startActivity(intent);
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView song_id_txt, song_title_txt, song_singer_txt, song_stream_txt;
        ImageView album_art;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
//            song_id_txt = binding.songId;
            song_title_txt = binding.title;
            song_singer_txt = binding.artist;
            album_art = binding.image;
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

    private byte[] getAlbumArt(String uri) throws IOException {
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        Log.d("Check Uri", uri);
        retriever.setDataSource(uri);
        byte[] art=retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

}