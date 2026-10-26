package app.aaps.plugins.boostport

import app.aaps.plugins.aps.openAPSBoostV5.BoostHistorySync
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BoostHistorySyncPortTest {
    private val thin = BoostHistorySync.History(daysWithTdd = 2, bgReadings = 400, treatments = 10)
    private val full = BoostHistorySync.History(daysWithTdd = 10, bgReadings = 2200, treatments = 150)
    private val empty = BoostHistorySync.State(attempts = 0, lastAttemptMs = 0L, preBgReadings = 0, preTreatments = 0)
    private val now = 1_800_000_000_000L

    @Test fun sufficientHistoryNeverRequestsBackfill() {
        val d = BoostHistorySync.decide(full, true, now, "now", empty)
        assertFalse(d.requestBackfill)
        assertEquals(0, assertNotNull(d.newState).attempts)
    }

    @Test fun thinHistoryRequestsOnlyWhenNsAvailable() {
        val unavailable = BoostHistorySync.decide(thin, false, now, "now", empty)
        assertFalse(unavailable.requestBackfill)
        assertEquals(0, assertNotNull(unavailable.newState).attempts)
        val available = BoostHistorySync.decide(thin, true, now, "now", empty)
        assertTrue(available.requestBackfill)
        assertEquals(1, assertNotNull(available.newState).attempts)
    }

    @Test fun cooldownBlocksImmediateRetry() {
        val first = BoostHistorySync.decide(thin, true, now, "now", empty)
        val d = BoostHistorySync.decide(thin, true, now + 60_000L, "later", assertNotNull(first.newState))
        assertFalse(d.requestBackfill)
    }

    @Test fun attemptCapBlocksFurtherRequests() {
        val state = empty.copy(attempts = BoostHistorySync.MAX_ATTEMPTS, firstSeenMs = now, lastAttemptMs = now - BoostHistorySync.RETRY_COOLDOWN_MS)
        val d = BoostHistorySync.decide(thin, true, now, "now", state)
        assertFalse(d.requestBackfill)
    }

    @Test fun expiredInstallWindowCannotBackfill() {
        val old = empty.copy(firstSeenMs = now - BoostHistorySync.NEW_INSTALL_WINDOW_MS - 1L)
        val d = BoostHistorySync.decide(thin, true, now, "now", old)
        assertFalse(d.requestBackfill)
    }

    @Test fun filledHistoryResetsAttemptsButKeepsFirstSeenAnchor() {
        val first = BoostHistorySync.decide(thin, true, now, "now", empty)
        val d = BoostHistorySync.decide(full, true, now + 1000L, "later", assertNotNull(first.newState))
        assertFalse(d.requestBackfill)
        assertEquals(0, assertNotNull(d.newState).attempts)
        assertEquals(now, assertNotNull(d.newState).firstSeenMs)
        assertTrue(assertNotNull(d.summary).startsWith("filled:"))
    }
}
