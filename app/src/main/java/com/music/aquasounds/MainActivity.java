package com.music.aquasounds;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.music.aquasounds.adapters.MusicRecyclerViewAdapter;
import com.music.aquasounds.viewmodels.MusicListViewModel;
import com.music.aquasounds.viewmodels.MusicViewModel;

public class MainActivity extends AppCompatActivity implements MusicRecyclerViewAdapter.OnMusicClickListener {

    private static final String[] PERMISSIONS = {
        Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final String TAG = "MainActivity";

    //vars
    private MediaPlayer mediaPlayer;
    private final MusicListViewModel musicListViewModel = new MusicListViewModel();
    private RecyclerView recyclerView;
    private SeekBar seekBar;

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
        mediaPlayer = new MediaPlayer();
        findViewById(R.id.previousButton).setOnClickListener(view -> previousMusic());
        ImageButton imageButton = findViewById(R.id.pauseButton);
        imageButton.setOnClickListener(view -> pauseMusic(imageButton));
        findViewById(R.id.nextButton).setOnClickListener(view -> nextMusic());
        seekBar = findViewById(R.id.seekBar);
        seekBar.setActivated(true);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekToUpdate();
            }
        });
        musicListViewModel.fillMusicViewModels();
        initMusicRecyclerView();
        if(savedInstanceState != null && savedInstanceState.containsKey("utilityBarVisibility")) {
            try {
                Log.d(TAG, "onCreate: " + savedInstanceState);
                int utilityBarVisibility = savedInstanceState.getInt("utilityBarVisibility");
                findViewById(R.id.utilityBar).setVisibility(utilityBarVisibility);
                findViewById(R.id.seekBar).setVisibility(utilityBarVisibility);
                playMusicAtPosition(savedInstanceState.getInt("currentMusicViewModel"));
                mediaPlayer.seekTo(savedInstanceState.getInt("mediaPlayerCurrentPosition"));
                if(!savedInstanceState.getBoolean("mediaPlayerIsPlaying"))
                    pauseMusic(imageButton);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(musicListViewModel.getCurrentMusicViewModel() == null)
            return;
        outState.putInt("utilityBarVisibility", seekBar.getVisibility());
        outState.putBoolean("mediaPlayerIsPlaying", mediaPlayer.isPlaying());
        outState.putInt("currentMusicViewModel", musicListViewModel.getPositionFromMusicViewModel(musicListViewModel.getCurrentMusicViewModel()));
        outState.putInt("mediaPlayerCurrentPosition", mediaPlayer.getCurrentPosition());
        mediaPlayer.reset();
    }

    private void initMusicRecyclerView() {
        recyclerView = findViewById(R.id.musicRecyclerView);
        MusicRecyclerViewAdapter musicRecyclerViewAdapter = new MusicRecyclerViewAdapter(musicListViewModel, this, this);
        recyclerView.setAdapter(musicRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void OnMusicClick(int position) {
        playMusicAtPosition(position);
    }

    private void playMusicAtPosition(int position) {
        findViewById(R.id.utilityBar).setVisibility(View.VISIBLE);
        findViewById(R.id.seekBar).setVisibility(View.VISIBLE);
        MusicViewModel musicViewModel = musicListViewModel.getMusicViewModelAtPosition(position);
        MusicViewModel currentMusicViewModel = musicListViewModel.getCurrentMusicViewModel();
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scrollToPosition(position);
        if (musicViewModel == currentMusicViewModel)
            return;
        musicListViewModel.setCurrentMusicViewModel(musicViewModel);
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(musicViewModel.getMusicPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            TextView totalMusicTime = findViewById(R.id.totalMusicTime);
            totalMusicTime.setText(durationToString(mediaPlayer.getDuration()));
            seekBar.setMax(mediaPlayer.getDuration());
            TextView currentMusicTime = findViewById(R.id.currentMusicTime);
            currentMusicTime.setText(durationToString(mediaPlayer.getCurrentPosition()));
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            new Thread() {
                @SuppressWarnings("BusyWait")
                public void run() {
                    while(mediaPlayer.getCurrentPosition() < mediaPlayer.getDuration()) {
                        runOnUiThread(() -> {
                            currentMusicTime.setText(durationToString(mediaPlayer.getCurrentPosition()));
                            seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        });
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String durationToString(int duration) {
        int seconds = duration % 60000 / 1000;
        int minutes = duration / 60000;
        String sSeconds = String.valueOf(seconds);
        if(seconds < 10) {
            sSeconds = "0" + sSeconds;
        }
        String sMinutes = String.valueOf(minutes);
        return sMinutes + ":" + sSeconds;
    }

    private void previousMusic() {
        if(mediaPlayer.getCurrentPosition() > 5000)
            mediaPlayer.seekTo(0);
        else {
            MusicViewModel currentMusicViewModel = musicListViewModel.getCurrentMusicViewModel();
            MusicViewModel musicViewModel = musicListViewModel.getMusicViewModelAtPosition(musicListViewModel.getPositionFromMusicViewModel(currentMusicViewModel) - 1);
            playMusicAtPosition(musicListViewModel.getPositionFromMusicViewModel(musicViewModel));
        }
    }

    private void pauseMusic(ImageButton pauseButton) {
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            pauseButton.setImageResource(android.R.drawable.ic_media_play);
        } else {
            mediaPlayer.start();
            pauseButton.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    private void nextMusic() {
        MusicViewModel currentMusicViewModel = musicListViewModel.getCurrentMusicViewModel();
        MusicViewModel musicViewModel = musicListViewModel.getMusicViewModelAtPosition(musicListViewModel.getPositionFromMusicViewModel(currentMusicViewModel) + 1);
        playMusicAtPosition(musicListViewModel.getPositionFromMusicViewModel(musicViewModel));
    }

    private void seekToUpdate() {
        mediaPlayer.seekTo(seekBar.getProgress());
    }
}