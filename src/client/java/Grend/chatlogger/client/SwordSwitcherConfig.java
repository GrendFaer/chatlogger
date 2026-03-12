package Grend.chatlogger.client;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Конфигурация для авто-переключения на меч
 */
public class SwordSwitcherConfig {
    private static SwordSwitcherConfig instance;
    private static final Path CONFIG_PATH = Paths.get("chatlogger_sword_switcher.properties");

    // Включено ли авто-переключение
    private boolean enabled = true;
    
    // Приоритет меча: true = незеритовый первым, false = алмазный первым
    private boolean netheritePriority = true;
    
    // Показывать сообщения о переключении
    private boolean showMessages = true;
    
    // Задержка перед переключением (в тиках, 0 = мгновенно)
    private int switchDelay = 0;

    private SwordSwitcherConfig() { load(); }

    public static synchronized SwordSwitcherConfig getInstance() {
        if (instance == null) instance = new SwordSwitcherConfig();
        return instance;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public boolean isNetheritePriority() {
        return netheritePriority;
    }

    public void setNetheritePriority(boolean netheritePriority) {
        this.netheritePriority = netheritePriority;
        save();
    }

    public boolean isShowMessages() {
        return showMessages;
    }

    public void setShowMessages(boolean showMessages) {
        this.showMessages = showMessages;
        save();
    }

    public int getSwitchDelay() {
        return switchDelay;
    }

    public void setSwitchDelay(int switchDelay) {
        this.switchDelay = Math.max(0, switchDelay);
        save();
    }

    public void save() {
        try {
            Properties props = new Properties();
            props.setProperty("enabled", String.valueOf(enabled));
            props.setProperty("netheritePriority", String.valueOf(netheritePriority));
            props.setProperty("showMessages", String.valueOf(showMessages));
            props.setProperty("switchDelay", String.valueOf(switchDelay));
            try (OutputStream os = Files.newOutputStream(CONFIG_PATH)) {
                props.store(os, "ChatLogger Sword Switcher Configuration");
            }
        } catch (IOException e) {
            System.err.println("[ChatLogger] Ошибка сохранения конфига меча: " + e.getMessage());
        }
    }

    public void load() {
        if (!Files.exists(CONFIG_PATH)) { save(); return; }
        try {
            Properties props = new Properties();
            try (InputStream is = Files.newInputStream(CONFIG_PATH)) { props.load(is); }
            
            enabled = Boolean.parseBoolean(props.getProperty("enabled", "true"));
            netheritePriority = Boolean.parseBoolean(props.getProperty("netheritePriority", "true"));
            showMessages = Boolean.parseBoolean(props.getProperty("showMessages", "true"));
            
            String delayValue = props.getProperty("switchDelay", "0");
            try {
                switchDelay = Integer.parseInt(delayValue);
            } catch (NumberFormatException e) {
                switchDelay = 0;
            }
        } catch (IOException e) {
            System.err.println("[ChatLogger] Ошибка загрузки конфига меча: " + e.getMessage());
        }
    }
}
