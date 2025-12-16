package com.practicum.playlistmaker.media.ui.favorites

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentInfoFavoritesBinding
import com.practicum.playlistmaker.media.domain.playlist.model.Playlist
import java.io.File
import org.koin.androidx.viewmodel.ext.android.viewModel


class FavoritesInfoFragment: Fragment() {

    private var _binding: FragmentInfoFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<FavoritesInfoViewModel>()

    private var selectedImageUri: Uri? = null

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        selectedImageUri = uri

        if (uri != null) {
            Glide.with(requireContext())
                .load(selectedImageUri)
                .centerCrop()
                .into(binding.ibImage)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInfoFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCreate.isEnabled = false

        binding.etName.addTextChangedListener { text ->
            binding.btnCreate.isEnabled = !text.isNullOrBlank()
        }

        binding.ivBackButton.setOnClickListener {
            if(toSavedData()) {
                toShowDialog()
            } else {
                findNavController().navigateUp()
            }
        }

        binding.ibImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnCreate.setOnClickListener {
            savePlaylist()
        }
    }

    private fun savePlaylist() {
        val name = binding.etName.text.toString().trim()
        val description = binding.etDescription.text?.toString()?.trim().orEmpty()

        val imagePath = selectedImageUri?.let {
            copyImage(it, name.replace(" ", "_"))
        }

        val playlist = Playlist(
            id = 0,
            name = name,
            description = description,
            filePath = imagePath,
            trackId = emptyList(),
            trackCount = 0
        )

        viewModel.createPlaylist(playlist)

        findNavController().navigateUp()
        Toast.makeText(requireContext(), R.string.playlist_created, Toast.LENGTH_SHORT).show()
    }

    private fun copyImage(uri: Uri, fileName: String): String? {
        return try {
            val file = File(requireContext().filesDir, "playlist_images")
            if (!file.exists()) file.mkdirs()

            val newFile = File(file, "$fileName${System.currentTimeMillis()}.jpg")

            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                newFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            newFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun toSavedData(): Boolean {
        val nameNotEmpty = !binding.etName.text.isNullOrEmpty()
        val descriptionNotEmpty = !binding.etDescription.text.isNullOrEmpty()
        val imageNotEmpty = selectedImageUri != null

        return nameNotEmpty || descriptionNotEmpty || imageNotEmpty
    }

    private fun toShowDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.playlist_complete) // Заголовок диалога
            .setMessage(R.string.playlist_lost) // Описание диалога
            .setNeutralButton(R.string.playlist_cancel) { dialog, which -> // Добавляет кнопку «Отмена»
                // Действия, выполняемые при нажатии на кнопку «Отмена»
            }
            .setPositiveButton(R.string.playlist_complete_btn) { dialog, which -> // Добавляет кнопку «Да»
                hideKeyboard()
                findNavController().navigateUp() // Действия, выполняемые при нажатии на кнопку «Да»
            }
            .show()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus ?: View(requireContext())
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}