package com.music.aquasounds.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.music.aquasounds.R;
import java.util.ArrayList;

public class MusicRecyclerViewAdapter extends RecyclerView.Adapter<MusicRecyclerViewAdapter.MusicViewHolder> {
    private final ArrayList<String> musicPaths;
    private final OnMusicClickListener onMusicClickListener;

    public MusicRecyclerViewAdapter(ArrayList<String> musicPaths, OnMusicClickListener onMusicClickListener) {
        this.musicPaths = musicPaths;
        this.onMusicClickListener = onMusicClickListener;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_recycler_view_item, parent, false);
        return new MusicViewHolder(view, onMusicClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        String musicPath = musicPaths.get(position);
        holder.musicPath = musicPath;
        String musicName = musicPath.substring(musicPath.lastIndexOf('/') + 1, musicPath.lastIndexOf('.'));
        holder.musicName.setText(musicName);
    }

    @Override
    public int getItemCount() {
        return musicPaths.size();
    }

    public static class MusicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView musicName;
        String musicPath;
        RelativeLayout parentLayout;
        OnMusicClickListener onMusicClickListener;

        public MusicViewHolder(@NonNull View itemView, OnMusicClickListener onMusicClickListener) {
            super(itemView);
            musicName = itemView.findViewById(R.id.musicName);
            parentLayout = itemView.findViewById(R.id.musicItem);
            this.onMusicClickListener = onMusicClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onMusicClickListener.OnMusicClick(this);
        }

        public String getMusicPath() {
            return musicPath;
        }

        public void setTextColor(int color) {
            musicName.setTextColor(color);
        }

        public void setBackgroundColor(int color) {
            parentLayout.setBackgroundColor(color);
        }
    }

    public interface OnMusicClickListener {
        void OnMusicClick(MusicViewHolder holder);
    }

//
//    private String durationToString(int duration) {
//        int seconds = duration % 60000 / 1000;
//        int minutes = duration / 60000;
//        String sSeconds = String.valueOf(seconds);
//        if(seconds < 10) {
//            sSeconds = "0" + sSeconds;
//        }
//        String sMinutes = String.valueOf(minutes);
//        return sMinutes + ":" + sSeconds;
//    }
//
//    private void previousMusic() {
//        if(mp.getCurrentPosition() > 5000)
//            mp.seekTo(0);
//        else {
//            String currentPath;
//            try {
//                currentPath = musicFilesList.get(currentlyPlaying - 1);
//            } catch (Exception e) {
//                currentPath = musicFilesList.get(musicFilesList.size() - 1);
//            }
//            playMusicFile(currentPath);
//        }
//    }
//    private void pauseMusic(ImageButton pauseButton) {
//        if(mp.isPlaying()) {
//            mp.pause();
//            pauseButton.setImageResource(android.R.drawable.ic_media_play);
//        } else {
//            mp.start();
//            pauseButton.setImageResource(android.R.drawable.ic_media_pause);
//        }
//    }
//    private void nextMusic() {
//        String currentPath;
//        try {
//            currentPath = musicFilesList.get(currentlyPlaying + 1);
//        } catch (Exception e) {
//            currentPath = musicFilesList.get(0);
//        }
//        playMusicFile(currentPath);
//    }
}
