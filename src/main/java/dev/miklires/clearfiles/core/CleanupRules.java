package dev.miklires.clearfiles.core;

import java.util.regex.Pattern;

public final class CleanupRules {
    private static final Pattern ARCHIVED_LOG = Pattern.compile(
            "(?:debug-)?\\d{4}-\\d{2}-\\d{2}(?:-\\d+)?\\.log\\.gz",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CRASH_REPORT = Pattern.compile(
            "crash-\\d{4}-\\d{2}-\\d{2}_\\d{2}\\.\\d{2}\\.\\d{2}-(?:client|server)\\.txt",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern TEMPORARY_FILE = Pattern.compile(
            ".+\\.(?:tmp|temp|bak|old)", Pattern.CASE_INSENSITIVE);

    private CleanupRules() {
    }

    public static boolean isArchivedLog(String name) {
        return ARCHIVED_LOG.matcher(name).matches();
    }

    public static boolean isCrashReport(String name) {
        return CRASH_REPORT.matcher(name).matches();
    }

    public static boolean isTemporaryFile(String name) {
        return TEMPORARY_FILE.matcher(name).matches();
    }
}
