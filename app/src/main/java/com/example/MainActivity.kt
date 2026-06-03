package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.TranslationViewModel
import com.example.ui.TranslationViewModelFactory
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Create the ViewModel utilizing our custom factory
    val factory = TranslationViewModelFactory(application)
    val viewModel = ViewModelProvider(this, factory)[TranslationViewModel::class.java]

    setContent {
      MyApplicationTheme(darkTheme = viewModel.isDarkMode.value) {
        MainAppScreen(viewModel = viewModel)
      }
    }
  }
}

