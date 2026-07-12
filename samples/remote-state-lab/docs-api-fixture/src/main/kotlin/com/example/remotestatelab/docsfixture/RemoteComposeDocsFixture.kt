package com.example.remotestatelab.docsfixture

import android.content.Context
import androidx.compose.remote.creation.compose.action.hostAction
import androidx.compose.remote.creation.compose.action.valueChange
import androidx.compose.remote.creation.compose.capture.RemoteCreationDisplayInfo
import androidx.compose.remote.creation.compose.capture.RemoteDensityBehavior
import androidx.compose.remote.creation.compose.capture.captureSingleRemoteDocument
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.layout.RemoteStateLayout
import androidx.compose.remote.creation.compose.layout.RemoteText
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.clickable
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.padding
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteBoolean
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.remote.creation.compose.state.rsp
import androidx.compose.runtime.Composable

/** Compile fixture for the public Compose-creation snippets documented in the wiki. */
@Composable
@RemoteComposable
public fun ChecklistContent() {
  val done = rememberMutableRemoteBoolean(false)

  RemoteColumn(
    modifier = RemoteModifier.fillMaxSize().padding(24.rdp),
  ) {
    RemoteText("배포 체크리스트".rs, fontSize = 24.rsp)
    RemoteStateLayout(currentState = done) { isDone ->
      RemoteText(
        text = if (isDone) "완료".rs else "미완료".rs,
        fontSize = 18.rsp,
        modifier = RemoteModifier.clickable(valueChange(done, (!isDone).rb)),
      )
    }
    RemoteText(
      text = "새 작업 작성".rs,
      modifier = RemoteModifier.clickable(hostAction("task.create".rs)),
    )
  }
}

public suspend fun captureChecklist(context: Context): ByteArray {
  val displayInfo =
    RemoteCreationDisplayInfo(
      width = 1080,
      height = 1920,
      densityDpi = 420,
      densityBehavior = RemoteDensityBehavior.Dp,
    )

  return captureSingleRemoteDocument(
    context = context,
    creationDisplayInfo = displayInfo,
  ) {
    ChecklistContent()
  }.bytes
}
