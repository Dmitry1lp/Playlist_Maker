package com.practicum.playlistmaker.settings.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.practicum.playlistmaker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {

    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle(false)
    val context = LocalContext.current
    val subject = stringResource(R.string.mailTe)
    val message = stringResource(R.string.mailMessage)

    // шрифты
    val ysDisplay = FontFamily(Font(R.font.ys_display_regular, FontWeight.Normal))
    val ysDisplayMedium = FontFamily(Font(R.font.ys_display_medium, FontWeight.Normal))

    val colors = if (isDark) DarkColors else LightColors

    MaterialTheme(colorScheme = colors) {


        @Composable
        fun SettingItem(
            text: Int,
            iconRes: Int,
            onClick: () -> Unit
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(text),
                    modifier = Modifier.weight(1f),
                    fontFamily = ysDisplay,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.setting),
                            fontFamily = ysDisplayMedium,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isDark) BlackLight else White,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    windowInsets = WindowInsets(0.dp),
                )
            },
            content = { paddingValues ->
                Column(modifier = Modifier.padding(paddingValues)) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.dark_theme),
                            modifier = Modifier.weight(1f),
                            fontFamily = ysDisplay,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Switch(
                            checked = isDark,
                            onCheckedChange = { viewModel.switchTheme(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.tertiary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }

                    SettingItem(text = R.string.share, iconRes = R.drawable.share) {
                        viewModel.onShareClicked()
                    }
                    SettingItem(text = R.string.support, iconRes = R.drawable.support) {
                        viewModel.onHelpClicked()
                    }
                    SettingItem(text = R.string.agreement, iconRes = R.drawable.agreement) {
                        viewModel.onAgreementClicked()
                    }
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.shareEvent.collect { text ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(intent)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.agreementEvent.collect { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.helpEvent.collect { email ->
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, message)
            }
            context.startActivity(intent)
        }
    }
}