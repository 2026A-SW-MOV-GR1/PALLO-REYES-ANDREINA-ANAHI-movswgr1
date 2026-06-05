package com.example.persistenciadual.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.persistenciadual.databinding.ItemSongBinding
import com.example.persistenciadual.model.Song

class SongAdapter(
    private val onItemClick: (Song) -> Unit,
    private val onDeleteClick: (Song) -> Unit
) : ListAdapter<Song, SongAdapter.SongViewHolder>(DiffCallback) {

    var engineLabel: String = "SQL"
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    companion object DiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(old: Song, new: Song) = old.id == new.id
        override fun areContentsTheSame(old: Song, new: Song) = old == new
    }

    inner class SongViewHolder(private val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song) {
            binding.tvTitle.text = song.title
            binding.tvDuration.text = song.duration
            binding.chipCategory.text = song.category.displayName

            binding.chipEngine.text = engineLabel
            if (engineLabel == "SQL") {
                binding.chipEngine.setChipBackgroundColorResource(android.R.color.holo_green_dark)
            } else {
                binding.chipEngine.setChipBackgroundColorResource(android.R.color.holo_blue_dark)
            }

            val platforms = song.links.joinToString(", ") { it.platform }
            binding.tvLinks.text = if (platforms.isNotBlank()) platforms else "Sin enlaces"

            binding.btnDelete.setOnClickListener { onDeleteClick(song) }
            binding.root.setOnClickListener { onItemClick(song) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}