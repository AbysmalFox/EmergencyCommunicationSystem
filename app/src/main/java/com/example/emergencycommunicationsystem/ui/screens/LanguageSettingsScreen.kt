package com.example.emergencycommunicationsystem.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.emergencycommunicationsystem.R

@Composable
fun LanguageSettingsScreen(
    currentLanguage: String,
    onConfirm: (String) -> Unit,
    onBackPressed: () -> Unit
) {
    var selectedLanguage by remember { mutableStateOf(currentLanguage) }
    val languages = listOf(
        "en" to stringResource(R.string.language_english),
        "fil" to stringResource(R.string.language_filipino),
        "es" to stringResource(R.string.language_spanish)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.language_settings_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(48.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            languages.forEach { (code, name) ->
                LanguageOption(
                    text = name,
                    isSelected = selectedLanguage == code,
                    onClick = { selectedLanguage = code }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBackPressed,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50)
            ) {
                Text(stringResource(R.string.language_settings_back))
            }
            Button(
                onClick = { onConfirm(selectedLanguage) },
                modifier = Modifier.weight(1f),
                enabled = selectedLanguage != currentLanguage, // Disable if language is not changed
                shape = RoundedCornerShape(50)
            ) {
                Text(stringResource(R.string.language_settings_confirm))
            }
        }
    }
}

@Composable
private fun LanguageOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .clip(shape)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onBackground
        )
    }
}
