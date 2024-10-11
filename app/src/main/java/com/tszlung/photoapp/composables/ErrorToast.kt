package com.tszlung.photoapp.composables

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun ErrorToast(message: String?) {
    if (message != null) {
        Toast.makeText(LocalContext.current, message, Toast.LENGTH_LONG).show()
    }
}