package com.music.aquasounds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            Log.d("filePathDebug",filePath);
            if (filePath.lastIndexOf("/") == -1)
                continue;
            final String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
            if (fileName.lastIndexOf(".") == -1)
                continue;
            final String extension = fileName.substring(fileName.lastIndexOf(".")+1);
            Log.d("extensionDebug",extension);
            if(extensions.contains(extension))
                musicFilesList.add(filePath);
        }
    }

    private void fillMusicList() {
        musicFilesList.clear();
        addMusicFilesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)));
        addMusicFilesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
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
            musicFilesList = new ArrayList<>();
            fillMusicList();
            textAdapter.setData(musicFilesList);
            listView.setAdapter(textAdapter);
            initialized = true;
        }
    }
}

class TextAdapter extends BaseAdapter {
    private List<String> data = new ArrayList<String>();

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
            convertView.setTag(new ViewHolder((TextView) convertView.findViewById(R.id.myItem)));
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