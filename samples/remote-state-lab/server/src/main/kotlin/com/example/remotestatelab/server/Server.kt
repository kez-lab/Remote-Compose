package com.example.remotestatelab.server

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

private const val MAX_TASK_TITLE_LENGTH = 80
private val taskStore = TaskStore()

fun main() {
  println("Remote State Lab server: http://0.0.0.0:8080")
  println("Android emulator client URL: http://10.0.2.2:8080")

  embeddedServer(Netty, host = "0.0.0.0", port = 8080) {
    routing {
      get("/health") {
        call.respondText("ok")
      }
      get("/document") {
        call.respondBytes(
          bytes = buildChecklistDocument(taskStore.snapshot()),
          contentType = ContentType.Application.OctetStream,
          status = HttpStatusCode.OK,
        )
      }
      post("/tasks") {
        val title = call.receiveText().trim()
        when {
          title.isEmpty() -> call.respondText("작업 이름이 비어 있습니다.", status = HttpStatusCode.BadRequest)
          title.length > MAX_TASK_TITLE_LENGTH ->
            call.respondText(
              "작업 이름은 ${MAX_TASK_TITLE_LENGTH}자 이하여야 합니다.",
              status = HttpStatusCode.PayloadTooLarge,
            )
          else -> call.respondSnapshot(taskStore.add(title))
        }
      }
      delete("/tasks/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null || id <= 0) {
          call.respondText("올바르지 않은 task id입니다.", status = HttpStatusCode.BadRequest)
        } else {
          val next = taskStore.delete(id)
          if (next == null) {
            call.respondText("task를 찾을 수 없습니다.", status = HttpStatusCode.NotFound)
          } else {
            call.respondSnapshot(next)
          }
        }
      }
    }
  }.start(wait = true)
}

private suspend fun io.ktor.server.application.ApplicationCall.respondSnapshot(
  snapshot: ChecklistServerSnapshot,
) {
  respondText(
    "{\"revision\":${snapshot.revision},\"taskCount\":${snapshot.tasks.size}}",
    ContentType.Application.Json,
  )
}
