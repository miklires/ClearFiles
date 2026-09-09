# Clear Files

Keeps Minecraft's log and crash-report folders small without touching saves, configuration, screenshots, resource packs, mods, or unknown files.

## Safe default behaviour

- preserves `logs/latest.log`
- keeps the 3 newest archived logs
- keeps the 3 newest recognized crash reports
- removes recognized `.tmp`, `.temp`, `.bak`, and `.old` files only inside `logs` and `crash-reports`
- ignores files newer than 10 minutes
- never scans recursively or follows symbolic links
- stops after 10,000 directory entries and deletes at most 2,000 files per launch
- performs cleanup on a background virtual thread after the client starts

Files with unknown names and all directories are preserved.

## Configuration

`config/clear-files.json` is created on first launch:

```json
{
  "enabled": true,
  "keepArchivedLogs": 3,
  "keepCrashReports": 3,
  "removeTemporaryFiles": true,
  "minimumAgeMinutes": 10,
  "maximumFilesPerRun": 2000
}
```

Numeric values are clamped to safe ranges. An unreadable config is preserved as `clear-files.json.invalid` before defaults are restored.

## Requirements

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API
- Java 25

## Build

```text
./gradlew clean build
```

## License

MIT
