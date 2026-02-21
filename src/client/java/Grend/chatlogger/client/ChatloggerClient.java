package Grend.chatlogger.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import Grend.chatlogger.client.commands.ModCommands;
import Grend.chatlogger.data.DataManager;

public class ChatloggerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            ModCommands.register(dispatcher);
        });

        DataManager.getInstance().load();
        ModConfig.getInstance();

        DataManager manager = DataManager.getInstance();
        if (manager.getPlayerCount() > 0) {
            MinecraftClient.getInstance().execute(() -> {
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(
                        Text.literal("§a[ChatLogger] Мод активен! Загружено " + manager.getPlayerCount() + " игроков."),
                        false
                    );
                }
            });
        } else {
            MinecraftClient.getInstance().execute(() -> {
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(
                        Text.literal("§a[ChatLogger] Мод активен! Ожидание сообщений в чате..."),
                        false
                    );
                }
            });
        }

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> DataManager.getInstance().save());
    }
}
