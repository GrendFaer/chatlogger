package Grend.chatlogger.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

/**
 * Авто-переключение на меч при изменении его слота
 */
public class SwordSwitcher {
    
    private static int lastKnownSwordSlot = -1; // Последний известный слот меча
    private static int currentHotbarSlot = -1;  // Текущий слот игрока
    private static int tickCounter = 0;
    
    /**
     * Поиск меча и переключение только если слот меча изменился
     * (например, от руны противника), а не от ручного переключения
     */
    public static int findAndSwitchToSword() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        
        if (player == null) return -1;
        
        PlayerInventory inventory = player.getInventory();
        SwordSwitcherConfig config = SwordSwitcherConfig.getInstance();
        
        int netheriteSlot = -1;
        int diamondSlot = -1;
        
        // Ищем мечи в горячей панели (слоты 0-8)
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStack(i);
            Item item = stack.getItem();
            
            if (item == Items.NETHERITE_SWORD) {
                netheriteSlot = i;
            } else if (item == Items.DIAMOND_SWORD) {
                diamondSlot = i;
            }
        }
        
        // Определяем текущий приоритетный слот с мечом
        int currentSwordSlot = -1;
        if (config.isNetheritePriority()) {
            if (netheriteSlot != -1) {
                currentSwordSlot = netheriteSlot;
            } else if (diamondSlot != -1) {
                currentSwordSlot = diamondSlot;
            }
        } else {
            if (diamondSlot != -1) {
                currentSwordSlot = diamondSlot;
            } else if (netheriteSlot != -1) {
                currentSwordSlot = netheriteSlot;
            }
        }
        
        // Проверяем, изменился ли слот меча
        if (currentSwordSlot != -1) {
            // Если слот меча изменился (не совпадает с последним известным)
            if (lastKnownSwordSlot != -1 && currentSwordSlot != lastKnownSwordSlot) {
                // Меч переместился! Переключаемся на него
                if (currentSwordSlot != inventory.selectedSlot) {
                    // Проверка задержки
                    if (config.getSwitchDelay() > 0) {
                        tickCounter++;
                        if (tickCounter < config.getSwitchDelay()) {
                            return currentSwordSlot;
                        }
                        tickCounter = 0;
                    }
                    
                    inventory.selectedSlot = currentSwordSlot;
                    
                    if (config.isShowMessages()) {
                        String swordType = (currentSwordSlot == netheriteSlot) ? "Незеритовый" : "Алмазный";
                        sendMessage("§a[ChatLogger] Меч перемещён! Переключено на " + swordType + " меч (слот " + (currentSwordSlot + 1) + ")");
                    }
                }
            }
            // Обновляем последний известный слот меча
            lastKnownSwordSlot = currentSwordSlot;
        } else {
            // Меч не найден
            if (lastKnownSwordSlot != -1 && config.isShowMessages()) {
                sendMessage("§c[ChatLogger] Меч пропал из горячей панели!");
            }
            lastKnownSwordSlot = -1;
        }
        
        return currentSwordSlot;
    }
    
    /**
     * Проверка наличия меча в горячей панели
     */
    public static boolean hasSwordInHotbar() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        
        if (player == null) return false;
        
        PlayerInventory inventory = player.getInventory();
        
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStack(i);
            Item item = stack.getItem();
            
            if (item == Items.NETHERITE_SWORD || item == Items.DIAMOND_SWORD) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Получить текущий слот с мечом
     */
    public static int getCurrentSwordSlot() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        
        if (player == null) return -1;
        
        PlayerInventory inventory = player.getInventory();
        SwordSwitcherConfig config = SwordSwitcherConfig.getInstance();
        
        int netheriteSlot = -1;
        int diamondSlot = -1;
        
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStack(i);
            Item item = stack.getItem();
            
            if (item == Items.NETHERITE_SWORD) {
                netheriteSlot = i;
            } else if (item == Items.DIAMOND_SWORD) {
                diamondSlot = i;
            }
        }
        
        if (config.isNetheritePriority()) {
            return netheriteSlot != -1 ? netheriteSlot : diamondSlot;
        } else {
            return diamondSlot != -1 ? diamondSlot : netheriteSlot;
        }
    }
    
    /**
     * Принудительное переключение на меч
     */
    public static void forceSwitch() {
        int slot = getCurrentSwordSlot();
        if (slot != -1) {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;
            
            if (player != null) {
                player.getInventory().selectedSlot = slot;
                SwordSwitcherConfig config = SwordSwitcherConfig.getInstance();
                if (config.isShowMessages()) {
                    sendMessage("§a[ChatLogger] Принудительное переключение на меч (слот " + (slot + 1) + ")");
                }
            }
        } else {
            sendMessage("§c[ChatLogger] Меч не найден в горячей панели!");
        }
    }
    
    /**
     * Сбросить последний известный слот (для инициализации)
     */
    public static void resetLastKnownSlot() {
        lastKnownSwordSlot = -1;
    }
    
    private static void sendMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal(message), false);
        }
    }
}
