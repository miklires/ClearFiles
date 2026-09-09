package dev.miklires.clearfiles.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class ConfigStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("clear-files.json");

    private ConfigStore() {
    }

    static ClearFilesConfig load(Logger logger) {
        ClearFilesConfig config = null;
        if (Files.isRegularFile(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                config = GSON.fromJson(reader, ClearFilesConfig.class);
            } catch (Exception exception) {
                preserveInvalidFile(logger);
                logger.warn("Could not read {}; defaults will be used", PATH, exception);
            }
        }
        if (config == null) {
            config = new ClearFilesConfig();
        }
        config.sanitize();
        save(config, logger);
        return config;
    }

    private static void save(ClearFilesConfig config, Logger logger) {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
        } catch (Exception exception) {
            logger.warn("Could not write {}", PATH, exception);
        }
    }

    private static void preserveInvalidFile(Logger logger) {
        try {
            Files.move(PATH, PATH.resolveSibling(PATH.getFileName() + ".invalid"),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception exception) {
            logger.warn("Could not preserve the invalid config", exception);
        }
    }
}
