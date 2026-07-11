package com.example.remotestatelab.remote

sealed interface HostActionCommand {
  data object CreateTask : HostActionCommand

  data class DeleteTask(val id: Int) : HostActionCommand
}

object HostActionRouter {
  private const val DELETE_PREFIX = "task.delete."

  fun commandFor(name: String): HostActionCommand? =
    when {
      name == "task.create" -> HostActionCommand.CreateTask
      name.startsWith(DELETE_PREFIX) ->
        name.removePrefix(DELETE_PREFIX).toIntOrNull()?.takeIf { it > 0 }?.let {
          HostActionCommand.DeleteTask(it)
        }
      else -> null
    }
}
