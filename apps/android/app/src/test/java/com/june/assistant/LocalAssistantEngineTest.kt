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
    fun testRemindMeWithNaturalTimeDelay() {
        val (reply, proposal) = engine.processInput("remind me to say i love to babu in 1 minute")
        assertNotNull(proposal)
        assertEquals("reminder.create", proposal!!.actionType)
        assertEquals("say i love to babu", proposal.target)
        assertEquals("60", proposal.parameters["delay_seconds"])
        assertEquals(RiskLevel.LOW, proposal.riskLevel)
        assertEquals(ActionStatus.AUTHORIZED, proposal.status)
        assertTrue(reply.contains("Reminder set for 1 minute from now"))
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
    fun testStartTimerIsNotAppOpen() {
        val (reply, proposal) = engine.processInput("start a timer")
        assertNull(proposal)
        assertTrue(reply.contains("I heard: \"start a timer\""))
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
