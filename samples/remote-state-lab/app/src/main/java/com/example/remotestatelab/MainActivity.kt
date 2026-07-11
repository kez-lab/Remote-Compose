package com.example.remotestatelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.remotestatelab.remote.RemoteSduiScreen
import com.example.remotestatelab.remote.RemoteSduiViewModel
import com.example.remotestatelab.theme.RemoteStateLabTheme

class MainActivity : ComponentActivity() {
  private val viewModel: RemoteSduiViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      RemoteStateLabTheme(dynamicColor = false, darkTheme = true) {
        val state by viewModel.state.collectAsStateWithLifecycle()
        RemoteSduiScreen(
          state = state,
          onServerUrlChange = viewModel::setServerUrl,
          onConnect = viewModel::loadDocument,
          onDisconnect = viewModel::disconnect,
          onHostAction = viewModel::handleHostAction,
          onCreateTask = viewModel::createTask,
          onDismissTaskEditor = viewModel::dismissTaskEditor,
        )
      }
    }
  }
}
