@file:Suppress("RestrictedApi")

package com.example.remotestatelab.remote

import androidx.compose.remote.player.core.RemoteDocument
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DEFAULT_SERVER_URL = "http://10.0.2.2:8080"
private const val MAX_DOCUMENT_BYTES = 512 * 1024
private const val MAX_TASK_TITLE_LENGTH = 80

data class RemoteSduiState(
  val serverUrl: String = DEFAULT_SERVER_URL,
  val documentBytes: ByteArray? = null,
  val loading: Boolean = false,
  val connected: Boolean = false,
  val taskEditorVisible: Boolean = false,
  val taskEditorError: String? = null,
  val lastAction: String? = null,
  val error: String? = null,
)

class RemoteSduiViewModel : ViewModel() {
  private val client =
    HttpClient(OkHttp) {
      install(HttpTimeout) {
        connectTimeoutMillis = 4_000
        requestTimeoutMillis = 8_000
        socketTimeoutMillis = 8_000
      }
      expectSuccess = false
    }

  private val mutableState = MutableStateFlow(RemoteSduiState())
  val state: StateFlow<RemoteSduiState> = mutableState.asStateFlow()

  fun setServerUrl(value: String) {
    mutableState.update { it.copy(serverUrl = value, error = null) }
  }

  fun loadDocument() {
    viewModelScope.launch { fetchDocument() }
  }

  fun disconnect() {
    mutableState.update {
      it.copy(
        documentBytes = null,
        loading = false,
        connected = false,
        taskEditorVisible = false,
        taskEditorError = null,
        lastAction = null,
        error = null,
      )
    }
  }

  fun dismissTaskEditor() {
    mutableState.update { it.copy(taskEditorVisible = false, taskEditorError = null) }
  }

  fun createTask(rawTitle: String) {
    val title = rawTitle.trim()
    val validationError =
      when {
        title.isEmpty() -> "작업 이름을 입력해 주세요."
        title.length > MAX_TASK_TITLE_LENGTH -> "작업 이름은 ${MAX_TASK_TITLE_LENGTH}자 이하여야 합니다."
        else -> null
      }
    if (validationError != null) {
      mutableState.update { it.copy(taskEditorError = validationError) }
      return
    }

    viewModelScope.launch {
      mutableState.update {
        it.copy(
          loading = true,
          taskEditorVisible = false,
          taskEditorError = null,
          error = null,
          lastAction = "task.create",
        )
      }
      runCatching {
          val response =
            client.post(endpoint("/tasks")) {
              contentType(ContentType.Text.Plain)
              setBody(title)
            }
          check(response.status.isSuccess()) { "/tasks returned ${response.status}" }
          fetchDocument(updateLoading = false)
        }
        .onFailure { failure -> mutationFailed(failure, "작업을 추가하지 못했습니다.") }
    }
  }

  fun handleHostAction(name: String, value: Any?) {
    when (val command = HostActionRouter.commandFor(name)) {
      HostActionCommand.CreateTask -> {
        mutableState.update {
          it.copy(
            taskEditorVisible = true,
            taskEditorError = null,
            error = null,
            lastAction = "$name(${value ?: "no payload"})",
          )
        }
      }
      is HostActionCommand.DeleteTask -> deleteTask(command.id)
      null -> {
        mutableState.update {
          it.copy(error = "허용되지 않은 호스트 액션입니다: $name", lastAction = name)
        }
      }
    }
  }

  private fun deleteTask(id: Int) {
    viewModelScope.launch {
      mutableState.update {
        it.copy(loading = true, error = null, lastAction = "task.delete.$id")
      }
      runCatching {
          val response = client.delete(endpoint("/tasks/$id"))
          check(response.status.isSuccess()) { "/tasks/$id returned ${response.status}" }
          fetchDocument(updateLoading = false)
        }
        .onFailure { failure -> mutationFailed(failure, "작업을 삭제하지 못했습니다.") }
    }
  }

  private fun mutationFailed(failure: Throwable, fallback: String) {
    mutableState.update {
      it.copy(
        loading = false,
        error = failure.message ?: fallback,
        connected = it.documentBytes != null,
      )
    }
  }

  private suspend fun fetchDocument(updateLoading: Boolean = true) {
    if (updateLoading) {
      mutableState.update { it.copy(loading = true, error = null) }
    }

    runCatching {
        val response = client.get(endpoint("/document"))
        check(response.status.isSuccess()) { "/document returned ${response.status}" }
        val bytes = response.bodyAsBytes()
        check(bytes.isNotEmpty()) { "서버가 빈 문서를 반환했습니다." }
        check(bytes.size <= MAX_DOCUMENT_BYTES) {
          "문서가 ${MAX_DOCUMENT_BYTES / 1024} KiB 제한을 초과했습니다."
        }
        RemoteDocument(bytes)
        bytes
      }
      .onSuccess { bytes ->
        mutableState.update {
          it.copy(
            documentBytes = bytes,
            loading = false,
            connected = true,
            error = null,
          )
        }
      }
      .onFailure { failure ->
        mutableState.update {
          it.copy(
            loading = false,
            connected = it.documentBytes != null,
            error = failure.message ?: "Remote Compose 문서를 가져오지 못했습니다.",
          )
        }
      }
  }

  private fun endpoint(path: String): String =
    mutableState.value.serverUrl.trim().trimEnd('/') + path

  override fun onCleared() {
    client.close()
  }
}
