package com.example.persistenciadual.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.persistenciadual.R
import com.example.persistenciadual.adapter.SongAdapter
import com.example.persistenciadual.databinding.FragmentSongListBinding
import com.example.persistenciadual.model.Song
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class SongListFragment : Fragment() {

    companion object {
        private const val TAG = "SongListFragment"
    }

    private var _binding: FragmentSongListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SongViewModel by activityViewModels()
    private lateinit var adapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SongAdapter(
            onItemClick = { song -> navigateToForm(song.id) },
            onDeleteClick = { song -> confirmDelete(song) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Restaurar posición del switch según estado del ViewModel
        binding.switchStorage.isChecked = !viewModel.isUsingSql

        // Observar lista de canciones
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.songs.collect { songs ->
                    adapter.submitList(songs)
                    binding.tvEmpty.visibility = if (songs.isEmpty()) View.VISIBLE else View.GONE
                    Log.d(TAG, "DEBUG — Lista actualizada: ${songs.size} canciones")
                }
            }
        }

        // Observar motor activo
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activeEngine.collect { engine ->
                    updateEngineUI(engine)
                    adapter.engineLabel = engine
                }
            }
        }

        // Switch para alternar motor
        binding.switchStorage.setOnCheckedChangeListener { _, isChecked ->
            viewModel.switchEngine(useSql = !isChecked)
            Log.d(TAG, "DEBUG — Switch cambiado a: ${if (isChecked) "NoSQL" else "SQL"}")
        }

        // FAB para nueva canción
        binding.fabAdd.setOnClickListener {
            navigateToForm(null)
        }

        Log.i(TAG, "INFO — SongListFragment iniciado")
    }

    private fun updateEngineUI(engine: String) {
        if (engine == "SQL") {
            binding.chipStorageMode.text = "SQLite (Relacional)"
            binding.chipStorageMode.setChipBackgroundColorResource(android.R.color.holo_green_dark)
            binding.tvListLabel.text = "Canciones – SQLite"
        } else {
            binding.chipStorageMode.text = "NoSQL (JSON)"
            binding.chipStorageMode.setChipBackgroundColorResource(android.R.color.holo_blue_dark)
            binding.tvListLabel.text = "Canciones – NoSQL"
        }
    }

    private fun confirmDelete(song: Song) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar canción")
            .setMessage("¿Eliminar \"${song.title}\"?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.delete(song.id)
                Log.i(TAG, "INFO — Usuario confirmó eliminar: ${song.title}")
            }
            .show()
    }

    private fun navigateToForm(songId: Long?) {
        val fragment = SongFormFragment().apply {
            arguments = Bundle().apply {
                if (songId != null) putLong("song_id", songId)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}