package Grend.chatlogger.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Утилита для быстрой активации посохов
 * Переключает на нужный слот → имитирует ПКМ → возвращается на предыдущий слот
 */
public class StaffQuickUse {
    
    private static int lastSlot = -1;
    private static boolean isUsingStaff = false;
    
    /**
     * Быстрое использование посоха из указанного слота
     * @param slotIndex индекс слота (0-8)
     */
    public static void useStaff(int slotIndex) {
        if (slotIndex < 0 || slotIndex > 8) {
            sendMessage("§c[ChatLogger] Неверный слот: " + slotIndex);
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        
        if (player == null) {
            sendMessage("§c[ChatLogger] Игрок не найден");
            return;
        }
        
        if (isUsingStaff) {
            sendMessage("§c[ChatLogger] Уже использую посох");
            return;
        }
        
        isUsingStaff = true;
        lastSlot = player.getInventory().selectedSlot;
        
        // Сохраняем текущий слот и переключаемся на целевой
        int targetSlot = slotIndex;
        player.getInventory().selectedSlot = targetSlot;
        
        // Имитируем ПКМ (использование предмета)
        performRightClick(client, player);
        
        // Возвращаемся на предыдущий слот
        player.getInventory().selectedSlot = lastSlot;
        lastSlot = -1;
        isUsingStaff = false;
        
        sendMessage("§a[ChatLogger] Посох использован из слота " + (slotIndex + 1));
    }
    
    /**
     * Имитация нажатия ПКМ (использование предмета)
     */
    private static void performRightClick(MinecraftClient client, ClientPlayerEntity player) {
        Hand hand = Hand.MAIN_HAND;
        
        // Отправляем пакет использования предмета
        HitResult hitResult = client.crosshairTarget;
        
        if (hitResult == null) {
            // Если нет цели, просто используем предмет
            client.interactionManager.interactItem(player, hand);
            return;
        }
        
        switch (hitResult.getType()) {
            case BLOCK:
                BlockHitResult blockHit = (BlockHitResult) hitResult;
                BlockPos pos = blockHit.getBlockPos();
                Direction face = blockHit.getSide();
                
                // Сначала отменяем выбор блока (чтобы не ставить блоки)
                client.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
                    pos,
                    face
                ));
                
                // Используем предмет
                client.interactionManager.interactItem(player, hand);
                break;
                
            case ENTITY:
                EntityHitResult entityHit = (EntityHitResult) hitResult;
                client.interactionManager.interactEntityAtLocation(
                    player,
                    entityHit.getEntity(),
                    entityHit,
                    hand
                );
                break;
                
            case MISS:
            default:
                // Просто используем предмет (например, для посохов с зельями)
                client.interactionManager.interactItem(player, hand);
                break;
        }
    }
    
    /**
     * Получить последний слот, на котором был игрок
     */
    public static int getLastSlot() {
        return lastSlot;
    }
    
    /**
     * Проверка, активен ли процесс использования посоха
     */
    public static boolean isActive() {
        return isUsingStaff;
    }
    
    private static void sendMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(net.minecraft.text.Text.literal(message), false);
        }
    }
}
