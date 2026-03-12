package Grend.chatlogger.client;

import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Конфигурация для 5 биндов посохов
 * Каждый бинд имеет свою клавишу и слот
 */
public class StaffBindsConfig {
    private static StaffBindsConfig instance;
    private static final Path CONFIG_PATH = Paths.get("chatlogger_staff_binds.properties");

    // 5 биндов: клавиши и слоты
    private int[] keyCodes = new int[]{GLFW.GLFW_KEY_R, GLFW.GLFW_KEY_F, GLFW.GLFW_KEY_V, GLFW.GLFW_KEY_C, GLFW.GLFW_KEY_X};
    private int[] targetSlots = new int[]{4, 5, 6, 7, 8}; // По умолчанию: слоты 5-9
    private boolean[] enabled = new boolean[]{true, true, true, true, true};

    // Для ожидания ввода новой клавиши
    private int waitingForBindIndex = -1;

    private StaffBindsConfig() { load(); }

    public static synchronized StaffBindsConfig getInstance() {
        if (instance == null) instance = new StaffBindsConfig();
        return instance;
    }

    // === КЛАВИШИ ===
    public int getKeyCode(int bindIndex) {
        if (bindIndex < 0 || bindIndex >= 5) return GLFW.GLFW_KEY_R;
        return keyCodes[bindIndex];
    }

    public void setKeyCode(int bindIndex, int keyCode) {
        if (bindIndex < 0 || bindIndex >= 5) return;
        keyCodes[bindIndex] = keyCode;
        save();
    }

    public String getKeyName(int bindIndex) {
        int keyCode = getKeyCode(bindIndex);
        return InputUtil.fromKeyCode(keyCode, 0).getLocalizedText().getString();
    }

    public void setWaitingForBind(int index) {
        waitingForBindIndex = index;
    }

    public int getWaitingForBind() {
        return waitingForBindIndex;
    }

    public boolean isWaitingForBind() {
        return waitingForBindIndex >= 0;
    }

    // === СЛОТЫ ===
    public int getTargetSlot(int bindIndex) {
        if (bindIndex < 0 || bindIndex >= 5) return 4;
        return targetSlots[bindIndex];
    }

    public void setTargetSlot(int bindIndex, int slot) {
        if (bindIndex < 0 || bindIndex >= 5) return;
        if (slot < 0 || slot > 8) return;
        targetSlots[bindIndex] = slot;
        save();
    }

    // === ВКЛЮЧЕНИЕ ===
    public boolean isEnabled(int bindIndex) {
        if (bindIndex < 0 || bindIndex >= 5) return false;
        return enabled[bindIndex];
    }

    public void setEnabled(int bindIndex, boolean value) {
        if (bindIndex < 0 || bindIndex >= 5) return;
        enabled[bindIndex] = value;
        save();
    }

    public void toggleEnabled(int bindIndex) {
        setEnabled(bindIndex, !isEnabled(bindIndex));
    }

    // === СОХРАНЕНИЕ/ЗАГРУЗКА ===
    public void save() {
        try {
            Properties props = new Properties();
            for (int i = 0; i < 5; i++) {
                props.setProperty("bind." + i + ".key", String.valueOf(keyCodes[i]));
                props.setProperty("bind." + i + ".slot", String.valueOf(targetSlots[i]));
                props.setProperty("bind." + i + ".enabled", String.valueOf(enabled[i]));
            }
            try (OutputStream os = Files.newOutputStream(CONFIG_PATH)) {
                props.store(os, "ChatLogger Staff Binds Configuration");
            }
        } catch (IOException e) {
            System.err.println("[ChatLogger] Ошибка сохранения конфига биндов: " + e.getMessage());
        }
    }

    public void load() {
        if (!Files.exists(CONFIG_PATH)) { save(); return; }
        try {
            Properties props = new Properties();
            try (InputStream is = Files.newInputStream(CONFIG_PATH)) { props.load(is); }
            
            for (int i = 0; i < 5; i++) {
                // Клавиша
                String keyValue = props.getProperty("bind." + i + ".key", String.valueOf(keyCodes[i]));
                try { keyCodes[i] = Integer.parseInt(keyValue); } 
                catch (NumberFormatException e) { keyCodes[i] = GLFW.GLFW_KEY_R; }
                
                // Слот
                String slotValue = props.getProperty("bind." + i + ".slot", String.valueOf(targetSlots[i]));
                try { targetSlots[i] = Integer.parseInt(slotValue); } 
                catch (NumberFormatException e) { targetSlots[i] = i + 4; }
                
                // Включено
                String enabledValue = props.getProperty("bind." + i + ".enabled", "true");
                enabled[i] = Boolean.parseBoolean(enabledValue);
            }
        } catch (IOException e) {
            System.err.println("[ChatLogger] Ошибка загрузки конфига биндов: " + e.getMessage());
        }
    }
}
