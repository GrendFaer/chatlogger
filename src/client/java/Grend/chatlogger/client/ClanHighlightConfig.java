package Grend.chatlogger.client;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ClanHighlightConfig {
    private static ClanHighlightConfig instance;
    private static final Path CONFIG_PATH = Paths.get("chatlogger_highlight_config.properties");

    private boolean highlightEnabled = true;
    private final Map<String, String> clanColors = new HashMap<>();

    private ClanHighlightConfig() { load(); }

    public static synchronized ClanHighlightConfig getInstance() {
        if (instance == null) instance = new ClanHighlightConfig();
        return instance;
    }

    public boolean isHighlightEnabled() { return highlightEnabled; }
    public void setHighlightEnabled(boolean enabled) { 
        this.highlightEnabled = enabled; 
        save();
    }

    public String getClanColor(String clan) { 
        return clanColors.get(clan.toUpperCase().trim()); 
    }

    public void setClanColor(String clan, String color) { 
        clanColors.put(clan.toUpperCase().trim(), color); 
        save();
    }

    public void removeClanColor(String clan) { 
        clanColors.remove(clan.toUpperCase().trim()); 
        save();
    }

    public Map<String, String> getAllClanColors() { 
        return new HashMap<>(clanColors); 
    }

    public boolean hasClanColor(String clan) {
        return clanColors.containsKey(clan.toUpperCase().trim());
    }

    public void save() {
        try {
            Properties props = new Properties();
            props.setProperty("highlightEnabled", String.valueOf(highlightEnabled));
            System.out.println("[ChatLogger] Сохранение цветов кланов:");
            for (Map.Entry<String, String> entry : clanColors.entrySet()) {
                String key = "clan.color." + entry.getKey();
                props.setProperty(key, entry.getValue());
                System.out.println("  " + entry.getKey() + " = " + entry.getValue());
            }
            try (OutputStream os = Files.newOutputStream(CONFIG_PATH)) {
                props.store(os, "ChatLogger Highlight Configuration");
            }
        } catch (IOException e) {
            System.err.println("[ChatLogger] Ошибка сохранения конфига подсветки: " + e.getMessage());
        }
    }

    public void load() {
        if (!Files.exists(CONFIG_PATH)) { save(); return; }
        try {
            Properties props = new Properties();
            try (InputStream is = Files.newInputStream(CONFIG_PATH)) { props.load(is); }
            highlightEnabled = Boolean.parseBoolean(props.getProperty("highlightEnabled", "true"));
            clanColors.clear();
            System.out.println("[ChatLogger] Загрузка цветов кланов:");
            for (String key : props.stringPropertyNames()) {
                if (key.startsWith("clan.color.")) {
                    String clan = key.substring("clan.color.".length());
                    String color = props.getProperty(key);
                    clanColors.put(clan.toUpperCase(), color);
                    System.out.println("  " + clan + " = " + color);
                }
            }
        } catch (IOException e) {
            System.err.println("[ChatLogger] Ошибка загрузки конфига подсветки: " + e.getMessage());
        }
    }

    /**
     * Преобразует название цвета в hex-формат
     */
    public static String normalizeColor(String color) {
        String lower = color.toLowerCase().trim();
        return switch (lower) {
            case "white", "белый" -> "#FFFFFF";
            case "red", "красный" -> "#FF0000";
            case "green", "зелёный", "зеленый" -> "#00FF00";
            case "blue", "синий" -> "#0000FF";
            case "yellow", "жёлтый", "желтый" -> "#FFFF00";
            case "cyan", "голубой" -> "#00FFFF";
            case "magenta", "фиолетовый" -> "#FF00FF";
            case "black", "чёрный", "черный" -> "#000000";
            case "gray", "серый" -> "#808080";
            case "orange", "оранжевый" -> "#FFA500";
            case "pink", "розовый" -> "#FFC0CB";
            case "purple", "пурпурный" -> "#800080";
            case "lime", "лайм" -> "#00FF00";
            case "teal", "бирюзовый" -> "#008080";
            case "navy", "тёмно-синий", "темно-синий" -> "#000080";
            case "brown", "коричневый" -> "#A52A2A";
            case "gold", "золотой" -> "#FFD700";
            case "silver", "серебряный" -> "#C0C0C0";
            default -> {
                // Проверяем, является ли строка hex-цветом
                if (color.matches("^#?[0-9A-Fa-f]{6}$")) {
                    yield color.startsWith("#") ? color : "#" + color;
                }
                yield "#FFFFFF"; // цвет по умолчанию
            }
        };
    }

    /**
     * Преобразует hex-цвет в RGB массив [r, g, b]
     */
    public static float[] hexToRgb(String hex) {
        if (hex == null || !hex.matches("^#?[0-9A-Fa-f]{6}$")) {
            return new float[]{1.0f, 1.0f, 1.0f};
        }
        String cleanHex = hex.startsWith("#") ? hex.substring(1) : hex;
        int r = Integer.parseInt(cleanHex.substring(0, 2), 16);
        int g = Integer.parseInt(cleanHex.substring(2, 4), 16);
        int b = Integer.parseInt(cleanHex.substring(4, 6), 16);
        return new float[]{r / 255.0f, g / 255.0f, b / 255.0f};
    }
}
