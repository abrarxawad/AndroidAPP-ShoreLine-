package com.shoreline.app.ui.admin;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.shoreline.app.databinding.ItemAdminStatTileBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * SYLLABUS: RecyclerView Adapter with GridLayoutManager (Dashboard command tiles)
 */
public class AdminStatTileAdapter extends RecyclerView.Adapter<AdminStatTileAdapter.StatTileViewHolder> {

    public interface OnTileClickListener {
        void onTileClick(AdminStatTile tile);
    }

    private final List<AdminStatTile> tiles = new ArrayList<>();
    private OnTileClickListener clickListener;

    public void setOnTileClickListener(OnTileClickListener listener) {
        this.clickListener = listener;
    }

    public void setTiles(List<AdminStatTile> newTiles) {
        this.tiles.clear();
        if (newTiles != null) {
            this.tiles.addAll(newTiles);
        }
        notifyDataSetChanged();
    }

    public void updateTileValue(AdminStatTile.TileType type, String newValue, String newSubtitle) {
        for (int i = 0; i < tiles.size(); i++) {
            if (tiles.get(i).getType() == type) {
                tiles.get(i).setValue(newValue);
                if (newSubtitle != null) {
                    tiles.get(i).setSubtitle(newSubtitle);
                }
                notifyItemChanged(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public StatTileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminStatTileBinding binding = ItemAdminStatTileBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new StatTileViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StatTileViewHolder holder, int position) {
        holder.bind(tiles.get(position));
    }

    @Override
    public int getItemCount() {
        return tiles.size();
    }

    class StatTileViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminStatTileBinding binding;

        StatTileViewHolder(ItemAdminStatTileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AdminStatTile tile) {
            binding.tvStatTitle.setText(tile.getTitle());
            binding.tvStatValue.setText(tile.getValue());
            binding.tvStatSubtitle.setText(tile.getSubtitle());
            binding.ivTileIcon.setImageResource(tile.getIconRes());

            try {
                int accent = Color.parseColor(tile.getAccentColor());
                int bgTint = Color.parseColor(tile.getBgTintColor());
                binding.tvStatValue.setTextColor(accent);
                binding.iconContainer.getBackground().setColorFilter(bgTint, PorterDuff.Mode.SRC_IN);
                binding.ivTileIcon.setColorFilter(accent, PorterDuff.Mode.SRC_IN);
            } catch (Exception ignored) {}

            binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onTileClick(tile);
                }
            });
        }
    }
}

