package com.practicum.playlistmaker.presentation.setting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.practicum.playlistmaker.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        val backButton = findViewById<MaterialToolbar>(R.id.toolbarSetting)
        val shareIcon = findViewById<ImageView>(R.id.shareButton)
        val helpIcon = findViewById<ImageView>(R.id.helpButton)
        val agreementIcon = findViewById<ImageView>(R.id.agreementButton)

        shareIcon.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.message))
            startActivity(shareIntent)
        }

        helpIcon.setOnClickListener {
            val helpIntent = Intent(Intent.ACTION_SENDTO)
            helpIntent.data = Uri.parse("mailto:")
            helpIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("perovdv1@ya.ru"))
            helpIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mailTe))
            helpIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.mailMessage))
            startActivity(helpIntent)
        }

        agreementIcon.setOnClickListener {
            val url = getString(R.string.offer_url)
            val agreeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(agreeIntent)
        }

        backButton.setNavigationOnClickListener {
            finish()
        }
    }
}