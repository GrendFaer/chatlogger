package Grend.chatlogger.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import Grend.chatlogger.data.DataManager;
import Grend.chatlogger.client.util.OnlineChecker;

import java.util.ArrayList;
import java.util.List;

/**
 * Экран списка кланов
 */
public class ClanListScreen extends Screen {
    private final Screen parent;
    private List<String> clans;
    private int scrollOffset = 0;
    private int maxVisibleClans = 6;

    public ClanListScreen(Screen parent) {
        super(Text.literal("ChatLogger - Список кланов"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        clans = new ArrayList<>(DataManager.getInstance().getAllClans());
        clans.sort(String.CASE_INSENSITIVE_ORDER);
        maxVisibleClans = (height - 120) / 28;

        int centerX = width / 2;
        int buttonWidth = 140;
        int buttonHeight = 20;

        addDrawableChild(ButtonWidget.builder(Text.literal("§eПроверить все"), button -> checkAllClans())
                .dimensions(centerX - buttonWidth - 5, height - 55, buttonWidth, buttonHeight)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§cСтоп"), button -> {
                    OnlineChecker.stopChecking();
                    if (client != null && client.player != null) {
                        client.player.sendMessage(Text.literal("§7[ChatLogger] Проверка остановлена"), false);
                    }
                })
                .dimensions(centerX + 5, height - 55, buttonWidth, buttonHeight)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§7Назад"), button -> MinecraftClient.getInstance().setScreen(parent))
                .dimensions(centerX - 75, height - 30, 150, buttonHeight)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // Заголовок
        context.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, 10, Formatting.GREEN.getColorValue());

        // Статистика
        context.drawCenteredTextWithShadow(textRenderer, "Всего кланов: " + clans.size(), width / 2, 22, Formatting.GRAY.getColorValue());

        // Список кланов
        int listX = width / 2 - 150;
        int listY = 38;
        int listWidth = 300;
        int listHeight = maxVisibleClans * 30;

        context.fill(listX - 2, listY - 2, listX + listWidth + 2, listY + listHeight + 2, 0x80000000);

        if (clans.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, "§7Кланы не найдены", width / 2, listY + 10, Formatting.GRAY.getColorValue());
            return;
        }

        int visibleCount = Math.min(clans.size() - scrollOffset, maxVisibleClans);

        for (int i = 0; i < visibleCount; i++) {
            int index = scrollOffset + i;
            if (index >= clans.size()) break;

            String clan = clans.get(index);
            int playerCount = DataManager.getInstance().getPlayersByClan(clan).size();
            int avgLevel = DataManager.getInstance().getAverageLevelByClan(clan);
            int y = listY + i * 30;

            boolean isHovered = mouseX >= listX && mouseX <= listX + listWidth &&
                               mouseY >= y && mouseY <= y + 28;
            int bgColor = isHovered ? 0x40FFFFFF : 0x20FFFFFF;
            context.fill(listX, y, listX + listWidth, y + 28, bgColor);

            // Кнопка "Check"
            int checkBtnX = listX + 5;
            boolean checkHovered = mouseX >= checkBtnX && mouseX <= checkBtnX + 40 &&
                                  mouseY >= y + 5 && mouseY <= y + 21;
            int checkBtnColor = checkHovered ? 0xFFAA8800 : 0xFF886600;
            context.fill(checkBtnX, y + 5, checkBtnX + 40, y + 21, checkBtnColor);
            context.drawCenteredTextWithShadow(textRenderer, "§eCheck", checkBtnX + 20, y + 9, 0xFFFFFF);

            // Кнопка "Удалить"
            int deleteBtnX = listX + 50;
            boolean deleteHovered = mouseX >= deleteBtnX && mouseX <= deleteBtnX + 55 &&
                                   mouseY >= y + 5 && mouseY <= y + 21;
            int deleteBtnColor = deleteHovered ? 0xFFAA0000 : 0xFF880000;
            context.fill(deleteBtnX, y + 5, deleteBtnX + 55, y + 21, deleteBtnColor);
            context.drawCenteredTextWithShadow(textRenderer, "§cУдалить", deleteBtnX + 27, y + 9, 0xFFFFFF);

            // Название клана (центр)
            context.drawText(textRenderer, "§b" + clan, listX + 120, y + 10, 0xFFFFFF, true);

            // Кол-во игроков и средний уровень
            context.drawText(textRenderer, "§7[" + playerCount + " игр. | §f" + avgLevel + "§7 lvl]", listX + 180, y + 10, 0xAAAAAA, true);

            // Кнопка "Игр."
            int playersBtnX = listX + listWidth - 75;
            boolean playersHovered = mouseX >= playersBtnX && mouseX <= playersBtnX + 50 &&
                                    mouseY >= y + 5 && mouseY <= y + 21;
            int playersBtnColor = playersHovered ? 0xFF00AA00 : 0xFF008800;
            context.fill(playersBtnX, y + 5, playersBtnX + 50, y + 21, playersBtnColor);
            context.drawCenteredTextWithShadow(textRenderer, "§aИгр.", playersBtnX + 25, y + 9, 0xFFFFFF);

            // Кнопка "Экспорт"
            int exportBtnX = listX + listWidth - 20;
            boolean exportHovered = mouseX >= exportBtnX && mouseX <= exportBtnX + 45 &&
                                   mouseY >= y + 5 && mouseY <= y + 21;
            int exportBtnColor = exportHovered ? 0xFF00AAAA : 0xFF008888;
            context.fill(exportBtnX, y + 5, exportBtnX + 45, y + 21, exportBtnColor);
            context.drawCenteredTextWithShadow(textRenderer, "§bЭкспорт", exportBtnX + 22, y + 9, 0xFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        int listX = width / 2 - 150;
        int listY = 38;
        int listWidth = 300;

        int visibleCount = Math.min(clans.size() - scrollOffset, maxVisibleClans);

        for (int i = 0; i < visibleCount; i++) {
            int index = scrollOffset + i;
            if (index >= clans.size()) break;

            String clan = clans.get(index);
            int y = listY + i * 30;

            // Кнопка "Игр."
            int playersBtnX = listX + listWidth - 75;
            if (mouseX >= playersBtnX && mouseX <= playersBtnX + 50 &&
                mouseY >= y + 5 && mouseY <= y + 21) {
                MinecraftClient.getInstance().setScreen(new ClanPlayersScreen(this, clan));
                return true;
            }

            // Кнопка "Экспорт"
            int exportBtnX = listX + listWidth - 20;
            if (mouseX >= exportBtnX && mouseX <= exportBtnX + 45 &&
                mouseY >= y + 5 && mouseY <= y + 21) {
                exportClan(clan);
                return true;
            }

            // Кнопка "Check"
            int checkBtnX = listX + 5;
            if (mouseX >= checkBtnX && mouseX <= checkBtnX + 40 &&
                mouseY >= y + 5 && mouseY <= y + 21) {
                checkClan(clan);
                return true;
            }

            // Кнопка "Удалить"
            int deleteBtnX = listX + 50;
            if (mouseX >= deleteBtnX && mouseX <= deleteBtnX + 55 &&
                mouseY >= y + 5 && mouseY <= y + 21) {
                deleteClan(clan);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (clans.size() > maxVisibleClans) {
            scrollOffset -= (int) verticalAmount;
            scrollOffset = Math.max(0, Math.min(scrollOffset, clans.size() - maxVisibleClans));
        }
        return true;
    }

    private void exportClan(String clan) {
        DataManager manager = DataManager.getInstance();
        try {
            manager.exportClanToFile(clan, java.nio.file.Paths.get("clan_" + clan + ".txt"));
            if (client != null && client.player != null) {
                client.player.sendMessage(Text.literal("§a[ChatLogger] Клан '" + clan + "' экспортирован"), false);
            }
        } catch (Exception e) {
            if (client != null && client.player != null) {
                client.player.sendMessage(Text.literal("§c[ChatLogger] Ошибка экспорта: " + e.getMessage()), false);
            }
        }
    }

    private void checkClan(String clan) {
        OnlineChecker.checkClan(clan, null);
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("§7[ChatLogger] Проверка клана: " + clan), false);
        }
    }

    private void checkAllClans() {
        OnlineChecker.checkAllPlayers();
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("§7[ChatLogger] Запущена проверка всех игроков"), false);
        }
    }

    private void deleteClan(String clan) {
        DataManager manager = DataManager.getInstance();
        manager.deleteClan(clan);
        clans.removeIf(c -> c.equalsIgnoreCase(clan));
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("§a[ChatLogger] Клан '" + clan + "' удалён"), false);
        }
    }
}
