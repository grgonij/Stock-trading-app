package com.example.projekt.ui.screens

import android.content.Context
import android.content.Intent
import android.os.LocaleList
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projekt.MainActivity
import com.example.projekt.R
import java.util.Locale

private val BgDark      = Color(0xFF0D1117)
private val Surface1    = Color(0xFF161B22)
private val Surface2    = Color(0xFF21262D)
private val TealAccent  = Color(0xFF1AAB8B)
private val TextPrimary = Color(0xFFE6EDF3)
private val TextMuted   = Color(0xFF8B949E)

private const val PREFS_NAME = "settings"
private const val KEY_LANG   = "language"

private val languages = listOf(
    "hr" to "Hrvatski",
    "en" to "English"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    // Čitaj iz SharedPreferences — ne iz Locale.getDefault()
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    var selectedLang by remember {
        mutableStateOf(prefs.getString(KEY_LANG, "hr") ?: "hr")
    }
    var expanded by remember { mutableStateOf(false) }

    val selectedLabel = languages.find { it.first == selectedLang }?.second ?: "Hrvatski"

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings_title),
                        color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Naslov sekcije
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
            ) {
                Icon(
                    Icons.Default.Language,
                    contentDescription = null,
                    tint     = TealAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.settings_language),
                    color         = TextMuted,
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }

            // Dropdown kartica
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = Surface1),
                border    = androidx.compose.foundation.BorderStroke(0.5.dp, Surface2)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Glavni red koji se klikće
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            selectedLabel,
                            color      = TextPrimary,
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier   = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick  = { expanded = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Odaberi jezik",
                                tint = TextMuted
                            )
                        }
                    }

                    // Dropdown menu
                    DropdownMenu(
                        expanded         = expanded,
                        onDismissRequest = { expanded = false },
                        modifier         = Modifier.fillMaxWidth(0.9f)
                    ) {
                        languages.forEach { (code, label) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        label,
                                        color      = if (selectedLang == code) TealAccent else TextPrimary,
                                        fontWeight = if (selectedLang == code) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    if (selectedLang != code) {
                                        selectedLang = code
                                        // Spremi u SharedPreferences PRIJE restarta
                                        prefs.edit().putString(KEY_LANG, code).apply()
                                        changeLanguage(context, code)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Jezik helper ─────────────────────────────────────────────────────────────

private fun changeLanguage(context: Context, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocales(LocaleList(locale))
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    context.startActivity(intent)
}