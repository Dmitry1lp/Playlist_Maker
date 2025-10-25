package com.practicum.playlistmaker.settings.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentSettingsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment: Fragment() {

    private val viewModel by viewModel<SettingsViewModel>()
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isDarkTheme.observe(viewLifecycleOwner) { isDark ->
            binding.settingSwitch.isChecked = isDark
            AppCompatDelegate.setDefaultNightMode(
                if (isDark) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.settingSwitch.setOnCheckedChangeListener { _, checked ->
            viewModel.switchTheme(checked)
        }

        viewModel.observeHelpLiveData().observe(viewLifecycleOwner) { email ->
            val helpIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mailTe))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.mailMessage))
            }
            startActivity(helpIntent)
        }

        viewModel.observeSharedLiveData().observe(viewLifecycleOwner) { text ->
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, text)
            startActivity(shareIntent)
        }

        viewModel.observeAgreementLiveData().observe(viewLifecycleOwner) { url ->
            val agreeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(agreeIntent)
        }

        binding.toolbarSetting.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.shareButton.setOnClickListener { viewModel.onShareClicked() }
        binding.helpButton.setOnClickListener { viewModel.onHelpClicked() }
        binding.agreementButton.setOnClickListener { viewModel.onAgreementClicked() }

    }
}
