package com.music.aquasounds;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.music.aquasounds.adapters.MusicRecyclerViewAdapter;
import com.music.aquasounds.viewmodels.MusicListViewModel;
import com.music.aquasounds.viewmodels.MusicViewModel;
import java.util.Objects;

public class MusicListActivity extends AppCompatActivity implements MusicRecyclerViewAdapter.OnMusicClickListener {

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };
    private static final String TAG = "MainActivity";

    //vars
    private MediaPlayer mediaPlayer;
    private final MusicListViewModel musicListViewModel = new MusicListViewModel();
    private RecyclerView recyclerView;
    ImageButton pauseButton;
    private SeekBar seekBar;
    FirebaseAuth mAuth;

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
        setContentView(R.layout.activity_music_list);
        mediaPlayer = new MediaPlayer();
        findViewById(R.id.previousButton).setOnClickListener(view -> previousMusic());
        pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(view -> pauseMusic());
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
                    pauseMusic();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mAuth = FirebaseAuth.getInstance();
        updateUserName();
        findViewById(R.id.logoutButton).setOnClickListener(view -> handleLogout());
        createNotificationChannel();
        createNotification();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.reset();
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
        Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
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
        pauseButton.setImageResource(android.R.drawable.ic_media_pause);
    }

    private void handleLogout() {
        mAuth.signOut();
        LoginManager.getInstance().logOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
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

    private void pauseMusic() {
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

    private void updateUserName() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        TextView userName = findViewById(R.id.userName);
        assert currentUser != null;
        userName.setText(currentUser.getDisplayName());
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "aquaSoundsChannel";
            String description = "Channel for Aqua Sounds App";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("aquaSounds", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotification() {
        Intent intent = new Intent(this, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "aquaSounds")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Start")
                .setContentText("Open the application.")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        Notification logoutNotification = builder.build();
        notificationManager.notify(100, logoutNotification);
    }
}