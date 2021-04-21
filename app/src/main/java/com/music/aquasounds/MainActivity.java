package com.music.aquasounds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final int REQUEST_CODE = 9257;
    private boolean initialized;
    private List<String> musicFilesList;
    private final MediaPlayer mp = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);
    }

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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void addMusicFilesFrom(String dirPath) {
        final File musicDir = new File(dirPath);
        if(!musicDir.exists()) {
            musicDir.mkdir();
            return;
        }
        final File[] files = musicDir.listFiles();
        List<String> extensions = Arrays.asList("mp3", "wav");
        for(File file : files) {
            final String filePath = file.getAbsolutePath();
            Log.d("filePathDebug", filePath);
            if (filePath.lastIndexOf("/") == -1)
                continue;
            final String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            if (fileName.lastIndexOf(".") == -1)
                continue;
            final String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
            Log.d("extensionDebug", extension);
            if(extensions.contains(extension))
                musicFilesList.add(filePath);
        }
    }

    private void fillMusicList() {
        musicFilesList.clear();
        addMusicFilesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)));
        addMusicFilesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
    }

    private int playMusicFile(String filePath) {
        try {
            mp.reset();
            mp.setDataSource(filePath);
            mp.prepare();
            mp.start();
            Log.d("playingDebug",filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mp.getDuration();
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

    @Override
    protected void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !permissionsGranted()) {
            requestPermissions(PERMISSIONS, REQUEST_CODE);
            return;
        }
        if(!initialized) {
            final ListView listView = findViewById(R.id.listView);
            final TextAdapter textAdapter = new TextAdapter();
            final SeekBar seekBar = findViewById(R.id.seekBar);
            final TextView currentMusicTime = findViewById(R.id.currentMusicTime);
            final TextView totalMusicTime = findViewById(R.id.totalMusicTime);
            musicFilesList = new ArrayList<>();
            fillMusicList();
            textAdapter.setData(musicFilesList);
            listView.setAdapter(textAdapter);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mp.seekTo(seekBar.getProgress());
                }
            });
            mp.setOnCompletionListener(mediaPlayer -> seekBar.setVisibility(View.GONE));
            listView.setOnItemClickListener((adapterView, view, i, l) -> {
                final String musicFilePath = musicFilesList.get(i);
                final int songDuration = playMusicFile(musicFilePath);
                seekBar.setMax(songDuration);
                runOnUiThread(() -> {
                    currentMusicTime.setText(durationToString(0));
                    currentMusicTime.setText(durationToString(mp.getCurrentPosition()));
                });
                runOnUiThread(() -> totalMusicTime.setText(durationToString(songDuration)));
                new Thread() {
                    @SuppressWarnings("BusyWait")
                    public void run() {
                        while(mp.isPlaying()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(() -> {
                                currentMusicTime.setText(durationToString(mp.getCurrentPosition()));
                                seekBar.setProgress(mp.getCurrentPosition());
                            });
                        }
                    }
                }.start();
                seekBar.setVisibility(View.VISIBLE);
            });
            initialized = true;
        }
    }
}

class TextAdapter extends BaseAdapter {
    private final List<String> data = new ArrayList<>();

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public String getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            convertView.setTag(new ViewHolder(convertView.findViewById(R.id.musicItem)));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        final String item = data.get(i);
        holder.info.setText(item.substring(item.lastIndexOf("/") + 1, item.lastIndexOf(".")));
        return convertView;
    }

    void setData(List<String> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }
}

class ViewHolder {
    TextView info;

    ViewHolder(TextView info) {
        this.info = info;
    }
}