package Grend.chatlogger.client;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Конфигурация для биндов слотов
 */
public class SlotBindConfig {
    private static SlotBindConfig instance;
    private static final Path CONFIG_PATH = Paths.get("chatlogger_slotbinds.properties");

    // Целевые слоты для каждого бинда (0-8, где 0 = первый слот)
    private int[] targetSlots = new int[]{4, 5, 6, 7, 8}; // По умолчанию: слоты 5-9

    private SlotBindConfig() { load(); }

    public static synchronized SlotBindConfig getInstance() {
        if (instance == null) instance = new SlotBindConfig();
        return instance;
    }

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

    public void save() {
        try {
            Properties props = new Properties();
            for (int i = 0; i < 5; i++) {
                props.setProperty("slotBind." + i, String.valueOf(targetSlots[i]));
            }
            try (OutputStream os = Files.newOutputStream(CONFIG_PATH)) {
                props.store(os, "ChatLogger Slot Binds Configuration");
            }
        } catch (IOException e) {
            System.err.println("[ChatLogger] Ошибка сохранения конфига слотов: " + e.getMessage());
        }
    }

    public void load() {
        if (!Files.exists(CONFIG_PATH)) { save(); return; }
        try {
            Properties props = new Properties();
            try (InputStream is = Files.newInputStream(CONFIG_PATH)) { props.load(is); }
            for (int i = 0; i < 5; i++) {
                String value = props.getProperty("slotBind." + i, String.valueOf(i + 4));
                try {
                    targetSlots[i] = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    targetSlots[i] = i + 4;
                }
            }
        } catch (IOException e) {
            System.err.println("[ChatLogger] Ошибка загрузки конфига слотов: " + e.getMessage());
        }
    }
}
