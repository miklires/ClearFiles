package dev.miklires.clearfiles.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClearFilesClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Clear Files");

    @Override
    public void onInitializeClient() {
        ClearFilesConfig config = ConfigStore.load(LOGGER);
        if (!config.enabled) {
            return;
        }

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            Thread worker = Thread.ofVirtual().name("clear-files-cleanup").unstarted(() ->
                    CleanupService.clean(FabricLoader.getInstance().getGameDir(), config, LOGGER));
            worker.setUncaughtExceptionHandler((thread, exception) ->
                    LOGGER.warn("Cleanup worker stopped unexpectedly", exception));
            worker.start();
        });
    }
}
