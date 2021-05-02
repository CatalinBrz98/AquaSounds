package com.music.aquasounds;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.music.aquasounds.adapters.MusicRecyclerViewAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MusicRecyclerViewAdapter.OnMusicClickListener {

    private static final String[] PERMISSIONS = {
        Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final String TAG = "MainActivity";

    //vars
    private MediaPlayer mediaPlayer;
    private final ArrayList<String> musicPaths = new ArrayList<>();
    private MusicRecyclerViewAdapter.MusicViewHolder currentPlayingHolder;

    @SuppressLint("NewApi")
    private boolean permissionsGranted() {
        for (String permission : PERMISSIONS) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(permissionsGranted()) {
            onResume();
        } else {
            ((ActivityManager) (this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
            recreate();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: started.");
        mediaPlayer = new MediaPlayer();
        fillMusicList();
    }

    private void sortMusicList() {
        Collections.sort(musicPaths);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void addMusicFilesFrom(String dirPath) {
        final File musicDir = new File(dirPath);
        if(!musicDir.exists()) {
            musicDir.mkdir();
            return;
        }
        final File[] files = musicDir.listFiles();
        if(files == null)
            return;
        List<String> extensions = Arrays.asList("mp3", "wav");
        for(File file : files) {
            final String filePath = file.getAbsolutePath();
            if (filePath.lastIndexOf("/") == -1)
                continue;
            final String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            if (fileName.lastIndexOf(".") == -1)
                continue;
            final String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
            if(extensions.contains(extension)) {
                musicPaths.add(filePath);
                Log.d(TAG, "addMusicFilesFrom: " + filePath);
            }
        }
        sortMusicList();
        initMusicRecyclerView();
    }

    private void fillMusicList() {
        musicPaths.clear();
        addMusicFilesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)));
        addMusicFilesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
    }

    private void initMusicRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.musicRecyclerView);
        Log.d(TAG, "initMusicRecyclerView: started.");
        MusicRecyclerViewAdapter musicRecyclerViewAdapter = new MusicRecyclerViewAdapter(musicPaths, this);
        recyclerView.setAdapter(musicRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void OnMusicClick(MusicRecyclerViewAdapter.MusicViewHolder holder) {
        Log.d(TAG, "OnMusicClick: clicked " + holder.getAdapterPosition());
        String musicPath = holder.getMusicPath();
        if (currentPlayingHolder == holder)
            return;
        playMusicFile(musicPath);
        if (currentPlayingHolder != null) {
            currentPlayingHolder.setTextColor(getResources().getColor(R.color.teal_200));
            currentPlayingHolder.setBackgroundColor(Color.TRANSPARENT);
        }
        holder.setTextColor(Color.BLACK);
        holder.setBackgroundColor(getResources().getColor(R.color.teal_200));
        currentPlayingHolder = holder;
    }

    private void playMusicFile(String musicPath) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(musicPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}