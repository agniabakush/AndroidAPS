package app.aaps.plugins.boostport

import app.aaps.plugins.aps.openAPSBoost.StepSourceResolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StepSourceResolverPortTest {
    private fun state(source: String, fresh: Boolean, days: Int = 0, steps: Int = 0) =
        StepSourceResolver.SourceState(source, fresh, days, steps)

    @Test fun emptyFeedHasNoActiveSource() {
        val result = StepSourceResolver.resolve(emptyList())
        assertNull(result.active)
        assertEquals(0, result.stepsToday)
        assertFalse(result.activeFresh)
    }

    @Test fun realtimeWearOutranksOtherFreshFeeds() {
        val result = StepSourceResolver.resolve(listOf(
            state("phone", true, 7, 1000),
            state("com.garmin.android.apps.connectmobile", true, 7, 2000),
            state("wear", true, 1, 1500)
        ))
        assertEquals("wear", result.active)
        assertEquals(1500, result.stepsToday)
        assertTrue(result.activeFresh)
    }

    @Test fun freshPhoneWinsOverStaleWear() {
        val result = StepSourceResolver.resolve(listOf(
            state("wear", false, 7, 3000),
            state("phone", true, 1, 1200)
        ))
        assertEquals("phone", result.active)
    }

    @Test fun highestTrustSourceWithHistoryIsFallback() {
        val result = StepSourceResolver.resolve(listOf(
            state("phone", false, 7, 800),
            state("garmin", false, 2, 1000)
        ))
        assertEquals("garmin", result.active)
        assertFalse(result.activeFresh)
    }

    @Test fun canonicalHealthConnectWriter() {
        assertEquals("hc:health", StepSourceResolver.canonical("com.example.health"))
        assertEquals("garmin", StepSourceResolver.canonical("com.garmin.android.apps.connectmobile"))
    }
}
