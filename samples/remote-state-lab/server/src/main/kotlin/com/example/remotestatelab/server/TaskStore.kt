package com.example.remotestatelab.server

data class ChecklistTask(
  val id: Int,
  val title: String,
  val detail: String,
)

data class ChecklistServerSnapshot(
  val revision: Int,
  val tasks: List<ChecklistTask>,
)

class TaskStore(
  initialTasks: List<ChecklistTask> = defaultChecklistTasks(),
) {
  private val lock = Any()
  private var revision = 1
  private var nextId = (initialTasks.maxOfOrNull(ChecklistTask::id) ?: 0) + 1
  private val tasks = initialTasks.toMutableList()

  fun snapshot(): ChecklistServerSnapshot =
    synchronized(lock) { ChecklistServerSnapshot(revision = revision, tasks = tasks.toList()) }

  fun add(title: String): ChecklistServerSnapshot =
    synchronized(lock) {
      val id = nextId++
      tasks +=
        ChecklistTask(
          id = id,
          title = title,
          detail = "앱에서 직접 작성한 작업 · 서버 ID #$id",
        )
      revision += 1
      ChecklistServerSnapshot(revision = revision, tasks = tasks.toList())
    }

  fun delete(id: Int): ChecklistServerSnapshot? =
    synchronized(lock) {
      if (!tasks.removeAll { it.id == id }) return@synchronized null
      revision += 1
      ChecklistServerSnapshot(revision = revision, tasks = tasks.toList())
    }
}

private fun defaultChecklistTasks(): List<ChecklistTask> =
  listOf(
    ChecklistTask(1, "R8 릴리즈 빌드 확인", "./gradlew app:minifyReleaseWithR8"),
    ChecklistTask(2, "에뮬레이터 스모크 테스트", "연결 · 입력 · 상세 · 삭제 플로우 확인"),
    ChecklistTask(3, "API 변경점 문서화", "alpha14 제한과 host 경계 기록"),
  )
