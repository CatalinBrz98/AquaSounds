package com.music.aquasounds.viewmodels;
import androidx.lifecycle.ViewModel;

public class MusicViewModel extends ViewModel implements Comparable<MusicViewModel> {
    private final String musicPath;

    public MusicViewModel(String musicPath) {
        this.musicPath = musicPath;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public String getMusicName() {
        return musicPath.substring(musicPath.lastIndexOf('/') + 1, musicPath.lastIndexOf('.'));
    }

    @Override
    public int compareTo(MusicViewModel musicViewModel) {
        return musicPath.compareTo(musicViewModel.musicPath);
    }
}
