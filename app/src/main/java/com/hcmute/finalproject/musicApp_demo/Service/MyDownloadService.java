package com.hcmute.finalproject.musicApp_demo.Service;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import com.hcmute.finalproject.musicApp_demo.MainActivity;
import com.hcmute.finalproject.musicApp_demo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MyDownloadService extends MyBaseTaskService {

    private static final String TAG = "Storage#DownloadService";

    /** Actions **/
    public static final String ACTION_DOWNLOAD = "action_download";
    public static final String DOWNLOAD_COMPLETED = "download_completed";
    public static final String DOWNLOAD_ERROR = "download_error";

    /** Extras **/
    public static final String EXTRA_DOWNLOAD_PATH = "extra_download_path";
    public static final String EXTRA_BYTES_DOWNLOADED = "extra_bytes_downloaded";

    private StorageReference mStorageRef;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Storage
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:" + intent + ":" + startId);

        if (ACTION_DOWNLOAD.equals(intent.getAction())) {
            // Get the path to download from the intent
            String downloadPath = intent.getStringExtra(EXTRA_DOWNLOAD_PATH);
            downloadFromPath(downloadPath);
        }

        return START_REDELIVER_INTENT;
    }


    private void downloadFromPath(final String downloadPath) {
        Log.d(TAG, "downloadFromPath:" + downloadPath);

        // Mark task started
        taskStarted();
//        showProgressNotification(getString(R.string.progress_downloading), 0, 0);


//        mStorageRef.child(downloadPath).getStream()

        // save to sdcard
        File musicFolder = new File(Environment.getExternalStorageDirectory() + "/MusicApp");
        try {
            // make sure the Music folder exists
            if (!musicFolder.exists()) {
                boolean result = musicFolder.mkdirs();
                Log.d(TAG, "downloadFromPath: " + "Music folder created: " + result);
            }

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }

        try {
            File file = new File(musicFolder.getAbsolutePath(), downloadPath.lastIndexOf('/') > 0 ? downloadPath.substring(downloadPath.lastIndexOf('/') + 1) : downloadPath);
            Log.e("Download", file.getAbsolutePath());

            // get download url
//            String downloadUrl = mStorageRef.child(downloadPath).getDownloadUrl().toString();
//            StorageReference ref = mStorageRef.child(downloadPath);
//            ref.getDownloadUrl().addOnSuccessListener(downloadURI -> {
//                ref.child(downloadPath).getFile(file)
//                    .addOnSuccessListener(taskSnapshot -> {
//                        Log.d(TAG, "download:SUCCESS");
//
//                        // Send success broadcast with number of bytes downloaded
//                        broadcastDownloadFinished(downloadPath, taskSnapshot.getTotalByteCount());
//                        showDownloadFinishedNotification(downloadPath, (int) taskSnapshot.getTotalByteCount());
//
//                        // Log downloaded path
//                        Log.d(TAG, "download:SUCCESS:" + downloadPath);
//
//                        // Mark task completed
//                        taskCompleted();
//                    }).addOnFailureListener(exception -> {
//                        Log.w(TAG, "download:FAILURE", exception);
//
//                        // Send failure broadcast
//                        broadcastDownloadFinished(downloadPath, -1);
//                        showDownloadFinishedNotification(downloadPath, -1);
//
//                        // Mark task completed
//                        taskCompleted();
//                    });
//            }).addOnFailureListener(exception -> {
//                Log.w(TAG, "download:FAILURE", exception);
//
//                // Send failure broadcast
//                broadcastDownloadFinished(downloadPath, -1);
//                showDownloadFinishedNotification(downloadPath, -1);
//
//                // Mark task completed
//                taskCompleted();
//            });

            mStorageRef.child(downloadPath).getFile(file)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(TAG, "download:SUCCESS");

                        // Send success broadcast with number of bytes downloaded
                        broadcastDownloadFinished(downloadPath, taskSnapshot.getTotalByteCount());
                        showDownloadFinishedNotification(downloadPath, (int) taskSnapshot.getTotalByteCount());

                        // Log downloaded path
                        Log.d(TAG, "download:SUCCESS:" + downloadPath);

                        // update the SCAN_FILE
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        intent.setData(Uri.fromFile(file));
                        sendBroadcast(intent);

                        // Mark task completed
                        taskCompleted();
                    })
                    .addOnFailureListener(exception -> {
                        Log.w(TAG, "download:FAILURE", exception);

                        // Send failure broadcast
                        broadcastDownloadFinished(downloadPath, -1);
                        showDownloadFinishedNotification(downloadPath, -1);

                        // Mark task completed
                        taskCompleted();
                    });

        } catch (Exception e) {
            Log.e("Download", e.getMessage());
        }

        // Download and get total bytes
//        mStorageRef.child(downloadPath).getStream(
//                        (taskSnapshot, inputStream) -> {
//                            // Close the stream at the end of the Task
//                            inputStream.close();
//                        })
//                .addOnSuccessListener(taskSnapshot -> {
//                    Log.d(TAG, "download:SUCCESS");
//
//                    // Send success broadcast with number of bytes downloaded
//                    broadcastDownloadFinished(downloadPath, taskSnapshot.getTotalByteCount());
//                    showDownloadFinishedNotification(downloadPath, (int) taskSnapshot.getTotalByteCount());
//
//                    // Mark task completed
//                    taskCompleted();
//                })
//                .addOnFailureListener(exception -> {
//                    Log.w(TAG, "download:FAILURE", exception);
//
//                    // Send failure broadcast
//                    broadcastDownloadFinished(downloadPath, -1);
//                    showDownloadFinishedNotification(downloadPath, -1);
//
//                    // Mark task completed
//                    taskCompleted();
//                });
    }

    /**
     * Broadcast finished download (success or failure).
     * @return true if a running receiver received the broadcast.
     */
    private boolean broadcastDownloadFinished(String downloadPath, long bytesDownloaded) {
        boolean success = bytesDownloaded != -1;
        String action = success ? DOWNLOAD_COMPLETED : DOWNLOAD_ERROR;

        Intent broadcast = new Intent(action)
                .putExtra(EXTRA_DOWNLOAD_PATH, downloadPath)
                .putExtra(EXTRA_BYTES_DOWNLOADED, bytesDownloaded);
        Log.e(TAG, "broadcastDownloadFinished: " + broadcast.toString());

        //getApplicationContext()
        return LocalBroadcastManager.getInstance(this)
                .sendBroadcast(broadcast);
    }

    /**
     * Show a notification for a finished download.
     */
    private void showDownloadFinishedNotification(String downloadPath, int bytesDownloaded) {
        // Hide the progress notification
        dismissProgressNotification();

        // Make Intent to MainActivity
        Intent intent = new Intent(this, MainActivity.class)
                .putExtra(EXTRA_DOWNLOAD_PATH, downloadPath)
                .putExtra(EXTRA_BYTES_DOWNLOADED, bytesDownloaded)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        boolean success = bytesDownloaded != -1;
        String caption = success ? getString(R.string.download_success) : getString(R.string.download_failure);
        showFinishedNotification(caption, intent, true);
    }


    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DOWNLOAD_COMPLETED);
        filter.addAction(DOWNLOAD_ERROR);

        return filter;
    }
}
