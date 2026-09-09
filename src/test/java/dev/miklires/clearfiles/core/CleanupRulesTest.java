package dev.miklires.clearfiles.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CleanupRulesTest {
    @Test
    void recognizesOnlyExpectedLogArchives() {
        assertTrue(CleanupRules.isArchivedLog("2026-09-09-1.log.gz"));
        assertTrue(CleanupRules.isArchivedLog("debug-2026-09-09-1.log.gz"));
        assertFalse(CleanupRules.isArchivedLog("latest.log"));
        assertFalse(CleanupRules.isArchivedLog("options.txt"));
    }

    @Test
    void recognizesCrashReportsWithoutMatchingOtherTextFiles() {
        assertTrue(CleanupRules.isCrashReport("crash-2026-09-09_12.34.56-client.txt"));
        assertFalse(CleanupRules.isCrashReport("README.txt"));
    }
}
