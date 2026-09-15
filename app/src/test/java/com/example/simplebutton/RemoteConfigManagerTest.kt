package com.example.simplebutton

import com.example.simplebutton.remote.RemoteConfigManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class RemoteConfigManagerTest {

    // ==========================================
    // 1. JSON Parsing Tests
    // ==========================================

    @Test
    fun testParseFullConfigJson() {
        val json = """
            {
              "latestVersionCode": 12,
              "latestVersionName": "1.2.0",
              "apkUrl": "https://example.com/LornshillTimetable.apk",
              "whatsNew": "Added timetable notifications, improved editor and fixed several bugs.",
              "notifications": [
                {
                  "id": "notice-001",
                  "enabled": true,
                  "title": "Timetable Update",
                  "message": "Your timetable has been updated. Please check your periods.",
                  "buttonText": "Close",
                  "actionUrl": "https://example.com/info",
                  "actionText": "Read More",
                  "expiresAt": "2026-12-31T23:59:59Z"
                }
              ]
            }
        """.trimIndent()

        val config = RemoteConfigManager.parseConfigJson(json)

        // Verify update info
        assertNotNull(config.updateInfo)
        assertEquals(12L, config.updateInfo!!.latestVersionCode)
        assertEquals("1.2.0", config.updateInfo!!.latestVersionName)
        assertEquals("https://example.com/LornshillTimetable.apk", config.updateInfo!!.apkUrl)
        assertTrue(config.updateInfo!!.whatsNew.contains("Added timetable notifications"))

        // Verify notifications
        assertEquals(1, config.notifications.size)
        val notice = config.notifications[0]
        assertEquals("notice-001", notice.id)
        assertTrue(notice.enabled)
        assertEquals("Timetable Update", notice.title)
        assertEquals("Close", notice.buttonText)
        assertEquals("https://example.com/info", notice.actionUrl)
        assertEquals("Read More", notice.actionText)
        assertEquals("2026-12-31T23:59:59Z", notice.expiresAt)
    }

    @Test
    fun testParseMinimalConfigWithOnlyUpdate() {
        val json = """
            {
              "latestVersionCode": 15,
              "latestVersionName": "1.3.0",
              "apkUrl": "https://example.com/update.apk",
              "whatsNew": "Bug fixes."
            }
        """.trimIndent()

        val config = RemoteConfigManager.parseConfigJson(json)
        assertNotNull(config.updateInfo)
        assertEquals(15L, config.updateInfo!!.latestVersionCode)
        assertEquals("1.3.0", config.updateInfo!!.latestVersionName)
        assertTrue(config.notifications.isEmpty())
    }

    @Test
    fun testParseMinimalConfigWithOnlyNotifications() {
        val json = """
            {
              "notifications": [
                {
                  "id": "alert-101",
                  "enabled": true,
                  "title": "School Closed",
                  "message": "School closed today due to weather."
                }
              ]
            }
        """.trimIndent()

        val config = RemoteConfigManager.parseConfigJson(json)
        assertNull(config.updateInfo)
        assertEquals(1, config.notifications.size)
        assertEquals("alert-101", config.notifications[0].id)
        assertEquals("Close", config.notifications[0].buttonText) // default fallback
        assertNull(config.notifications[0].actionUrl)
        assertNull(config.notifications[0].expiresAt)
    }

    @Test
    fun testParseResilientToEmptyOrMissingFields() {
        val json = """
            {
              "notifications": [
                {
                  "id": "test-blank",
                  "enabled": false,
                  "title": "",
                  "message": ""
                },
                {
                  "id": ""
                }
              ]
            }
        """.trimIndent()

        val config = RemoteConfigManager.parseConfigJson(json)
        assertNull(config.updateInfo)
        // Only notice with non-blank id should be parsed
        assertEquals(1, config.notifications.size)
        assertEquals("test-blank", config.notifications[0].id)
        assertFalse(config.notifications[0].enabled)
    }

    // ==========================================
    // 2. Version Comparison & Weekly Cooldown Tests
    // ==========================================

    @Test
    fun testUpdateAvailableWhenInstalledCodeLower() {
        // Installed = 10, Remote = 12, Never shown before
        val shouldShow = RemoteConfigManager.shouldShowUpdatePrompt(
            installedVersionCode = 10L,
            latestVersionCode = 12L,
            lastShownVersion = 0L,
            lastShownTimestamp = 0L
        )
        assertTrue("Update prompt should show when installed code is lower and never shown", shouldShow)
    }

    @Test
    fun testUpdateNotAvailableWhenInstalledCodeEqual() {
        // Installed = 12, Remote = 12
        val shouldShow = RemoteConfigManager.shouldShowUpdatePrompt(
            installedVersionCode = 12L,
            latestVersionCode = 12L,
            lastShownVersion = 0L,
            lastShownTimestamp = 0L
        )
        assertFalse("Update prompt should NOT show when app is already up to date", shouldShow)
    }

    @Test
    fun testUpdateNotAvailableWhenInstalledCodeNewer() {
        // Installed = 15 (dev build), Remote = 12
        val shouldShow = RemoteConfigManager.shouldShowUpdatePrompt(
            installedVersionCode = 15L,
            latestVersionCode = 12L,
            lastShownVersion = 0L,
            lastShownTimestamp = 0L
        )
        assertFalse("Update prompt should NOT show when installed app is newer than remote", shouldShow)
    }

    @Test
    fun testCooldownSuppressesPromptWithin7Days() {
        val now = 1700000000000L // arbitrary fixed time
        val threeDaysAgo = now - (3L * 24L * 60L * 60L * 1000L)

        // Version 12 was shown 3 days ago (< 7 days)
        val shouldShow = RemoteConfigManager.shouldShowUpdatePrompt(
            installedVersionCode = 10L,
            latestVersionCode = 12L,
            lastShownVersion = 12L,
            lastShownTimestamp = threeDaysAgo,
            currentTimeMillis = now
        )
        assertFalse("Prompt must be suppressed when 7 days have not elapsed for the same version", shouldShow)
    }

    @Test
    fun testCooldownAllowsPromptAfter7Days() {
        val now = 1700000000000L
        val eightDaysAgo = now - (8L * 24L * 60L * 60L * 1000L)

        // Version 12 was shown 8 days ago (>= 7 days)
        val shouldShow = RemoteConfigManager.shouldShowUpdatePrompt(
            installedVersionCode = 10L,
            latestVersionCode = 12L,
            lastShownVersion = 12L,
            lastShownTimestamp = eightDaysAgo,
            currentTimeMillis = now
        )
        assertTrue("Prompt should appear again after 7 days have elapsed if still on older version", shouldShow)
    }

    @Test
    fun testNewVersionBypassesOldCooldownImmediately() {
        val now = 1700000000000L
        val oneHourAgo = now - (3600L * 1000L)

        // Version 12 was shown 1 hour ago. But now version 13 is released!
        val shouldShow = RemoteConfigManager.shouldShowUpdatePrompt(
            installedVersionCode = 10L,
            latestVersionCode = 13L, // New release!
            lastShownVersion = 12L,  // Old version that was recorded
            lastShownTimestamp = oneHourAgo,
            currentTimeMillis = now
        )
        assertTrue("A new release version must bypass the previous version's cooldown immediately", shouldShow)
    }

    // ==========================================
    // 3. Expiry Date & Timestamp Parsing Tests
    // ==========================================

    @Test
    fun testNotificationExpiryWithUtcIso() {
        val pastDate = "2020-01-01T00:00:00Z"
        val futureDate = "2099-01-01T00:00:00Z"

        val fixedNow = Instant.parse("2026-09-15T12:00:00Z")

        assertTrue("Past ISO timestamp should be expired", RemoteConfigManager.isNotificationExpired(pastDate, fixedNow))
        assertFalse("Future ISO timestamp should not be expired", RemoteConfigManager.isNotificationExpired(futureDate, fixedNow))
    }

    @Test
    fun testNotificationExpiryWithOffsetIso() {
        val pastDate = "2020-01-01T00:00:00+01:00"
        val futureDate = "2099-01-01T00:00:00+01:00"

        val fixedNow = Instant.parse("2026-09-15T12:00:00Z")

        assertTrue("Past offset timestamp should be expired", RemoteConfigManager.isNotificationExpired(pastDate, fixedNow))
        assertFalse("Future offset timestamp should not be expired", RemoteConfigManager.isNotificationExpired(futureDate, fixedNow))
    }

    @Test
    fun testNotificationExpiryWhenOmittedOrBlank() {
        val fixedNow = Instant.parse("2026-09-15T12:00:00Z")

        assertFalse("Null expiry should never expire", RemoteConfigManager.isNotificationExpired(null, fixedNow))
        assertFalse("Empty expiry should never expire", RemoteConfigManager.isNotificationExpired("", fixedNow))
        assertFalse("Whitespace expiry should never expire", RemoteConfigManager.isNotificationExpired("   ", fixedNow))
    }

    @Test
    fun testNotificationExpiryWithMalformedDateFallback() {
        val fixedNow = Instant.parse("2026-09-15T12:00:00Z")
        // Malformed string should fail gracefully and not throw exception
        assertFalse("Malformed date should not crash and should return false", RemoteConfigManager.isNotificationExpired("invalid-date-string", fixedNow))
    }
}
