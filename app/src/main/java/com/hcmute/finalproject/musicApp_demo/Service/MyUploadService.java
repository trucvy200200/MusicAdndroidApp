package com.hcmute.finalproject.musicApp_demo.Service;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hcmute.finalproject.musicApp_demo.MainActivity;
import com.hcmute.finalproject.musicApp_demo.R;
import com.hcmute.finalproject.musicApp_demo.model.Music;

import java.io.InputStream;

public class MyUploadService extends MyBaseTaskService {
    private static final String TAG = "MyUploadService";

    /** Intent Actions **/
    public static final String ACTION_UPLOAD = "action_upload";
    public static final String UPLOAD_COMPLETED = "upload_completed";
    public static final String UPLOAD_ERROR = "upload_error";

    /** Intent Extras **/
    public static final String EXTRA_FILE_URI = "extra_file_uri";
    public static final String EXTRA_DOWNLOAD_URL = "extra_download_url";

    public static final String EXTRA_SONG_NAME = "extra_song_name";
    public static final String EXTRA_FILE_NAME = "extra_file_name";
    public static final String EXTRA_IMAGE_URL = "extra_image_url";
    public static final String EXTRA_ARTIST = "extra_artist";
    public static final String EXTRA_ALBUM = "extra_album";
    public static final String EXTRA_DURATION = "extra_duration";


    // [START declare_ref]
    private StorageReference mStorageRef;
    // [END declare_ref]

    @Override
    public void onCreate() {
        super.onCreate();

        // [START get_storage_ref]
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // [END get_storage_ref]
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:" + intent + ":" + startId);
        if (ACTION_UPLOAD.equals(intent.getAction())) {
            Uri fileUri = intent.getParcelableExtra(EXTRA_FILE_URI);
            String songName = intent.getStringExtra(EXTRA_SONG_NAME);

            if (songName.equals("Unknown")) {
                songName = intent.getStringExtra(EXTRA_FILE_NAME);
            }
            String imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL);
            String artist = intent.getStringExtra(EXTRA_ARTIST);
            String album = intent.getStringExtra(EXTRA_ALBUM);
            int duration = intent.getIntExtra(EXTRA_DURATION, 0);


            // Make sure we have permission to read the data
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getContentResolver().takePersistableUriPermission(
                        fileUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            uploadFromUri(fileUri, songName, imageUrl, artist, album, duration);
        }

        return START_REDELIVER_INTENT;
    }

    private int classifyFile(Uri fileUri) {
        /*
        * 0: audio
        * 1: video
        * 2: image
        */
//        String extension = MimeTypeMap.getFileExtensionFromUrl(String.valueOf(fileUri));
//        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        // get file extension from uri
        String type = getContentResolver().getType(fileUri);
        Log.e("TAG", "classifyFile: " + type);

        if (type == null) {
            return 2; // default to image
        }

        if (type.startsWith("audio/")) {
            return 0;
        } else if (type.startsWith("video/")) {
            return 1;
        } else {
            return 2;
        }
    }

    // [START upload_from_uri]
    private void uploadFromUri(final Uri fileUri, String songName, String imageUrl, String artist, String album, int duration) {
        // [START_EXCLUDE]
        taskStarted();
        showProgressNotification(getString(R.string.progress_uploading), 0, 0);
        // [END_EXCLUDE]

        // [START get_child_ref]
        int type = classifyFile(fileUri);


        String uploadPath;
        if (type != 0) {
            uploadPath = "photos";
        } else {
            uploadPath = "audio";
        }
//        switch (type) {
//            case 0:
//                uploadPath = "audio";
//                break;
//            case 1:
//                uploadPath = "videos";
//                break;
//            default:
//                uploadPath = "photos";
//                break;
//        }
//        File file = new File(fileUri.getPath());
//        String fileName = file.getPath();
//        Log.e("TAG", "uploadFromUri: " + fileName);
        // Get a reference to store file at photos/<FILENAME>.jpg
//        final StorageReference photoRef = mStorageRef.child(uploadPath)
//                .child(fileUri.getLastPathSegment());
        final StorageReference photoRef = mStorageRef.child(uploadPath)
                .child(songName + ".mp3");
        // [END get_child_ref]

        // Upload file to Firebase Storage
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        try {
            InputStream stream = getContentResolver().openInputStream(fileUri);
            UploadTask uploadTask = photoRef.putStream(stream);

            uploadTask.addOnFailureListener(e -> {
                Log.w(TAG, "uploadFromUri:onFailure", e);

                // [START_EXCLUDE]
                broadcastUploadFinished(null, fileUri);
                showUploadFinishedNotification(null, fileUri);
                taskCompleted();
                // [END_EXCLUDE]
            }).addOnSuccessListener(taskSnapshot -> {
                photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.e("TAG", "onSuccess: " + uri.toString());
                    broadcastUploadFinished(uri, fileUri);
                    showUploadFinishedNotification(uri, fileUri);

//                    String path = uri.toString(); // later use this to stream the file
//                    String storagePath = taskSnapshot.getMetadata().getPath(); // later use this to download the file
                    String path = taskSnapshot.getMetadata().getPath(); // later use this to download the file
                    uploadDetailsToDatabase(songName, path, imageUrl, artist, album, duration);
                    taskCompleted();
                    Log.e("onSuccess", "onSuccess: " + taskSnapshot.getMetadata().getReference().getDownloadUrl());
                });
                // [END_EXCLUDE]
            }).addOnProgressListener(taskSnapshot -> {
                showProgressNotification(getString(R.string.progress_uploading),
                        taskSnapshot.getBytesTransferred(),
                        taskSnapshot.getTotalByteCount());
                Log.e("onProgress", "onProgress: " + taskSnapshot.getBytesTransferred() + " " + taskSnapshot.getTotalByteCount());
            });
        } catch (Exception e) {
            Log.e(TAG, "uploadFromUri: Exception: " + e.getMessage());
        }

    }
    // [END upload_from_uri]

    // UPLOAD SONG NAME AND URL TO REALTIME DATABASE
    public void uploadDetailsToDatabase(String songName, String songUrl, String imageUrl, String artistName, String albumName, int songDuration){

//        Music song = new Music(songUrl, songName, songDuration, artistName, albumName);

        Music song = new Music(songUrl, songName, songDuration, artistName, albumName, imageUrl);
//        song.setStoragePath(storagePath);

        FirebaseDatabase.getInstance().getReference("Songs")
                .push().setValue(song).addOnCompleteListener(task -> {
//                    Toast.makeText(getApplicationContext(), "Song Uploaded to Database", Toast.LENGTH_SHORT).show();
                    taskCompleted();
                }).addOnFailureListener(e -> {
            Log.i("database", "upload failed" + e.getMessage());
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }

    /**
     * Broadcast finished upload (success or failure).
     * @return true if a running receiver received the broadcast.
     */
    private boolean broadcastUploadFinished(@Nullable Uri downloadUrl, @Nullable Uri fileUri) {
        boolean success = downloadUrl != null;

        String action = success ? UPLOAD_COMPLETED : UPLOAD_ERROR;

        Intent broadcast = new Intent(action)
                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
                .putExtra(EXTRA_FILE_URI, fileUri);
        // get main activity context
//        MainActivity activity = (MainActivity) getActivity();
//        activity.sendBroadcast(broadcast);
        return LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(broadcast);
//        return getApplicationContext().sendBroadcast(broadcast);
    }

    /**
     * Show a notification for a finished upload.
     */
    private void showUploadFinishedNotification(@Nullable Uri downloadUrl, @Nullable Uri fileUri) {
        // Hide the progress notification
        dismissProgressNotification();

        // Make Intent to MainActivity
//        Intent intent = new Intent(this, MainActivity.class)
//                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
//                .putExtra(EXTRA_FILE_URI, fileUri)
//                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

//        boolean success = downloadUrl != null;
//        String caption = success ? getString(R.string.upload_success) : getString(R.string.upload_failure);
//        showFinishedNotification(caption, intent, success);
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPLOAD_COMPLETED);
        filter.addAction(UPLOAD_ERROR);

        return filter;
    }
}
