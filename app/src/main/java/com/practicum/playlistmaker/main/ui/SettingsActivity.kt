package com.practicum.playlistmaker.main.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.settings.ui.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    private val viewModel by viewModel<SettingsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById<android.view.View>(android.R.id.content)) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBar.top)
            insets
        }

        val backButton = findViewById<MaterialToolbar>(R.id.toolbarSetting)
        val shareIcon = findViewById<ImageView>(R.id.shareButton)
        val helpIcon = findViewById<ImageView>(R.id.helpButton)
        val agreementIcon = findViewById<ImageView>(R.id.agreementButton)
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.settingSwitch)

        viewModel.isDarkTheme.observe(this) { isDark ->
            themeSwitcher.isChecked = isDark
            AppCompatDelegate.setDefaultNightMode(
                if (isDark) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        themeSwitcher.setOnCheckedChangeListener { _, checked ->
            viewModel.switchTheme(checked)
        }

        viewModel.observeHelpLiveData().observe(this) { email ->
            val helpIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mailTe))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.mailMessage))
            }
            startActivity(helpIntent)
        }

        viewModel.observeSharedLiveData().observe(this) { text ->
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, text)
            startActivity(shareIntent)
        }

        viewModel.observeAgreementLiveData().observe(this) { url ->
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