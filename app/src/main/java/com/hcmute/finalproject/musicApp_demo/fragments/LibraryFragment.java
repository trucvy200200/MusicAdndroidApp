package com.hcmute.finalproject.musicApp_demo.fragments;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hcmute.finalproject.musicApp_demo.Adapter.CustomAdapter;
import com.hcmute.finalproject.musicApp_demo.MainActivity;
import com.hcmute.finalproject.musicApp_demo.R;
import com.hcmute.finalproject.musicApp_demo.Service.MyUploadService;
import com.hcmute.finalproject.musicApp_demo.SongActivity;
import com.hcmute.finalproject.musicApp_demo.databinding.FragmentLibraryBinding;
import com.hcmute.finalproject.musicApp_demo.helper.FirebaseAPI;
import com.hcmute.finalproject.musicApp_demo.helper.helper;

import java.util.HashMap;


public class LibraryFragment extends Fragment implements helper {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private FragmentLibraryBinding binding;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    static FloatingActionButton uploadFab;
    private ActivityResultLauncher<String[]> intentLauncher;

    public LibraryFragment() {
        // Required empty public constructor
    }
    // TODO: Rename and change types and number of parameters
//    public static LibraryFragment newInstance(String param1, String param2) {
//        LibraryFragment fragment = new LibraryFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLibraryBinding.inflate(inflater, container, false);

        FirebaseAPI firebaseAPI = new FirebaseAPI((SongActivity) getActivity());
        firebaseAPI.retrieveSongs(this);

        uploadFab = binding.uploadFab;
        uploadFab.setOnClickListener(v -> {
            launchDoc();
        });

        intentLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(), fileUri -> {
                    if (fileUri != null) {
                        uploadFromUri(fileUri);
                    } else {
                        Log.w(TAG, "File URI is null");
                    }
                });

        return binding.getRoot();
    }
    @Override
    public void onCallback(CustomAdapter customAdapter) {
        RecyclerView recyclerView = binding.recyclerView;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        if (customAdapter != null && customAdapter.getItemCount() != 0) {
            binding.emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void uploadFromUri(Uri fileUri) {

        SongActivity activity = (SongActivity) getActivity();

        // Save the File URI
//        activity.mFileUri = fileUri;

        // Clear the last download, if any
//        activity.updateUI(activity.mAuth.getCurrentUser());
//        activity.mDownloadUrl = null;

        // get file metadata
        String artist, album, title, fileName = null;

        // put all the metadata into a hashmap
        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("artist", null);
        metadata.put("album", null);
        metadata.put("title", null);
        metadata.put("fileName", null);

        int duration = 0;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(getActivity(), fileUri);
            duration = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            Cursor cursor = activity.getContentResolver().query(fileUri, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                // remove file extension
                fileName = fileName.substring(0, fileName.lastIndexOf("."));

            }

            // put all the metadata into a hashmap
            metadata.put("artist", artist);
            metadata.put("album", album);
            metadata.put("title", title);
            metadata.put("fileName", fileName);
            metadata.put("duration", String.valueOf(duration));

            // initialize upload args (put default values)
            metadata = initUploadArgs(metadata);

        } catch (Exception e) {
            Log.e(TAG, "uploadFromUri:exception" + e.getMessage());
        }

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        activity.startService(new Intent(activity, MyUploadService.class)
                .putExtra(MyUploadService.EXTRA_FILE_URI, fileUri)
                .putExtra(MyUploadService.EXTRA_SONG_NAME, metadata.get("title"))
                .putExtra(MyUploadService.EXTRA_FILE_NAME, metadata.get("fileName"))
                .putExtra(MyUploadService.EXTRA_IMAGE_URL, "")
                .putExtra(MyUploadService.EXTRA_ARTIST, metadata.get("artist"))
                .putExtra(MyUploadService.EXTRA_DURATION, Integer.parseInt(metadata.get("duration")))
                .setAction(MyUploadService.ACTION_UPLOAD));

//        Log.e(TAG, "uploadFromUri: " + metadata.get("title") + " " + metadata.get("fileName") + " " + metadata.get("artist") + " " + metadata.get("duration"));

        // Show loading spinner
//        activity.showProgressBar(getString(R.string.progress_uploading));

    }

    private HashMap initUploadArgs(HashMap<String, String> metadata) {
        // for each key in the hashmap, if the value is null, set it to "Unknown"
        for (String key : metadata.keySet()) {
            if (metadata.get(key) == null) {
                metadata.put(key, "Unknown");
            }
        }
        return metadata;
    }

    private void launchDoc() {
        // Pick an image from storage
//        intentLauncher.launch(new String[]{ "image/*", "video/*", "audio/*" });
        intentLauncher.launch(new String[]{ "audio/*" });
    }

}