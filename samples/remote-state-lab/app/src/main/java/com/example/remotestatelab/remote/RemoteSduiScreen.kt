@file:Suppress("RestrictedApi")
@file:SuppressLint("RestrictedApi")

package com.example.remotestatelab.remote

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.remote.player.compose.RemoteDocumentPlayer
import androidx.compose.remote.player.core.RemoteDocument
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF0B1016)
private val Panel = Color(0xFF17212B)
private val Lime = Color(0xFFD5F36B)
private val Coral = Color(0xFFFF8F70)
private val Mist = Color(0xFFB8C5CE)

@Composable
fun RemoteSduiScreen(
  state: RemoteSduiState,
  onServerUrlChange: (String) -> Unit,
  onConnect: () -> Unit,
  onDisconnect: () -> Unit,
  onHostAction: (String, Any?) -> Unit,
  onCreateTask: (String) -> Unit,
  onDismissTaskEditor: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize()) {
    val bytes = state.documentBytes
    if (state.connected && bytes != null) {
      BackHandler(onBack = onDisconnect)
      FullscreenRemoteDocument(
        bytes = bytes,
        loading = state.loading,
        error = state.error,
        onHostAction = onHostAction,
        modifier = Modifier.fillMaxSize(),
      )
    } else {
      ServerConnectionScreen(
        state = state,
        onServerUrlChange = onServerUrlChange,
        onConnect = onConnect,
        modifier = Modifier.fillMaxSize(),
      )
    }
  }

  if (state.taskEditorVisible) {
    TaskEditorDialog(
      error = state.taskEditorError,
      onSubmit = onCreateTask,
      onDismiss = onDismissTaskEditor,
    )
  }
}

@Composable
private fun ServerConnectionScreen(
  state: RemoteSduiState,
  onServerUrlChange: (String) -> Unit,
  onConnect: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(modifier = modifier.fillMaxSize(), color = Ink) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
          .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
      Text(
        text = "REMOTE COMPOSE LAB",
        color = Lime,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
      )

      Spacer(Modifier.weight(1f))

      Text(
        text = "서버 문서에\n연결합니다",
        color = Color.White,
        fontSize = 38.sp,
        lineHeight = 46.sp,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "Ktor 서버가 생성한 Remote Compose 화면을 앱 전체 화면에서 확인합니다.",
        modifier = Modifier.padding(top = 16.dp),
        color = Mist,
        fontSize = 19.sp,
        lineHeight = 29.sp,
      )

      OutlinedTextField(
        value = state.serverUrl,
        onValueChange = onServerUrlChange,
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        singleLine = true,
        textStyle = TextStyle(fontSize = 18.sp, color = Color.White),
        label = { Text("Ktor 서버 주소", fontSize = 16.sp) },
        supportingText = {
          Text("에뮬레이터 기본값 · 10.0.2.2:8080", fontSize = 15.sp, color = Mist)
        },
      )

      if (state.error != null) {
        Text(
          text = state.error,
          modifier = Modifier.padding(top = 12.dp),
          color = Coral,
          fontSize = 16.sp,
          lineHeight = 23.sp,
        )
      }

      Button(
        onClick = onConnect,
        enabled = !state.loading,
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(64.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Ink),
      ) {
        if (state.loading) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = Ink,
            strokeWidth = 3.dp,
          )
        } else {
          Text("서버 연결", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(Modifier.weight(1f))

      Row(
        modifier =
          Modifier.fillMaxWidth()
            .border(1.dp, Panel, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Android 앱 내부 SDUI", color = Mist, fontSize = 15.sp)
        Text("alpha14 POC", color = Coral, fontSize = 15.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
private fun FullscreenRemoteDocument(
  bytes: ByteArray,
  loading: Boolean,
  error: String?,
  onHostAction: (String, Any?) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(modifier = modifier.fillMaxSize(), color = Ink) {
    Box(
      modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
      contentAlignment = Alignment.Center,
    ) {
      RemotePlayer(
        bytes = bytes,
        onHostAction = onHostAction,
        modifier = Modifier.fillMaxSize(),
      )

      if (loading) {
        Box(
          modifier = Modifier.fillMaxSize().background(Ink.copy(alpha = 0.76f)),
          contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator(color = Lime, strokeWidth = 4.dp)
        }
      } else if (error != null) {
        Text(
          text = error,
          modifier =
            Modifier.align(Alignment.TopCenter)
              .padding(16.dp)
              .background(Coral, RoundedCornerShape(14.dp))
              .padding(horizontal = 18.dp, vertical = 14.dp),
          color = Ink,
          fontSize = 16.sp,
          lineHeight = 22.sp,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@Composable
private fun TaskEditorDialog(
  error: String?,
  onSubmit: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  var title by rememberSaveable { mutableStateOf("") }
  val canSubmit = title.trim().isNotEmpty() && title.length <= 80
  val submit = {
    if (canSubmit) {
      onSubmit(title)
      title = ""
    }
  }

  AlertDialog(
    onDismissRequest = {
      title = ""
      onDismiss()
    },
    containerColor = Panel,
    titleContentColor = Color.White,
    textContentColor = Mist,
    shape = RoundedCornerShape(22.dp),
    title = {
      Text("새 작업 작성", fontSize = 25.sp, fontWeight = FontWeight.Bold)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          "여기서 입력한 이름은 Ktor 서버에 저장되고 새 Remote Compose 문서에 추가됩니다.",
          fontSize = 16.sp,
          lineHeight = 23.sp,
        )
        OutlinedTextField(
          value = title,
          onValueChange = { if (it.length <= 80) title = it },
          modifier = Modifier.fillMaxWidth(),
          label = { Text("작업 이름") },
          placeholder = { Text("예: QA 체크리스트 공유") },
          supportingText = {
            Text(error ?: "${title.length}/80 · 입력은 Android TextField가 담당")
          },
          isError = error != null,
          singleLine = true,
          textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(onDone = { submit() }),
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = submit,
        enabled = canSubmit,
        colors = ButtonDefaults.textButtonColors(contentColor = Lime),
      ) {
        Text("서버에 추가", fontSize = 17.sp, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          title = ""
          onDismiss()
        }
      ) {
        Text("취소", color = Mist, fontSize = 17.sp)
      }
    },
  )
}

@Composable
private fun RemotePlayer(
  bytes: ByteArray,
  onHostAction: (String, Any?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val parsed = remember(bytes) { runCatching { RemoteDocument(bytes).document } }
  val document = parsed.getOrNull()
  if (document == null) {
    RemoteDocumentError(parsed.exceptionOrNull()?.message ?: "Remote Compose 문서를 해석하지 못했습니다.")
    return
  }

  RemoteDocumentPlayer(
    document = document,
    documentWidth = 390,
    documentHeight = 720,
    modifier = modifier,
    init = { player ->
      player.setMaxOpCount(20_000)
      player.setMaxImageDimension(2_048)
      player.setMaxBitmapMemory(8 * 1024 * 1024)
    },
    onNamedAction = { name, value, _ -> onHostAction(name, value) },
  )
}

@Composable
private fun RemoteDocumentError(error: String) {
  Column(
    modifier = Modifier.padding(28.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text("문서를 표시할 수 없습니다", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Text(error, color = Coral, fontSize = 17.sp, lineHeight = 25.sp)
  }
}
