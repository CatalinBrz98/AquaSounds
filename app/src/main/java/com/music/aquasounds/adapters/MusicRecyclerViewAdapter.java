package com.music.aquasounds.adapters;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.music.aquasounds.R;
import com.music.aquasounds.viewmodels.MusicListViewModel;
import com.music.aquasounds.viewmodels.MusicViewModel;

public class MusicRecyclerViewAdapter extends RecyclerView.Adapter<MusicRecyclerViewAdapter.MusicViewHolder> {
    private final MusicListViewModel musicListViewModel;
    private final OnMusicClickListener onMusicClickListener;
    private final Context context;

    public MusicRecyclerViewAdapter(MusicListViewModel musicListViewModel, OnMusicClickListener onMusicClickListener, Context context) {
        this.musicListViewModel = musicListViewModel;
        this.onMusicClickListener = onMusicClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_recycler_view_item, parent, false);
        return new MusicViewHolder(view, onMusicClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        MusicViewModel musicViewModel = musicListViewModel.getMusicViewModelAtPosition(position);
        holder.musicName.setText(musicViewModel.getMusicName());
        holder.position = position;
        if(musicViewModel == musicListViewModel.getCurrentMusicViewModel()) {
            holder.setTextColor(Color.BLACK);
            holder.setBackgroundColor(context.getResources().getColor(R.color.teal_200));
        }
        else {
            holder.setTextColor(context.getResources().getColor(R.color.teal_200));
            holder.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        return musicListViewModel.getItemCount();
    }

    public static class MusicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView musicName;
        int position;
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
            onMusicClickListener.OnMusicClick(position);
        }

        public void setTextColor(int color) {
            musicName.setTextColor(color);
        }

        public void setBackgroundColor(int color) {
            parentLayout.setBackgroundColor(color);
        }

    }

    public interface OnMusicClickListener {
        void OnMusicClick(int position);
    }
}
