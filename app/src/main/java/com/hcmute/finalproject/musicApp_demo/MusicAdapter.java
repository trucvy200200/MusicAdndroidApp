package com.hcmute.finalproject.musicApp_demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hcmute.finalproject.musicApp_demo.model.Music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {
    private Context mContext;
    private ArrayList<Music> songs;
    public MusicAdapter(Context mContext, ArrayList<Music> songs){
        this.songs=songs;
        this.mContext=mContext;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.music, parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.file_name.setText(songs.get(position).getTitle());
        holder.artist.setText(songs.get(position).getArtist());
        try {
            byte[] image = getAlbumArt(songs.get(position).getPath());
            if (image!=null){
                Glide.with(mContext).asBitmap().load(image)
                        .into(holder.album_art);
            }
            else{
                Glide.with(mContext).load(R.drawable.ic_heart)
                        .into(holder.album_art);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext,PlayerActivity.class);
                intent.putExtra("position",holder.getAbsoluteAdapterPosition());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView file_name;
        TextView artist;
        ImageView album_art;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            artist=itemView.findViewById(R.id.artist);
            file_name=itemView.findViewById(R.id.title);
            album_art=itemView.findViewById(R.id.image);
        }

    }
    private byte[] getAlbumArt(String uri) throws IOException {
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art=retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
