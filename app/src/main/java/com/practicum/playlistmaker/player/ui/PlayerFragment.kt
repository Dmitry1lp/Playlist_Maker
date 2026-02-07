package com.practicum.playlistmaker.player.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentAudioplayerBinding
import com.practicum.playlistmaker.media.ui.media.MediaAdapter
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.utils.BroadcastReceiverConnection
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlayerFragment: Fragment() {

    private var _binding: FragmentAudioplayerBinding? = null
    private val binding get() = _binding!!
    private val connectionReceiver = BroadcastReceiverConnection()

    private lateinit var adapterBottomSheet: MediaAdapter

    private var playerService: PlayerService? = null

    private var playerState: PlayerState = PlayerState.Default()

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PlayerService.PlayerServiceBinder
            playerService = binder.getService()
            viewModel.setAudioPlayerControl(binder.getService())
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            viewModel.removeAudioPlayerControl()
        }
    }

    private val track: Track by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable(KEY_TRACK, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            requireArguments().getParcelable<Track>(KEY_TRACK)
        } ?: throw IllegalArgumentException("Track is null")
    }

    private val viewModel: PlayerViewModel by viewModel {
        parametersOf(track)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAudioplayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // На версиях ниже Android 13 —
            // можно сразу стартовать сервис.
            startPlayerService()
        }

        bindPlayerService()

        val analytics = FirebaseAnalytics.getInstance(requireContext())

        adapterBottomSheet = MediaAdapter(playlists = emptyList()) { playlist ->
            viewModel.onAddTrackToPlaylistClicked(playlist)
        }

        binding.rvAudio.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAudio.adapter = adapterBottomSheet

        val bottomSheetContainer = binding.standartBottomSheet
        val overlay = binding.overlay

        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        binding.ibAddButton.setOnClickListener {
            analytics.logEvent("Test_event") {
                param("TestValue", 123)
                param("Name", "Cool")
            }
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {

                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        overlay.visibility = View.GONE
                    }
                    else -> {
                        overlay.visibility = View.VISIBLE
                    }
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        viewModel.observePlayerUiState().observe(viewLifecycleOwner) { state ->
            binding.ibLikeButton.setImageResource(
                if(state.isLiked) R.drawable.like_red_button else R.drawable.like_button
            )
        }

        viewModel.observePlayerBottomSheetUiState.observe(viewLifecycleOwner) { state ->
            when(state) {
                is PlayerBottomSheetUiState.Content -> {
                    binding.rvAudio.visibility = View.VISIBLE
                    adapterBottomSheet.update(state.playlists)
                }
                is PlayerBottomSheetUiState.Empty -> {
                    binding.rvAudio.visibility = View.GONE
                }
            }
        }

        viewModel.observePlayerState().observe(viewLifecycleOwner) { state ->
            binding.tvTimeIndicator.text = state.progress

            // Меняем кнопку в зависимости от состояния
            binding.ibPlayButton.setPlaying(
                state is PlayerState.Playing
            )

            binding.ibPlayButton.isEnabled = state.isPlayButtonEnabled
        }

        viewModel.observeAddTrack.observe(viewLifecycleOwner) { result ->
            when(result) {
                is AddTrack.Added -> {
                    Toast.makeText(requireContext(), "Добавлено в плейлист", Toast.LENGTH_SHORT).show()
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN }
                is AddTrack.NotAdded -> {
                    Toast.makeText(requireContext(), "Трек уже есть в плейлисте", Toast.LENGTH_SHORT).show() }
            }
        }

        binding.mbNewPlaylist.setOnClickListener {
            findNavController().navigate(
                R.id.action_playerFragment_to_favoritesInfoFragment
            )
        }

        binding.ivBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
        // Обработка нажатия кнопки Play
        binding.ibPlayButton.onButtonClick = {
            viewModel.onPlayButtonClicked()
        }
        //Обработка нажатия кнопки Like
        binding.ibLikeButton.setOnClickListener {
            viewModel.onFavoriteClicked()
        }

        track.let {
            Glide.with(this)
                .load(it.artworkUrl100?.replace("100x100bb.jpg", "512x512bb.jpg"))
                .placeholder(R.drawable.ic_placeholder)
                .centerCrop()
                .into(binding.ivAlbum)

            binding.tvTrackName.text = it.trackName
            binding.tvGroupName.text = it.artistName
            binding.tvDuration.text = it.trackTime
            binding.tvAlbum.text = it.collectionName ?: ""
            binding.tvYear.text = it.releaseDate
            binding.tvGenre.text = it.primaryGenreName
            binding.tvCountry.text = it.country
        }
    }

    override fun onResume() {
        super.onResume()

        ContextCompat.registerReceiver(
            requireContext(),
            connectionReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        unbindPlayerService()
        super.onDestroy()
    }

    private fun bindPlayerService() {
        val intent = Intent(requireContext(), PlayerService::class.java)

        requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindPlayerService() {
        requireContext().unbindService(serviceConnection)
    }

    // Описали обработчик разрешения
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Если выдали разрешение — запускаем сервис.
            startPlayerService()
        } else {
            // Иначе просто покажем ошибку
            Toast.makeText(requireContext(), "Can't start foreground service!", Toast.LENGTH_LONG).show()
        }
    }

    private fun startPlayerService() {
        val intent = Intent(requireContext(), PlayerService::class.java)
        ContextCompat.startForegroundService(requireContext().applicationContext, intent)
    }

    companion object {
        private const val KEY_TRACK = "KEY_TRACK"

        fun createArgs(track: Track): Bundle =
            Bundle().apply {
                putParcelable(KEY_TRACK, track)
            }
    }
}