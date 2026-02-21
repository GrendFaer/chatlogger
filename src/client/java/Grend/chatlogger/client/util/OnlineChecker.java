package Grend.chatlogger.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import Grend.chatlogger.data.*;
import Grend.chatlogger.client.ModConfig;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Утилита для проверки онлайна игроков через отправку ЛС
 */
public class OnlineChecker {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final DataManager manager = DataManager.getInstance();
    private static final ModConfig config = ModConfig.getInstance();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    // Ожидание между проверками (мс)
    private static final int CHECK_DELAY = 1000;
    
    // Список игроков на проверку
    private static List<String> checkQueue = new ArrayList<>();
    private static int currentIndex = 0;
    private static boolean isChecking = false;

    /**
     * Останавливает текущую проверку
     */
    public static void stopChecking() {
        isChecking = false;
        checkQueue.clear();
        currentIndex = 0;
        sendMessage("§7[ChatLogger] Проверка остановлена пользователем");
    }
    // Callback после проверки клана
    private static Runnable clanCheckCallback = null;
    private static String clanCheckClanName = null;
    
    // Последний проверяемый игрок
    private static String lastCheckedPlayer = null;

    /**
     * Запускает проверку всех игроков
     */
    public static void checkAllPlayers() {
        if (isChecking) {
            sendMessage("§7[ChatLogger] Проверка уже выполняется...");
            return;
        }
        
        checkQueue.clear();
        for (PlayerData player : manager.getAllPlayers()) {
            checkQueue.add(player.getNickname());
        }
        
        if (checkQueue.isEmpty()) {
            sendMessage("§7[ChatLogger] Нет игроков для проверки");
            return;
        }
        
        // Сначала помечаем всех как оффлайн
        manager.markAllOffline();
        
        sendMessage("§a[ChatLogger] Начинаю проверку " + checkQueue.size() + " игроков через ЛС...");
        currentIndex = 0;
        isChecking = true;
        
        scheduler.schedule(OnlineChecker::checkNextPlayer, 100, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Запускает проверку игроков конкретного клана
     */
    public static void checkClan(String clan, Runnable callback) {
        if (isChecking) {
            sendMessage("§7[ChatLogger] Проверка уже выполняется...");
            if (callback != null) callback.run();
            return;
        }
        
        List<PlayerData> clanPlayers = manager.getPlayersByClan(clan);
        if (clanPlayers.isEmpty()) {
            sendMessage("§7[ChatLogger] Клан '" + clan + "' не найден или пуст");
            if (callback != null) callback.run();
            return;
        }
        
        checkQueue.clear();
        for (PlayerData player : clanPlayers) {
            checkQueue.add(player.getNickname());
        }
        
        // Помечаем игроков клана как оффлайн
        for (PlayerData player : clanPlayers) {
            player.markOffline();
        }
        
        clanCheckClanName = clan;
        clanCheckCallback = callback;
        
        sendMessage("§a[ChatLogger] Проверка клана '" + clan + "' (" + checkQueue.size() + " игроков) через ЛС...");
        currentIndex = 0;
        isChecking = true;
        
        scheduler.schedule(OnlineChecker::checkNextPlayer, 100, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Проверяет следующего игрока
     */
    private static void checkNextPlayer() {
        if (currentIndex >= checkQueue.size()) {
            isChecking = false;
            checkQueue.clear();
            
            // Показываем результат
            if (clanCheckClanName != null) {
                int online = 0;
                for (PlayerData p : manager.getPlayersByClan(clanCheckClanName)) {
                    if (p.isOnline()) online++;
                }
                sendMessage("§a[ChatLogger] Проверка клана завершена! Онлайн: " + online + " / " + manager.getPlayersByClan(clanCheckClanName).size());
                
                if (clanCheckCallback != null) {
                    clanCheckCallback.run();
                }
                
                clanCheckCallback = null;
                clanCheckClanName = null;
            } else {
                sendMessage("§a[ChatLogger] Проверка завершена!");
                int online = 0;
                for (PlayerData p : manager.getAllPlayers()) {
                    if (p.isOnline()) online++;
                }
                sendMessage("§aОнлайн: " + online + " / " + manager.getPlayerCount());
            }
            return;
        }
        
        String nickname = checkQueue.get(currentIndex);
        lastCheckedPlayer = nickname;
        sendTellMessage(nickname);
        
        currentIndex++;
        scheduler.schedule(OnlineChecker::checkNextPlayer, CHECK_DELAY, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Отправляет ЛС игроку (команду от имени игрока)
     */
    private static void sendTellMessage(String nickname) {
        if (mc.player == null || mc.player.networkHandler == null) return;

        // Отправляем как команду (сервер воспримет как команду от игрока)
        mc.player.networkHandler.sendChatCommand(config.buildTellCommand(nickname));
    }
    
    /**
     * Обработка ответа сервера
     */
    public static void handleServerResponse(String message) {
        if (!isChecking || lastCheckedPlayer == null) return;

        // Игнорируем сообщения от ChatLogger
        if (message.contains("[ChatLogger")) return;

        // Игнорируем сообщения которые сами отправили
        String sentCommand = config.buildTellCommand(lastCheckedPlayer);
        if (message.contains("/" + sentCommand) || message.contains(sentCommand)) return;

        // Игрок оффлайн если "не найден"
        boolean isOffline = message.contains("не найден") ||
                           message.contains("не существует") ||
                           message.contains("offline");

        // Игрок онлайн если видим префикс ЛС
        boolean isOnline = message.contains("ЛС | Я »") ||
                          message.contains("»") ||
                          message.contains("не может получить");

        if (isOffline || isOnline) {
            PlayerData player = manager.getPlayer(lastCheckedPlayer);
            if (player != null) {
                player.setOnline(isOnline);
            }

            if (isOnline) {
                sendMessage("§a[ChatLogger] " + lastCheckedPlayer + " - онлайн");
            } else {
                sendMessage("§7[ChatLogger] " + lastCheckedPlayer + " - оффлайн");
            }

            lastCheckedPlayer = null;
        }
    }
    
    /**
     * Проверяет конкретного игрока
     */
    public static void checkPlayer(String nickname) {
        if (mc.player == null) return;
        
        sendMessage("§7[ChatLogger] Проверка игрока: " + nickname);
        lastCheckedPlayer = nickname;
        isChecking = true;
        sendTellMessage(nickname);
    }
    
    private static void sendMessage(String text) {
        if (mc.player != null) {
            mc.execute(() -> mc.player.sendMessage(Text.literal(text), false));
        }
    }
}
