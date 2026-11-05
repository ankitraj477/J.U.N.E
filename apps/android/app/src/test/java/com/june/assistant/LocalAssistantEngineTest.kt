package com.june.assistant

import com.june.assistant.domain.LocalAssistantEngine
import com.june.assistant.domain.model.ActionStatus
import com.june.assistant.domain.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalAssistantEngineTest {

    private val engine = LocalAssistantEngine()

    @Test
    fun testGreeting() {
        val (reply, proposal) = engine.processInput("hello")
        assertNull(proposal)
        assertTrue(reply.contains("J.U.N.E. is online"))
    }

    @Test
    fun testIdentity() {
        val (reply, proposal) = engine.processInput("who are you")
        assertNull(proposal)
        assertTrue(reply.contains("Just Useful, Never Excessive"))
    }

    @Test
    fun testHelp() {
        val (reply, proposal) = engine.processInput("what can you do")
        assertNull(proposal)
        assertTrue(reply.contains("Notes"))
        assertTrue(reply.contains("Reminders"))
    }

    @Test
    fun testTime() {
        val (reply, proposal) = engine.processInput("what time is it")
        assertNull(proposal)
        assertTrue(reply.contains("current time is"))
    }

    @Test
    fun testCreateNote() {
        val (reply, proposal) = engine.processInput("take note Buy groceries")
        assertNotNull(proposal)
        assertEquals("notes.create", proposal!!.actionType)
        assertEquals("Buy groceries", proposal.target)
        assertEquals(RiskLevel.LOW, proposal.riskLevel)
        assertEquals(ActionStatus.AUTHORIZED, proposal.status)
        assertTrue(reply.contains("Note saved"))
    }

    @Test
    fun testRemindMe() {
        val (_, proposal) = engine.processInput("remind me to Call mom at 5pm")
        assertNotNull(proposal)
        assertEquals("reminder.create", proposal!!.actionType)
        assertEquals("Call mom at 5pm", proposal.target)
        assertEquals(RiskLevel.LOW, proposal.riskLevel)
        assertEquals(ActionStatus.AUTHORIZED, proposal.status)
    }

    @Test
    fun testAppLaunch() {
        val (reply, proposal) = engine.processInput("open chrome")
        assertNotNull(proposal)
        assertEquals("system.app_open", proposal!!.actionType)
        assertEquals("chrome", proposal.target)
        assertEquals(RiskLevel.MEDIUM, proposal.riskLevel)
        assertTrue(reply.contains("Opening chrome"))
    }

    @Test
    fun testDeleteHighRisk() {
        val (reply, proposal) = engine.processInput("delete confidential.docx")
        assertNotNull(proposal)
        assertEquals("file.delete", proposal!!.actionType)
        assertEquals("confidential.docx", proposal.target)
        assertEquals(RiskLevel.HIGH, proposal.riskLevel)
        assertEquals(ActionStatus.PROPOSED, proposal.status)
        assertTrue(reply.contains("High-risk action"))
    }

    @Test
    fun testConversationalFallback() {
        val (reply, proposal) = engine.processInput("What do you think about AI?")
        assertNull(proposal)
        assertTrue(reply.contains("I can save this as a note"))
    }
}
