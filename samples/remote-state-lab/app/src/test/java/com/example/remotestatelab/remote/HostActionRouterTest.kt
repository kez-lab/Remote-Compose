package com.example.remotestatelab.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HostActionRouterTest {
  @Test
  fun `create action opens the native task editor`() {
    assertEquals(HostActionCommand.CreateTask, HostActionRouter.commandFor("task.create"))
  }

  @Test
  fun `delete action accepts only a positive numeric server id`() {
    assertEquals(HostActionCommand.DeleteTask(42), HostActionRouter.commandFor("task.delete.42"))
    assertNull(HostActionRouter.commandFor("task.delete.0"))
    assertNull(HostActionRouter.commandFor("task.delete.anything"))
  }

  @Test
  fun `unknown document action is rejected`() {
    assertNull(HostActionRouter.commandFor("open.arbitrary.url"))
  }
}
