package dev.miklires.clearfiles.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CleanupPlannerTest {
    @TempDir
    Path directory;

    @Test
    void keepsNewestAndIgnoresUnknownFiles() throws Exception {
        Instant now = Instant.parse("2026-09-09T12:00:00Z");
        Path oldest = create("2026-09-01-1.log.gz", now.minus(Duration.ofDays(8)));
        create("2026-09-02-1.log.gz", now.minus(Duration.ofDays(7)));
        create("latest.log", now.minus(Duration.ofDays(30)));
        create("important.txt", now.minus(Duration.ofDays(30)));

        CleanupPlanner.Plan plan = CleanupPlanner.create(directory, CleanupRules::isArchivedLog,
                1, 100, 100, now, Duration.ofMinutes(10));

        assertEquals(List.of(oldest), plan.files());
        assertFalse(plan.scanTruncated());
    }

    @Test
    void respectsDeleteLimit() throws Exception {
        Instant now = Instant.parse("2026-09-09T12:00:00Z");
        create("2026-09-01-1.log.gz", now.minus(Duration.ofDays(8)));
        create("2026-09-02-1.log.gz", now.minus(Duration.ofDays(7)));
        create("2026-09-03-1.log.gz", now.minus(Duration.ofDays(6)));

        CleanupPlanner.Plan plan = CleanupPlanner.create(directory, CleanupRules::isArchivedLog,
                0, 100, 2, now, Duration.ZERO);

        assertEquals(2, plan.files().size());
    }

    private Path create(String name, Instant modified) throws Exception {
        Path path = Files.writeString(directory.resolve(name), "test");
        Files.setLastModifiedTime(path, FileTime.from(modified));
        return path;
    }
}
