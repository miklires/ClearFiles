package dev.miklires.clearfiles.core;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public final class CleanupPlanner {
    private CleanupPlanner() {
    }

    public static Plan create(
            Path directory,
            Predicate<String> acceptedName,
            int keepNewest,
            int maxEntries,
            int maxDeletes,
            Instant now,
            Duration minimumAge) throws IOException {
        if (keepNewest < 0 || maxEntries < 1 || maxDeletes < 0 || minimumAge.isNegative()) {
            throw new IllegalArgumentException("Cleanup limits must be non-negative");
        }
        if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(directory)) {
            return new Plan(List.of(), false);
        }

        List<Candidate> candidates = new ArrayList<>();
        int inspected = 0;
        boolean truncated = false;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                if (++inspected > maxEntries) {
                    truncated = true;
                    break;
                }
                if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(path)) {
                    continue;
                }
                String name = path.getFileName().toString();
                if (!acceptedName.test(name)) {
                    continue;
                }
                FileTime modified = Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS);
                if (modified.toInstant().isAfter(now.minus(minimumAge))) {
                    continue;
                }
                candidates.add(new Candidate(path, modified));
            }
        }

        candidates.sort(Comparator.comparing(Candidate::modified).reversed());
        int from = Math.min(keepNewest, candidates.size());
        int to = Math.min(candidates.size(), from + maxDeletes);
        return new Plan(candidates.subList(from, to).stream().map(Candidate::path).toList(), truncated);
    }

    private record Candidate(Path path, FileTime modified) {
    }

    public record Plan(List<Path> files, boolean scanTruncated) {
        public Plan {
            files = List.copyOf(files);
        }
    }
}
