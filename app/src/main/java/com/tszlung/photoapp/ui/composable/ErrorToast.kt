package com.tszlung.photoapp.ui.composable

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun ErrorToast(message: String?, afterShown: () -> Unit) {
    if (message != null) {
        Toast.makeText(LocalContext.current, message, Toast.LENGTH_SHORT).show()
        afterShown()
    }
}