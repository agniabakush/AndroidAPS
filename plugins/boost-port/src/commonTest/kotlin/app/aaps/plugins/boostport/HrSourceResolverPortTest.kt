package app.aaps.plugins.boostport

import app.aaps.plugins.aps.openAPSBoost.HrFeedDarkTracker
import app.aaps.plugins.aps.openAPSBoost.HrSourceResolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HrSourceResolverPortTest {
    private val now = 1_800_000_000_000L

    @Test fun categorizesSourcesWithoutReadingDatabase() {
        assertEquals("garmin", HrSourceResolver.canonical("Garmin"))
        assertEquals("hc", HrSourceResolver.canonical("HealthConnect"))
        assertEquals("hc", HrSourceResolver.canonical(" "))
        assertEquals("worn:OPPO OWWE261", HrSourceResolver.canonical("OPPO OWWE261"))
    }

    @Test fun prefersFreshRealtimeToHealthConnect() {
        val r = HrSourceResolver.resolve(
            listOf(
                HrSourceResolver.Reading("HealthConnect", now - 60_000L),
                HrSourceResolver.Reading("OPPO OWWE261", now - 2 * 60_000L)
            ), now
        )
        assertTrue(r.anyFresh)
        assertEquals("worn:OPPO OWWE261", r.active)
    }

    @Test fun staleSourcesAreNotSelected() {
        val r = HrSourceResolver.resolve(
            listOf(HrSourceResolver.Reading("Garmin", now - 13 * 60_000L)), now
        )
        assertFalse(r.anyFresh)
        assertNull(r.active)
    }

    @Test fun initialDarkStateDoesNotReportTransition() {
        val tracker = HrFeedDarkTracker(notifyAfterMin = 60)
        val dark = HrSourceResolver.resolve(emptyList(), now)
        assertNull(tracker.onCycle(dark, now, 12).wentDarkNote)
    }

    @Test fun transitionAlertsOnceDuringDayAndResetsOnRecovery() {
        val tracker = HrFeedDarkTracker(notifyAfterMin = 60)
        val fresh = HrSourceResolver.resolve(listOf(HrSourceResolver.Reading("Garmin", now)), now)
        val dark = HrSourceResolver.resolve(listOf(HrSourceResolver.Reading("Garmin", now - 15 * 60_000L)), now)
        tracker.onCycle(fresh, now, 12)
        val edge = tracker.onCycle(dark, now + 15 * 60_000L, 12)
        assertNotNull(edge.wentDarkNote)
        assertFalse(edge.raiseNotification)
        assertTrue(tracker.onCycle(dark, now + 76 * 60_000L, 12).raiseNotification)
        assertFalse(tracker.onCycle(dark, now + 77 * 60_000L, 12).raiseNotification)
        tracker.onCycle(fresh, now + 78 * 60_000L, 12)
        assertNotNull(tracker.onCycle(dark, now + 79 * 60_000L, 12).wentDarkNote)
    }
}
