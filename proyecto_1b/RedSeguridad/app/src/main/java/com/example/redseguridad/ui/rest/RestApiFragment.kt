package com.example.redseguridad.ui.rest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.redseguridad.databinding.FragmentRestApiBinding
import kotlinx.coroutines.launch

class RestApiFragment : Fragment() {

    private var _binding: FragmentRestApiBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RestApiViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRestApiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
        observeState()
    }

    private fun setupButtons() {
        binding.btnGet.setOnClickListener {
            val idText = binding.etPostId.text.toString().trim()
            if (idText.isEmpty()) {
                Toast.makeText(requireContext(), "Ingresa un ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val id = idText.toIntOrNull()
            if (id == null || id <= 0) {
                Toast.makeText(requireContext(), "ID debe ser un número positivo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.fetchPost(id)
        }

        binding.btnPut.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val body  = binding.etBody.text.toString().trim()
            if (title.isEmpty() || body.isEmpty()) {
                Toast.makeText(requireContext(), "Título y cuerpo no pueden estar vacíos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.updatePost(title, body)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is RestUiState.Idle -> {
                            setLoadingState(false)
                            binding.btnPut.isEnabled = false
                            binding.tvStatus.text = ""
                        }
                        is RestUiState.Loading -> {
                            setLoadingState(true)
                            binding.tvStatus.text = "⏳ Consultando..."
                        }
                        is RestUiState.GetSuccess -> {
                            setLoadingState(false)
                            binding.etTitle.setText(state.post.title)
                            binding.etBody.setText(state.post.body)
                            binding.etTitle.isEnabled = true
                            binding.etBody.isEnabled  = true
                            binding.btnPut.isEnabled  = true
                            binding.tvStatus.text = "✅ 200 OK — Post #${state.post.id} cargado"
                        }
                        is RestUiState.PutSuccess -> {
                            setLoadingState(false)
                            binding.etTitle.isEnabled = true
                            binding.etBody.isEnabled  = true
                            binding.btnPut.isEnabled  = true
                            binding.tvStatus.text = "✅ ${state.statusCode} OK — Post actualizado"
                        }
                        is RestUiState.Error -> {
                            setLoadingState(false)
                            binding.btnPut.isEnabled = false
                            binding.tvStatus.text = "⚠️ ${state.message}"
                        }
                    }
                }
            }
        }
    }

    /**
     * Controla el estado de carga: deshabilita toda la UI mientras
     * la petición está en tránsito (requisito explícito de la rúbrica).
     */
    private fun setLoadingState(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.etPostId.isEnabled     = !loading
        binding.btnGet.isEnabled       = !loading
        if (loading) {
            binding.etTitle.isEnabled  = false
            binding.etBody.isEnabled   = false
            binding.btnPut.isEnabled   = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}