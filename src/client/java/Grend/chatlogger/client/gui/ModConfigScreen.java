package Grend.chatlogger.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import Grend.chatlogger.client.ModConfig;

/**
 * Экран общих настроек мода
 */
public class ModConfigScreen extends Screen {
    private final Screen parent;
    private ModConfig config;
    private TextFieldWidget tellCommandField;
    private TextFieldWidget tellMessageField;
    private TextFieldWidget clanChatSymbolField;

    public ModConfigScreen(Screen parent) {
        super(Text.literal("ChatLogger - Настройки"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
    }

    @Override
    protected void init() {
        super.init();

        int centerX = width / 2;
        int fieldWidth = 250;
        int fieldHeight = 18;
        int startY = height / 4;
        int gap = 45;

        tellCommandField = new TextFieldWidget(textRenderer, centerX - fieldWidth / 2, startY, fieldWidth, fieldHeight, Text.literal(""));
        tellCommandField.setText(config.getTellCommand());
        tellCommandField.setMaxLength(16);
        addDrawableChild(tellCommandField);

        tellMessageField = new TextFieldWidget(textRenderer, centerX - fieldWidth / 2, startY + gap, fieldWidth, fieldHeight, Text.literal(""));
        tellMessageField.setText(config.getTellMessage());
        tellMessageField.setMaxLength(64);
        addDrawableChild(tellMessageField);

        clanChatSymbolField = new TextFieldWidget(textRenderer, centerX - fieldWidth / 2, startY + gap * 2, fieldWidth, fieldHeight, Text.literal(""));
        clanChatSymbolField.setText(config.getClanChatSymbol());
        clanChatSymbolField.setMaxLength(4);
        addDrawableChild(clanChatSymbolField);

        addDrawableChild(ButtonWidget.builder(Text.literal("§aСохранить"), button -> saveSettings())
                .dimensions(centerX - 130, startY + gap * 3, 120, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§cСбросить"), button -> resetSettings())
                .dimensions(centerX + 10, startY + gap * 3, 120, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§7Назад"), button -> MinecraftClient.getInstance().setScreen(parent))
                .dimensions(centerX - 75, height - 35, 150, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // Заголовок
        context.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, 20, Formatting.GREEN.getColorValue());

        int labelColor = Formatting.GRAY.getColorValue();
        int centerX = width / 2;
        int startY = height / 4;
        int gap = 45;

        // Подписи к полям
        context.drawCenteredTextWithShadow(textRenderer, "Команда для ЛС (без /)", centerX, startY - 10, labelColor);
        context.drawCenteredTextWithShadow(textRenderer, "Сообщение для проверки", centerX, startY + gap - 10, labelColor);
        context.drawCenteredTextWithShadow(textRenderer, "Символ для клан-чата", centerX, startY + gap * 2 - 10, labelColor);

        // Подсказки под полями
        context.drawCenteredTextWithShadow(textRenderer, "§7Например: m, tell, msg", centerX, startY + 30, 0x666666);
        context.drawCenteredTextWithShadow(textRenderer, "§7Например: 1, check", centerX, startY + gap + 30, 0x666666);
        context.drawCenteredTextWithShadow(textRenderer, "§7Например: @, !, #", centerX, startY + gap * 2 + 30, 0x666666);
    }

    private void saveSettings() {
        String tellCommand = tellCommandField.getText().trim();
        String tellMessage = tellMessageField.getText().trim();
        String clanChatSymbol = clanChatSymbolField.getText().trim();

        if (!tellCommand.isEmpty()) {
            config.setTellCommand(tellCommand);
        }
        if (!tellMessage.isEmpty()) {
            config.setTellMessage(tellMessage);
        }
        if (!clanChatSymbol.isEmpty()) {
            config.setClanChatSymbol(clanChatSymbol.substring(0, 1));
        }

        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("§a[ChatLogger] Настройки сохранены"), false);
        }
    }

    private void resetSettings() {
        config.setTellCommand("m");
        config.setTellMessage("1");
        config.setClanChatSymbol("@");

        tellCommandField.setText("m");
        tellMessageField.setText("1");
        clanChatSymbolField.setText("@");

        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("§a[ChatLogger] Настройки сброшены"), false);
        }
    }
}
