package com.music.aquasounds.viewmodels;
import android.os.Environment;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MusicListViewModel {
    private final ArrayList<MusicViewModel> musicViewModels;
    private MusicViewModel currentMusicViewModel;
    private final ArrayList<String> musicSearchDirectories;

    public MusicListViewModel() {
        musicViewModels = new ArrayList<>();
        currentMusicViewModel = null;
        musicSearchDirectories = new ArrayList<>();
        musicSearchDirectories.add(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)));
        musicSearchDirectories.add(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
        fillMusicViewModels();
    }

    public void sortMusicList() {
        Collections.sort(musicViewModels);
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
            if(extensions.contains(extension))
                musicViewModels.add(new MusicViewModel(filePath));
        }
        sortMusicList();
    }

    public void fillMusicViewModels() {
        musicViewModels.clear();
        for(String musicSearchDirectory: musicSearchDirectories)
            addMusicFilesFrom(musicSearchDirectory);
    }

    public MusicViewModel getMusicViewModelAtPosition(int position) {
        return musicViewModels.get(modHelper(position, getItemCount()));
    }

    public MusicViewModel getCurrentMusicViewModel() {
        return currentMusicViewModel;
    }

    public int getPositionFromMusicViewModel(MusicViewModel musicViewModel) {
        return musicViewModels.indexOf(musicViewModel);
    }

    public void setCurrentMusicViewModel(MusicViewModel musicViewModel) {
        currentMusicViewModel = musicViewModel;
    }

    public void setCurrentMusicViewModel(int position) {
        currentMusicViewModel = musicViewModels.get(modHelper(position, getItemCount()));
    }

    public int getItemCount() {
        return musicViewModels.size();
    }

    public int modHelper(int a, int b) {
        return (a % b + b) % b;
    }
}
