package Grend.chatlogger.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import Grend.chatlogger.client.SwordSwitcher;
import Grend.chatlogger.client.SwordSwitcherConfig;

/**
 * Экран настройки авто-переключения на меч
 */
public class SwordSwitcherScreen extends Screen {
    private final Screen parent;
    private final SwordSwitcherConfig config;
    
    private ButtonWidget enableButton;
    private ButtonWidget priorityButton;
    private ButtonWidget messagesButton;
    private ButtonWidget delayButton;
    private ButtonWidget slotInfoButton;

    public SwordSwitcherScreen(Screen parent) {
        super(Text.literal("ChatLogger - Авто-меч"));
        this.parent = parent;
        this.config = SwordSwitcherConfig.getInstance();
    }

    @Override
    protected void init() {
        super.init();

        int centerX = width / 2;
        int buttonWidth = 250;
        int buttonHeight = 20;
        int startY = height / 4;
        int gap = 24;

        // Кнопка включения/выключения
        enableButton = addDrawableChild(ButtonWidget.builder(
                config.isEnabled() ? Text.literal("§aСтатус: §fВКЛ") : Text.literal("§cСтатус: §fВЫКЛ"),
                button -> {
                    config.setEnabled(!config.isEnabled());
                    updateButtons();
                })
                .dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight)
                .build());

        // Кнопка приоритета меча
        priorityButton = addDrawableChild(ButtonWidget.builder(
                config.isNetheritePriority() ? Text.literal("§eПриоритет: §fНезеритовый") : Text.literal("§eПриоритет: §fАлмазный"),
                button -> {
                    config.setNetheritePriority(!config.isNetheritePriority());
                    updateButtons();
                })
                .dimensions(centerX - buttonWidth / 2, startY + gap, buttonWidth, buttonHeight)
                .build());

        // Кнопка сообщений
        messagesButton = addDrawableChild(ButtonWidget.builder(
                config.isShowMessages() ? Text.literal("§bСообщения: §fВКЛ") : Text.literal("§7Сообщения: §fВЫКЛ"),
                button -> {
                    config.setShowMessages(!config.isShowMessages());
                    updateButtons();
                })
                .dimensions(centerX - buttonWidth / 2, startY + gap * 2, buttonWidth, buttonHeight)
                .build());

        // Кнопка задержки
        delayButton = addDrawableChild(ButtonWidget.builder(
                Text.literal("§6Задержка: §f" + config.getSwitchDelay() + " тиков"),
                button -> {
                    int delay = config.getSwitchDelay();
                    config.setSwitchDelay(delay >= 20 ? 0 : delay + 5);
                    updateButtons();
                })
                .dimensions(centerX - buttonWidth / 2, startY + gap * 3, buttonWidth, buttonHeight)
                .build());

        // Кнопка теста
        addDrawableChild(ButtonWidget.builder(
                Text.literal("§aТест переключения"),
                button -> {
                    SwordSwitcher.forceSwitch();
                })
                .dimensions(centerX - buttonWidth / 2, startY + gap * 4, buttonWidth, buttonHeight)
                .build());

        // Информация о текущем слоте
        updateSlotInfoButton();
        
        // Кнопка "Назад"
        addDrawableChild(ButtonWidget.builder(
                Text.literal("§7Назад"),
                button -> MinecraftClient.getInstance().setScreen(parent))
                .dimensions(centerX - buttonWidth / 2, height - 35, buttonWidth, buttonHeight)
                .build());
    }
    
    private void updateSlotInfoButton() {
        int currentSlot = SwordSwitcher.getCurrentSwordSlot();
        String slotInfo = currentSlot != -1 ?
            "§fТекущий слот меча: §b" + (currentSlot + 1) :
            "§cМеч не найден в горячей панели";
        
        if (slotInfoButton != null) {
            slotInfoButton.setMessage(Text.literal(slotInfo));
        } else {
            int centerX = width / 2;
            int buttonWidth = 250;
            int buttonHeight = 20;
            int startY = height / 4;
            int gap = 24;
            
            slotInfoButton = addDrawableChild(ButtonWidget.builder(
                    Text.literal(slotInfo),
                    button -> {})
                    .dimensions(centerX - buttonWidth / 2, startY + gap * 5, buttonWidth, buttonHeight)
                    .build());
        }
    }
    
    private void updateButtons() {
        if (enableButton != null) {
            enableButton.setMessage(config.isEnabled() ? Text.literal("§aСтатус: §fВКЛ") : Text.literal("§cСтатус: §fВЫКЛ"));
        }
        if (priorityButton != null) {
            priorityButton.setMessage(config.isNetheritePriority() ? 
                Text.literal("§eПриоритет: §fНезеритовый") : 
                Text.literal("§eПриоритет: §fАлмазный"));
        }
        if (messagesButton != null) {
            messagesButton.setMessage(config.isShowMessages() ? 
                Text.literal("§bСообщения: §fВКЛ") : 
                Text.literal("§7Сообщения: §fВЫКЛ"));
        }
        if (delayButton != null) {
            delayButton.setMessage(Text.literal("§6Задержка: §f" + config.getSwitchDelay() + " тиков"));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // Заголовок
        context.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, 15, Formatting.GREEN.getColorValue());

        // Описание
        context.drawCenteredTextWithShadow(textRenderer, "§7Автоматически переключается на меч",
                width / 2, height / 4 - 20, Formatting.GRAY.getColorValue());

        context.drawCenteredTextWithShadow(textRenderer, "§7при изменении его слота (руна противника)",
                width / 2, height / 4 - 8, Formatting.GRAY.getColorValue());

        // Подсказка о задержке
        if (config.getSwitchDelay() > 0) {
            context.drawCenteredTextWithShadow(textRenderer, "§7Задержка: " + (config.getSwitchDelay() / 20.0) + " сек",
                    width / 2, height / 4 + 72 - 8, Formatting.GRAY.getColorValue());
        }
    }

    @Override
    public void tick() {
        // Обновление кнопки с текущим слотом меча
        if (client != null && client.player != null) {
            updateSlotInfoButton();
        }
    }
}
