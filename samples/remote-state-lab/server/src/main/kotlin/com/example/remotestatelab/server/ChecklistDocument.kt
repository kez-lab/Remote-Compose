@file:Suppress("RestrictedApi")

package com.example.remotestatelab.server

import androidx.compose.remote.core.CoreDocument
import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.core.RcProfiles
import androidx.compose.remote.core.operations.Header
import androidx.compose.remote.creation.RemoteComposeWriter
import androidx.compose.remote.creation.dsl.Modifier
import androidx.compose.remote.creation.dsl.RcActionScope
import androidx.compose.remote.creation.dsl.RcFloat
import androidx.compose.remote.creation.dsl.RcHorizontalPositioning
import androidx.compose.remote.creation.dsl.RcProfile
import androidx.compose.remote.creation.dsl.RcRowHorizontalPositioning
import androidx.compose.remote.creation.dsl.RcScope
import androidx.compose.remote.creation.dsl.RcSp
import androidx.compose.remote.creation.dsl.RcTextOverflow
import androidx.compose.remote.creation.dsl.RcVerticalPositioning
import androidx.compose.remote.creation.dsl.background
import androidx.compose.remote.creation.dsl.clip
import androidx.compose.remote.creation.dsl.createRcBuffer
import androidx.compose.remote.creation.dsl.fillMaxSize
import androidx.compose.remote.creation.dsl.fillMaxWidth
import androidx.compose.remote.creation.dsl.height
import androidx.compose.remote.creation.dsl.onClick
import androidx.compose.remote.creation.dsl.padding
import androidx.compose.remote.creation.dsl.rsp
import androidx.compose.remote.creation.dsl.verticalScroll
import androidx.compose.remote.creation.dsl.verticalWeight
import androidx.compose.remote.creation.dsl.width
import androidx.compose.remote.creation.modifiers.RoundedRectShape
import androidx.compose.remote.creation.profile.Profile

const val DOCUMENT_WIDTH = 390
const val DOCUMENT_HEIGHT = 720

private data class ChecklistMetrics(
  val eyebrow: RcSp,
  val title: RcSp,
  val body: RcSp,
  val section: RcSp,
  val taskTitle: RcSp,
  val taskDetail: RcSp,
  val action: RcSp,
  val footnote: RcSp,
  val detailTitle: RcSp,
  val rowHeight: RcFloat,
  val addHeight: RcFloat,
  val deleteWidth: RcFloat,
  val deleteHeight: RcFloat,
  val backWidth: RcFloat,
  val backHeight: RcFloat,
)

private val serverProfile =
  Profile(
    CoreDocument.DOCUMENT_API_LEVEL,
    RcProfiles.PROFILE_ANDROIDX,
    RcPlatformServices.None,
  ) { _, profile, _ ->
    RemoteComposeWriter(profile)
  }

private val rcProfile = RcProfile(serverProfile)

/**
 * 서버 snapshot의 현재 task 수만큼 row와 detail state를 생성한다.
 *
 * Remote Compose alpha14에는 TextField/IME component surface가 없으므로 입력은 Android host가
 * 담당한다. 문서는 스크롤 목록, 상세 화면 전환, create/delete host action만 표현한다.
 */
fun buildChecklistDocument(snapshot: ChecklistServerSnapshot): ByteArray =
  createRcBuffer(
    rcProfile,
    RemoteComposeWriter.HTag(Header.DOC_WIDTH, DOCUMENT_WIDTH),
    RemoteComposeWriter.HTag(Header.DOC_HEIGHT, DOCUMENT_HEIGHT),
    RemoteComposeWriter.HTag(
      Header.DOC_DENSITY_BEHAVIOR,
      CoreDocument.DENSITY_BEHAVIOR_DP,
    ),
    RemoteComposeWriter.HTag(
      Header.DOC_CONTENT_DESCRIPTION,
      "서버가 생성한 ${snapshot.tasks.size}개 Android 배포 작업 revision ${snapshot.revision}",
    ),
  ) {
    val screen = remoteNamedInteger("screen", 0)
    val density = density()
    val metrics =
      ChecklistMetrics(
        eyebrow = scaledSp(density, 14f),
        title = scaledSp(density, 38f),
        body = scaledSp(density, 17f),
        section = scaledSp(density, 17f),
        taskTitle = scaledSp(density, 18f),
        taskDetail = scaledSp(density, 14f),
        action = scaledSp(density, 17f),
        footnote = scaledSp(density, 14f),
        detailTitle = scaledSp(density, 34f),
        rowHeight = (density * 76f).flush(),
        addHeight = (density * 64f).flush(),
        deleteWidth = (density * 68f).flush(),
        deleteHeight = (density * 48f).flush(),
        backWidth = (density * 112f).flush(),
        backHeight = (density * 52f).flush(),
      )

    StateLayout(stateIndex = screen, modifier = Modifier.fillMaxSize()) {
      TaskListScreen(snapshot = snapshot, screen = screen, metrics = metrics)
      snapshot.tasks.forEach { task ->
        TaskDetailScreen(
          snapshot = snapshot,
          task = task,
          screen = screen,
          metrics = metrics,
        )
      }
    }
  }

private fun RcScope.TaskListScreen(
  snapshot: ChecklistServerSnapshot,
  screen: androidx.compose.remote.creation.dsl.RcInteger,
  metrics: ChecklistMetrics,
) {
  Column(modifier = Modifier.fillMaxSize().background(Ink).padding(18f)) {
    Text("REMOTE TASKS  ·  R${snapshot.revision}", color = Lime, fontSize = metrics.eyebrow)
    Text(
      "배포 작업",
      modifier = Modifier.padding(top = 7f),
      color = White,
      fontSize = metrics.title,
    )
    Text(
      "${snapshot.tasks.size}개 작업 · Ktor 서버에 저장됨",
      modifier = Modifier.padding(top = 4f),
      color = Mist,
      fontSize = metrics.body,
    )
    Text(
      "작업을 선택하면 상세 화면으로 이동합니다",
      modifier = Modifier.padding(top = 15f, bottom = 5f),
      color = Mist,
      fontSize = metrics.section,
    )

    if (snapshot.tasks.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxWidth().verticalWeight(1f),
        horizontal = RcHorizontalPositioning.Center,
        vertical = RcVerticalPositioning.Center,
      ) {
        Text("아직 작업이 없습니다", color = Mist, fontSize = metrics.body)
      }
    } else {
      Column(
        modifier = Modifier.fillMaxWidth().verticalWeight(1f).verticalScroll(),
      ) {
        snapshot.tasks.forEachIndexed { index, task ->
          TaskRow(
            task = task,
            detailScreenIndex = index + 1,
            screen = screen,
            metrics = metrics,
          )
        }
      }
    }

    ActionButton(
      label = "+  새 작업 작성",
      modifier = Modifier.fillMaxWidth().height(metrics.addHeight).padding(top = 10f),
      background = Lime,
      contentColor = Ink,
      fontSize = metrics.action,
    ) {
      hostAction("task.create")
    }
    Text(
      "텍스트 입력은 Android · 목록과 상세는 Remote Compose",
      modifier = Modifier.padding(top = 8f),
      color = Mist,
      fontSize = metrics.footnote,
    )
  }
}

private fun RcScope.TaskRow(
  task: ChecklistTask,
  detailScreenIndex: Int,
  screen: androidx.compose.remote.creation.dsl.RcInteger,
  metrics: ChecklistMetrics,
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .height(metrics.rowHeight)
        .padding(top = 5f)
        .clip(RoundedRectShape(15f, 15f, 15f, 15f))
        .background(Panel)
        .padding(start = 14f, top = 10f, end = 10f, bottom = 10f),
    horizontal = RcRowHorizontalPositioning.SpaceBetween,
    vertical = RcVerticalPositioning.Center,
  ) {
    Column(
      modifier = Modifier.weight(1f).onClick { setValue(screen, detailScreenIndex) },
    ) {
      Text(
        task.title,
        color = White,
        fontSize = metrics.taskTitle,
        overflow = RcTextOverflow.Ellipsis,
        maxLines = 1,
      )
      Text(
        "상세 보기  ·  #${task.id}",
        modifier = Modifier.padding(top = 3f),
        color = Mist,
        fontSize = metrics.taskDetail,
        maxLines = 1,
      )
    }
    ActionButton(
      label = "삭제",
      modifier =
        Modifier.width(metrics.deleteWidth).height(metrics.deleteHeight).padding(start = 8f),
      background = DeepPanel,
      contentColor = Coral,
      fontSize = metrics.action,
    ) {
      hostAction("task.delete.${task.id}")
    }
  }
}

private fun RcScope.TaskDetailScreen(
  snapshot: ChecklistServerSnapshot,
  task: ChecklistTask,
  screen: androidx.compose.remote.creation.dsl.RcInteger,
  metrics: ChecklistMetrics,
) {
  Column(modifier = Modifier.fillMaxSize().background(Ink).padding(18f)) {
    ActionButton(
      label = "←  목록",
      modifier = Modifier.width(metrics.backWidth).height(metrics.backHeight),
      background = Panel,
      fontSize = metrics.action,
    ) {
      setValue(screen, 0)
    }

    Text(
      "TASK DETAIL  ·  #${task.id}",
      modifier = Modifier.padding(top = 30f),
      color = Lime,
      fontSize = metrics.eyebrow,
    )
    Text(
      task.title,
      modifier = Modifier.padding(top = 10f),
      color = White,
      fontSize = metrics.detailTitle,
      overflow = RcTextOverflow.Ellipsis,
      maxLines = 4,
    )
    Text(
      task.detail,
      modifier = Modifier.padding(top = 16f),
      color = Mist,
      fontSize = metrics.body,
      maxLines = 3,
    )

    Column(
      modifier =
        Modifier.fillMaxWidth()
          .padding(top = 28f)
          .clip(RoundedRectShape(18f, 18f, 18f, 18f))
          .background(Panel)
          .padding(18f),
    ) {
      Text("DOCUMENT", color = Mist, fontSize = metrics.eyebrow)
      Text(
        "서버 revision R${snapshot.revision}",
        modifier = Modifier.padding(top = 8f),
        color = White,
        fontSize = metrics.body,
      )
      Text(
        "이 화면 전환은 Remote Compose StateLayout 내부에서 실행됩니다.",
        modifier = Modifier.padding(top = 8f),
        color = Mist,
        fontSize = metrics.taskDetail,
        maxLines = 3,
      )
    }

    Box(modifier = Modifier.fillMaxWidth().verticalWeight(1f)) {}

    ActionButton(
      label = "이 작업 삭제",
      modifier = Modifier.fillMaxWidth().height(metrics.addHeight),
      background = Coral,
      contentColor = Ink,
      fontSize = metrics.action,
    ) {
      hostAction("task.delete.${task.id}")
    }
    Text(
      "삭제가 성공하면 최신 목록 문서를 자동으로 다시 불러옵니다",
      modifier = Modifier.padding(top = 8f),
      color = Mist,
      fontSize = metrics.footnote,
    )
  }
}

private fun scaledSp(density: RcFloat, value: Float): RcSp = RcSp((density * value).toFloat())

private fun RcScope.ActionButton(
  label: String,
  modifier: Modifier,
  background: Int = Panel,
  contentColor: Int = White,
  fontSize: RcSp = 18.rsp,
  action: RcActionScope.() -> Unit,
) {
  Box(
    modifier =
      modifier.clip(RoundedRectShape(15f, 15f, 15f, 15f))
        .background(background)
        .onClick(action),
    horizontal = RcHorizontalPositioning.Center,
    vertical = RcVerticalPositioning.Center,
  ) {
    Text(label, color = contentColor, fontSize = fontSize)
  }
}

private const val Ink = 0xFF0B1016.toInt()
private const val Panel = 0xFF17212B.toInt()
private const val DeepPanel = 0xFF101820.toInt()
private const val Lime = 0xFFD5F36B.toInt()
private const val Coral = 0xFFFF8F70.toInt()
private const val Mist = 0xFFB8C5CE.toInt()
private const val White = 0xFFFFFFFF.toInt()
