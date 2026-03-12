package Grend.chatlogger.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import Grend.chatlogger.client.ClanHighlightConfig;
import Grend.chatlogger.data.DataManager;
import Grend.chatlogger.data.PlayerData;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public class PlayerHighlightMixin<L extends LivingEntity, S extends LivingEntityRenderState> {

    private static float[] currentColor = null;
    private static String lastMatchedPlayer = null;

    @Inject(
        method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("HEAD")
    )
    private void beforeRender(S state,
                              net.minecraft.client.util.math.MatrixStack matrixStack,
                              VertexConsumerProvider vertexConsumerProvider,
                              int i, CallbackInfo ci) {
        currentColor = null;
        lastMatchedPlayer = null;

        try {
            if (!ClanHighlightConfig.getInstance().isHighlightEnabled()) {
                return;
            }

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world == null || mc.player == null) {
                return;
            }

            // Получаем всех игроков в мире
            List<AbstractClientPlayerEntity> players = mc.world.getPlayers();
            
            // Пытаемся найти игрока по имени из displayName
            String displayName = state.displayName != null ? state.displayName.getString() : null;
            
            PlayerEntity targetPlayer = null;
            
            if (displayName != null && !displayName.isEmpty()) {
                // Ищем игрока по имени
                for (AbstractClientPlayerEntity player : players) {
                    if (player.getName().getString().equals(displayName)) {
                        targetPlayer = player;
                        break;
                    }
                }
            }
            
            // Если не нашли по displayName, пробуем найти по расстоянию (для отладки)
            if (targetPlayer == null) {
                for (AbstractClientPlayerEntity player : players) {
                    if (player == mc.player) continue; // Пропускаем себя
                    
                    double dist = player.squaredDistanceTo(mc.player);
                    if (dist < 100) { // В радиусе 10 блоков
                        targetPlayer = player;
                        break;
                    }
                }
            }
            
            if (targetPlayer == null) {
                return;
            }

            String playerName = targetPlayer.getName().getString();
            PlayerData playerData = DataManager.getInstance().getPlayer(playerName);

            if (playerData == null) {
                return;
            }

            String clan = playerData.getClan();
            if (clan == null || clan.trim().isEmpty() || "Без клана".equalsIgnoreCase(clan)) {
                return;
            }

            ClanHighlightConfig config = ClanHighlightConfig.getInstance();
            if (!config.hasClanColor(clan)) {
                return;
            }

            String colorHex = config.getClanColor(clan);
            currentColor = ClanHighlightConfig.hexToRgb(colorHex);
            lastMatchedPlayer = playerName;
            
            // Отладочное сообщение
            System.out.println("[ChatLogger] Подсветка игрока: " + playerName + " (клан: " + clan + ", цвет: " + colorHex + ")");
            
        } catch (Exception e) {
            // Игнорируем ошибки, чтобы не крашить игру
            currentColor = null;
            System.err.println("[ChatLogger] Ошибка подсветки: " + e.getMessage());
        }
    }

    @Inject(
        method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("RETURN")
    )
    private void afterRender(S state,
                             net.minecraft.client.util.math.MatrixStack matrixStack,
                             VertexConsumerProvider vertexConsumerProvider,
                             int i, CallbackInfo ci) {
        currentColor = null;
        lastMatchedPlayer = null;
    }

    @Redirect(
        method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;"
        )
    )
    private VertexConsumer redirectGetBuffer(VertexConsumerProvider instance, RenderLayer layer) {
        if (currentColor != null) {
            return new ColoredVertexConsumer(instance.getBuffer(layer), currentColor[0], currentColor[1], currentColor[2]);
        }
        return instance.getBuffer(layer);
    }

    public static class ColoredVertexConsumer implements VertexConsumer {
        private final VertexConsumer parent;
        private final float r, g, b;

        public ColoredVertexConsumer(VertexConsumer parent, float r, float g, float b) {
            this.parent = parent;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            return parent.vertex(x, y, z);
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            int newRed = (int)(red * r);
            int newGreen = (int)(green * g);
            int newBlue = (int)(blue * b);
            return parent.color(newRed, newGreen, newBlue, alpha);
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            return parent.texture(u, v);
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            return parent.overlay(u, v);
        }

        @Override
        public VertexConsumer light(int u, int v) {
            return parent.light(u, v);
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return parent.normal(x, y, z);
        }
    }
}
