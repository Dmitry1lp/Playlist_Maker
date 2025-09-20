package com.practicum.playlistmaker.main.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.search.data.storage.PrefsStorageClient
import com.practicum.playlistmaker.settings.ui.SettingsViewModel
import com.practicum.playlistmaker.settings.ui.App

class SettingsActivity : AppCompatActivity() {

    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        val backButton = findViewById<MaterialToolbar>(R.id.toolbarSetting)
        val shareIcon = findViewById<ImageView>(R.id.shareButton)
        val helpIcon = findViewById<ImageView>(R.id.helpButton)
        val agreementIcon = findViewById<ImageView>(R.id.agreementButton)
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.settingSwitch)

        viewModel = ViewModelProvider(this, SettingsViewModel.getFactory(Creator.provideSettingsInteractor(this)))
            .get(SettingsViewModel::class.java)

        viewModel.isDarkTheme.observe(this) { isDark ->
            themeSwitcher.isChecked = isDark
        }

        viewModel.themeChangedEvent.observe(this) { isDark ->
            (application as App).switchTheme(isDark)
        }

        themeSwitcher.setOnCheckedChangeListener { _, checked ->
            viewModel.switchTheme(checked)
        }

        viewModel?.observeHelpLiveData()?.observe(this) { email ->
            val helpIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mailTe))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.mailMessage))
            }
            startActivity(helpIntent)
        }

        viewModel?.observeSharedLiveData()?.observe(this) { text ->
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, text)
            startActivity(shareIntent)
        }

        viewModel?.observeAgreementLiveData()?.observe(this) { url ->
            val agreeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(agreeIntent)
        }

        backButton.setNavigationOnClickListener {
            finish()
        }

        shareIcon.setOnClickListener { viewModel.onShareClicked() }
        helpIcon.setOnClickListener { viewModel.onHelpClicked() }
        agreementIcon.setOnClickListener { viewModel.onAgreementClicked() }
    }
}