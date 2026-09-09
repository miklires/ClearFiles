package dev.miklires.clearfiles.client;

final class ClearFilesConfig {
    boolean enabled = true;
    int keepArchivedLogs = 3;
    int keepCrashReports = 3;
    boolean removeTemporaryFiles = true;
    int minimumAgeMinutes = 10;
    int maximumFilesPerRun = 2000;

    void sanitize() {
        keepArchivedLogs = clamp(keepArchivedLogs, 0, 100);
        keepCrashReports = clamp(keepCrashReports, 0, 100);
        minimumAgeMinutes = clamp(minimumAgeMinutes, 1, 1440);
        maximumFilesPerRun = clamp(maximumFilesPerRun, 1, 5000);
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
