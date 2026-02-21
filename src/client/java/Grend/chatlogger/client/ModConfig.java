package Grend.chatlogger.client;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class ModConfig {
    private static ModConfig instance;
    private static final Path CONFIG_PATH = Paths.get("chatlogger_config.properties");

    private String tellCommand = "m";
    private String tellMessage = "1";
    private String clanChatSymbol = "@";

    private ModConfig() { load(); }

    public static synchronized ModConfig getInstance() {
        if (instance == null) instance = new ModConfig();
        return instance;
    }

    public String getTellCommand() { return tellCommand; }
    public void setTellCommand(String tellCommand) { this.tellCommand = tellCommand; save(); }
    public String getTellMessage() { return tellMessage; }
    public void setTellMessage(String tellMessage) { this.tellMessage = tellMessage; save(); }
    public String getClanChatSymbol() { return clanChatSymbol; }
    public void setClanChatSymbol(String clanChatSymbol) { this.clanChatSymbol = clanChatSymbol; save(); }

    public String buildTellCommand(String nickname) { return tellCommand + " " + nickname + " " + tellMessage; }

    public void save() {
        try {
            Properties props = new Properties();
            props.setProperty("tellCommand", tellCommand);
            props.setProperty("tellMessage", tellMessage);
            props.setProperty("clanChatSymbol", clanChatSymbol);
            try (OutputStream os = Files.newOutputStream(CONFIG_PATH)) {
                props.store(os, "ChatLogger Configuration");
            }
        } catch (IOException e) {
            System.err.println("[ChatLogger] Ошибка сохранения конфига: " + e.getMessage());
        }
    }

    public void load() {
        if (!Files.exists(CONFIG_PATH)) { save(); return; }
        try {
            Properties props = new Properties();
            try (InputStream is = Files.newInputStream(CONFIG_PATH)) { props.load(is); }
            tellCommand = props.getProperty("tellCommand", "m");
            tellMessage = props.getProperty("tellMessage", "1");
            clanChatSymbol = props.getProperty("clanChatSymbol", "@");
        } catch (IOException e) {
            System.err.println("[ChatLogger] Ошибка загрузки конфига: " + e.getMessage());
        }
    }
}
