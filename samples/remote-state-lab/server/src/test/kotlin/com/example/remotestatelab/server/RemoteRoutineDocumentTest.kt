package com.example.remotestatelab.server

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

class RemoteRoutineDocumentTest {
  @Test
  fun `document generation is deterministic per revision`() {
    val snapshot =
      ChecklistServerSnapshot(
        revision = 7,
        tasks = listOf(ChecklistTask(1, "테스트 작업", "테스트 상세")),
      )
    val first = buildChecklistDocument(snapshot)
    val second = buildChecklistDocument(snapshot)

    assertTrue(first.size > 100)
    assertContentEquals(first, second)
  }

  @Test
  fun `task content is encoded into a different document`() {
    val first =
      buildChecklistDocument(
        ChecklistServerSnapshot(1, listOf(ChecklistTask(1, "A", "첫 번째")))
      )
    val second =
      buildChecklistDocument(
        ChecklistServerSnapshot(2, listOf(ChecklistTask(1, "B", "두 번째")))
      )

    assertTrue(!first.contentEquals(second))
  }

  @Test
  fun `server list is not capped at four tasks`() {
    val store = TaskStore(initialTasks = emptyList())

    repeat(25) { index -> store.add("직접 입력한 작업 ${index + 1}") }

    assertTrue(store.snapshot().tasks.size == 25)
    assertTrue(buildChecklistDocument(store.snapshot()).size < 512 * 1024)
  }
}
