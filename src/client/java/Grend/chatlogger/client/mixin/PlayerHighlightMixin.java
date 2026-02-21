package Grend.chatlogger.client.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import Grend.chatlogger.client.ClanHighlightConfig;
import Grend.chatlogger.data.DataManager;
import Grend.chatlogger.data.PlayerData;

@Mixin(LivingEntityRenderer.class)
public class PlayerHighlightMixin {

    private static float[] currentColor = null;

    @Inject(
        method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("HEAD")
    )
    private void beforeRender(LivingEntityRenderState state, 
                              net.minecraft.client.util.math.MatrixStack matrixStack, 
                              VertexConsumerProvider vertexConsumerProvider, 
                              int i, CallbackInfo ci) {
        currentColor = null;

        try {
            if (!ClanHighlightConfig.getInstance().isHighlightEnabled()) {
                return;
            }

            if (state == null || state.displayName == null) {
                return;
            }

            String displayName = state.displayName.getString();
            PlayerData playerData = DataManager.getInstance().getPlayer(displayName);
            
            if (playerData == null) {
                return;
            }

            String clan = playerData.getClan();
            if (clan == null || clan.trim().isEmpty()) {
                return;
            }

            ClanHighlightConfig config = ClanHighlightConfig.getInstance();
            if (!config.hasClanColor(clan)) {
                return;
            }

            String colorHex = config.getClanColor(clan);
            currentColor = ClanHighlightConfig.hexToRgb(colorHex);
        } catch (Exception e) {
            // Игнорируем ошибки, чтобы не крашить игру
            currentColor = null;
        }
    }

    @Inject(
        method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("RETURN")
    )
    private void afterRender(LivingEntityRenderState state, 
                             net.minecraft.client.util.math.MatrixStack matrixStack, 
                             VertexConsumerProvider vertexConsumerProvider, 
                             int i, CallbackInfo ci) {
        currentColor = null;
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
