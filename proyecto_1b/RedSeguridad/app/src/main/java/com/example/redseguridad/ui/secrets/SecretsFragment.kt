package com.example.redseguridad.ui.secrets

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
import com.example.redseguridad.data.model.StorageEngine
import com.example.redseguridad.databinding.FragmentSecretsBinding
import kotlinx.coroutines.launch

class SecretsFragment : Fragment() {

    private var _binding: FragmentSecretsBinding? = null
    private val binding get() = _binding!!

    // AndroidViewModel: el delegado by viewModels() le pasa la Application automáticamente
    private val viewModel: SecretsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecretsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
        observeState()
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            val key    = binding.etKey.text.toString().trim()
            val value  = binding.etValue.text.toString().trim()
            val engine = selectedEngine()

            if (key.isEmpty()) {
                Toast.makeText(requireContext(), "La Llave no puede estar vacía", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (value.isEmpty()) {
                Toast.makeText(requireContext(), "El Valor no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveSecret(key, value, engine)
        }

        binding.btnRetrieve.setOnClickListener {
            val key    = binding.etKey.text.toString().trim()
            val engine = selectedEngine()

            if (key.isEmpty()) {
                Toast.makeText(requireContext(), "Ingresa una Llave para recuperar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.retrieveSecret(key, engine)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {

                        is SecretsUiState.Idle -> {
                            binding.cardResult.visibility = View.GONE
                        }

                        is SecretsUiState.SaveSuccess -> {
                            binding.cardResult.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "✅ Guardado en ${state.engine}",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Reset evita que el Toast se repita en rotación
                            viewModel.resetState()
                        }

                        is SecretsUiState.RetrieveSuccess -> {
                            binding.tvResult.text =
                                "Motor:  ${state.engine}\n" +
                                        "Llave:  ${state.key}\n" +
                                        "Valor:  ${state.value}"
                            binding.cardResult.visibility = View.VISIBLE
                        }

                        is SecretsUiState.Error -> {
                            binding.cardResult.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                state.message,
                                Toast.LENGTH_LONG
                            ).show()
                            viewModel.resetState()
                        }
                    }
                }
            }
        }
    }

    /**
     * Mapea la posición del Spinner al enum StorageEngine.
     * El orden en strings.xml → storage_engines coincide con el ordinal del enum:
     *   0 → SHARED_PREFS | 1 → DATA_STORE | 2 → ENCRYPTED_SHARED_PREFS
     */
    private fun selectedEngine(): StorageEngine =
        StorageEngine.values()[binding.spinnerEngine.selectedItemPosition]

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}