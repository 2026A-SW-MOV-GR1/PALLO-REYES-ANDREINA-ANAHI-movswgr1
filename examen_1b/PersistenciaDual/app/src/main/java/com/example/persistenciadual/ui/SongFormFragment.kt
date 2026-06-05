package com.example.persistenciadual.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.persistenciadual.databinding.FragmentSongFormBinding
import com.example.persistenciadual.databinding.ItemLinkRowBinding
import com.example.persistenciadual.model.Category
import com.example.persistenciadual.model.Song
import com.example.persistenciadual.model.SongLink

class SongFormFragment : Fragment() {

    companion object {
        private const val TAG = "SongFormFragment"
        private val PLATFORMS = listOf("Spotify", "YouTube", "SoundCloud", "Apple Music", "Otro")
    }

    private var _binding: FragmentSongFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SongViewModel by activityViewModels()
    private var editingSongId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategorySpinner()

        // Determinar si es modo crear o editar
        editingSongId = arguments?.getLong("song_id", 0L)?.takeIf { it > 0L }

        if (editingSongId != null) {
            binding.toolbar.title = "Editar canción"
            val song = viewModel.getSongById(editingSongId!!)
            if (song != null) {
                populateForm(song)
                Log.d(TAG, "DEBUG — Editando canción id=${song.id}: ${song.title}")
            }
        } else {
            binding.toolbar.title = "Nueva canción"
            addLinkRow(null)
            Log.d(TAG, "DEBUG — Creando nueva canción")
        }

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnAddLink.setOnClickListener { addLinkRow(null) }
        binding.btnSave.setOnClickListener { saveSong() }
    }

    private fun setupCategorySpinner() {
        val categories = Category.entries.map { it.displayName }
        binding.spinnerCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
    }

    private fun addLinkRow(link: SongLink?) {
        val rowBinding = ItemLinkRowBinding.inflate(
            layoutInflater, binding.linksContainer, false
        )

        rowBinding.spinnerPlatform.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            PLATFORMS
        )

        if (link != null) {
            val idx = PLATFORMS.indexOf(link.platform).coerceAtLeast(0)
            rowBinding.spinnerPlatform.setSelection(idx)
            rowBinding.etUrl.setText(link.url)
        }

        rowBinding.btnRemoveLink.setOnClickListener {
            binding.linksContainer.removeView(rowBinding.root)
        }

        binding.linksContainer.addView(rowBinding.root)
    }

    private fun populateForm(song: Song) {
        binding.etTitle.setText(song.title)
        binding.etDuration.setText(song.duration)
        binding.spinnerCategory.setSelection(song.category.ordinal)

        for (link in song.links) {
            addLinkRow(link)
        }
    }

    private fun collectLinks(): List<SongLink> {
        val links = mutableListOf<SongLink>()
        for (i in 0 until binding.linksContainer.childCount) {
            val row = binding.linksContainer.getChildAt(i)
            val rowBinding = ItemLinkRowBinding.bind(row)
            val platform = rowBinding.spinnerPlatform.selectedItem.toString()
            val url = rowBinding.etUrl.text.toString().trim()
            if (url.isNotBlank()) {
                links.add(SongLink(platform, url))
            }
        }
        return links
    }

    private fun saveSong() {
        val title = binding.etTitle.text.toString().trim()
        val duration = binding.etDuration.text.toString().trim()

        if (title.isBlank()) {
            binding.tilTitle.error = "Ingresa el nombre"
            return
        }
        if (duration.isBlank()) {
            binding.tilDuration.error = "Ingresa la duración"
            return
        }

        binding.tilTitle.error = null
        binding.tilDuration.error = null

        val categoryIndex = binding.spinnerCategory.selectedItemPosition
        val category = Category.entries[categoryIndex]
        val links = collectLinks()

        val song = Song(
            id = editingSongId ?: 0L,
            title = title,
            duration = duration,
            category = category,
            links = links
        )

        if (editingSongId != null) {
            viewModel.update(song)
            Log.i(TAG, "INFO — Canción actualizada: $title")
        } else {
            viewModel.insert(song)
            Log.i(TAG, "INFO — Canción creada: $title")
        }

        Toast.makeText(requireContext(), "Canción guardada", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}