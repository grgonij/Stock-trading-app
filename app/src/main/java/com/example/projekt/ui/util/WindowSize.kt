package com.example.projekt.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Jednostavan helper koji vraća true kad je uređaj u landscape modu.
 * Koristi se u svim ekranima za prilagodbu layouta.
 */
@Composable
fun isLandscape(): Boolean {
    val config = LocalConfiguration.current
    return config.screenWidthDp > config.screenHeightDp
}