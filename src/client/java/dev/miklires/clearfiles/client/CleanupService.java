package dev.miklires.clearfiles.client;

import dev.miklires.clearfiles.core.CleanupPlanner;
import dev.miklires.clearfiles.core.CleanupRules;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

final class CleanupService {
    private static final int MAX_SCANNED_ENTRIES = 10_000;

    private CleanupService() {
    }

    static void clean(Path gameDirectory, ClearFilesConfig config, Logger logger) {
        Path normalizedGameDirectory = gameDirectory.toAbsolutePath().normalize();
        AtomicInteger remaining = new AtomicInteger(config.maximumFilesPerRun);
        Duration minimumAge = Duration.ofMinutes(config.minimumAgeMinutes);
        Instant now = Instant.now();

        cleanDirectory(normalizedGameDirectory.resolve("logs"), CleanupRules::isArchivedLog,
                config.keepArchivedLogs, remaining, now, minimumAge, logger);
        cleanDirectory(normalizedGameDirectory.resolve("crash-reports"), CleanupRules::isCrashReport,
                config.keepCrashReports, remaining, now, minimumAge, logger);

        if (config.removeTemporaryFiles && remaining.get() > 0) {
            cleanDirectory(normalizedGameDirectory.resolve("logs"), CleanupRules::isTemporaryFile,
                    0, remaining, now, minimumAge, logger);
            cleanDirectory(normalizedGameDirectory.resolve("crash-reports"), CleanupRules::isTemporaryFile,
                    0, remaining, now, minimumAge, logger);
        }
    }

    private static void cleanDirectory(
            Path directory,
            Predicate<String> acceptedName,
            int keepNewest,
            AtomicInteger remaining,
            Instant now,
            Duration minimumAge,
            Logger logger) {
        if (remaining.get() <= 0) {
            return;
        }
        try {
            CleanupPlanner.Plan plan = CleanupPlanner.create(directory, acceptedName, keepNewest,
                    MAX_SCANNED_ENTRIES, remaining.get(), now, minimumAge);
            int removed = 0;
            for (Path file : plan.files()) {
                if (remaining.getAndDecrement() <= 0) {
                    break;
                }
                try {
                    if (Files.deleteIfExists(file)) {
                        removed++;
                    }
                } catch (Exception exception) {
                    logger.warn("Could not remove {}", file, exception);
                }
            }
            if (removed > 0) {
                logger.info("Removed {} old files from {}", removed, directory.getFileName());
            }
            if (plan.scanTruncated()) {
                logger.warn("Stopped scanning {} after {} entries", directory, MAX_SCANNED_ENTRIES);
            }
        } catch (Exception exception) {
            logger.warn("Could not clean {}", directory, exception);
        }
    }
}
