package Grend.chatlogger.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import Grend.chatlogger.data.DataManager;
import Grend.chatlogger.client.ModKeyBindings;

/**
 * Главное меню мода ChatLogger
 */
public class MainScreen extends Screen {
    private final Screen parent;

    public MainScreen(Screen parent) {
        super(Text.literal("ChatLogger - Главное меню"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = width / 2;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int startY = height / 4;
        int gap = 22;

        addDrawableChild(ButtonWidget.builder(Text.literal("§aСписок кланов"), button -> {
                    MinecraftClient.getInstance().setScreen(new ClanListScreen(this));
                })
                .dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§bЭкспорт данных"), button -> {
                    exportAll();
                    close();
                })
                .dimensions(centerX - buttonWidth / 2, startY + gap, buttonWidth, buttonHeight)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§eПодсветка кланов"), button -> {
                    MinecraftClient.getInstance().setScreen(new HighlightConfigScreen(this));
                })
                .dimensions(centerX - buttonWidth / 2, startY + gap * 2, buttonWidth, buttonHeight)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§6Настройки"), button -> {
                    MinecraftClient.getInstance().setScreen(new ModConfigScreen(this));
                })
                .dimensions(centerX - buttonWidth / 2, startY + gap * 3, buttonWidth, buttonHeight)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§dБинды посохов"), button -> {
                    MinecraftClient.getInstance().setScreen(new StaffBindsConfigScreen(this));
                })
                .dimensions(centerX - buttonWidth / 2, startY + gap * 4, buttonWidth, buttonHeight)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§9Авто-меч"), button -> {
                    MinecraftClient.getInstance().setScreen(new SwordSwitcherScreen(this));
                })
                .dimensions(centerX - buttonWidth / 2, startY + gap * 5, buttonWidth, buttonHeight)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§5Автоотправка"), button -> {
                    MinecraftClient.getInstance().setScreen(new AutoMessageScreen(this));
                })
                .dimensions(centerX - buttonWidth / 2, startY + gap * 6, buttonWidth, buttonHeight)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§2Руны"), button -> {
                    MinecraftClient.getInstance().setScreen(new RunesScreen(this));
                })
                .dimensions(centerX - buttonWidth / 2, startY + gap * 7, buttonWidth, buttonHeight)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§cОчистить данные"), button -> {
                    clearData();
                    close();
                })
                .dimensions(centerX - buttonWidth / 2, startY + gap * 8, buttonWidth, buttonHeight)
                .build());

        String keyName = ModKeyBindings.openGuiKey.getBoundKeyLocalizedText().getString();
        addDrawableChild(ButtonWidget.builder(Text.literal("§fКлавиша: §b" + keyName), button -> {
                    MinecraftClient.getInstance().setScreen(new KeyBindingScreen(this));
                })
                .dimensions(centerX - buttonWidth / 2, startY + gap * 9, buttonWidth, buttonHeight)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§7Назад"), button -> {
                    if (parent != null) {
                        MinecraftClient.getInstance().setScreen(parent);
                    } else {
                        close();
                    }
                })
                .dimensions(centerX - buttonWidth / 2, height - 35, buttonWidth, buttonHeight)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // Заголовок
        context.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, 15, Formatting.GREEN.getColorValue());

        // Статистика - с большим отступом
        int playerCount = DataManager.getInstance().getPlayerCount();
        int clanCount = DataManager.getInstance().getAllClans().size();
        String stats = String.format("Всего игроков: %d | Всего кланов: %d", playerCount, clanCount);
        context.drawCenteredTextWithShadow(textRenderer, stats, width / 2, 30, Formatting.GRAY.getColorValue());
    }

    private void exportAll() {
        DataManager manager = DataManager.getInstance();
        try {
            manager.exportToFile(manager.getExportPath());
            if (client != null && client.player != null) {
                client.player.sendMessage(Text.literal("§a[ChatLogger] Данные экспортированы в " + manager.getExportPath().toAbsolutePath()), false);
            }
        } catch (Exception e) {
            if (client != null && client.player != null) {
                client.player.sendMessage(Text.literal("§c[ChatLogger] Ошибка экспорта: " + e.getMessage()), false);
            }
        }
    }

    private void clearData() {
        DataManager.getInstance().clear();
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("§a[ChatLogger] Данные очищены"), false);
        }
    }
}
