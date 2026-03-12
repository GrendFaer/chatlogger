package Grend.chatlogger.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import Grend.chatlogger.client.gui.ChatLoggerGui;

/**
 * Регистрация клавиш мода
 */
public class ModKeyBindings {

    public static final String KEY_CATEGORY = "key.chatlogger.category";
    public static final String KEY_OPEN_GUI = "key.chatlogger.open_gui";

    public static KeyBinding openGuiKey;
    
    private static boolean wasGuiPressedLastTick = false;
    private static boolean[] wasStaffPressedLastTick = new boolean[5];

    public static void register() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            KEY_OPEN_GUI,
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Не обрабатывать бинды если открыт чат или другой экран
            if (client.currentScreen != null) return;
            
            // Обработка клавиши GUI
            if (openGuiKey.isPressed()) {
                if (!wasGuiPressedLastTick) {
                    wasGuiPressedLastTick = true;
                    MinecraftClient.getInstance().execute(() -> {
                        MinecraftClient.getInstance().player.sendMessage(
                            Text.literal("§a[ChatLogger] Открываю GUI..."),
                            false
                        );
                        ChatLoggerGui.openMainScreen();
                    });
                }
            } else {
                wasGuiPressedLastTick = false;
            }
        });
        
        // Регистрация и обработка биндов посохов
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Не обрабатывать бинды если открыт чат или другой экран
            if (client.currentScreen != null) return;
            
            StaffBindsConfig config = StaffBindsConfig.getInstance();
            for (int i = 0; i < 5; i++) {
                if (config.isEnabled(i)) {
                    int keyCode = config.getKeyCode(i);
                    if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), keyCode)) {
                        if (!wasStaffPressedLastTick[i]) {
                            wasStaffPressedLastTick[i] = true;
                            int slot = config.getTargetSlot(i);
                            StaffQuickUse.useStaff(slot);
                        }
                    } else {
                        wasStaffPressedLastTick[i] = false;
                    }
                }
            }
        });
        
        // Авто-переключение на меч
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Не обрабатывать если открыт чат или другой экран
            if (client.currentScreen != null) return;
            
            SwordSwitcherConfig config = SwordSwitcherConfig.getInstance();
            if (config.isEnabled()) {
                SwordSwitcher.findAndSwitchToSword();
            }
        });
        
        // Автоотправка сообщений
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Не отправлять если открыт чат
            if (client.currentScreen != null) return;
            
            AutoMessageSender.tick();
        });
    }
}
