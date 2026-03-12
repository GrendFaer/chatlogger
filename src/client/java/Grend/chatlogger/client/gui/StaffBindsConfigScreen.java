package Grend.chatlogger.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import Grend.chatlogger.client.StaffBindsConfig;
import Grend.chatlogger.client.StaffQuickUse;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Экран настройки 5 биндов для посохов
 * Можно настроить клавишу и слот для каждого бинда
 */
public class StaffBindsConfigScreen extends Screen {
    private final Screen parent;
    private final StaffBindsConfig config;
    
    private ButtonWidget[] keyButtons = new ButtonWidget[5];
    private ButtonWidget[] slotButtons = new ButtonWidget[5];
    private ButtonWidget[] enableButtons = new ButtonWidget[5];

    public StaffBindsConfigScreen(Screen parent) {
        super(Text.literal("ChatLogger - Бинды посохов"));
        this.parent = parent;
        this.config = StaffBindsConfig.getInstance();
    }

    @Override
    protected void init() {
        super.init();

        int centerX = width / 2;
        int rowHeight = 25;
        int startY = height / 4;
        
        // Заголовки
        int labelOffset = -100;

        for (int i = 0; i < 5; i++) {
            final int index = i;
            int yPos = startY + i * rowHeight;
            
            // Номер бинда
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("§fБинд §b" + (i + 1)),
                    button -> {})
                    .dimensions(centerX - 180, yPos, 60, 20)
                    .build());
            
            // Кнопка включения/выключения
            enableButtons[i] = addDrawableChild(ButtonWidget.builder(
                    config.isEnabled(i) ? Text.literal("§aВКЛ") : Text.literal("§cВЫКЛ"),
                    button -> {
                        config.toggleEnabled(index);
                        button.setMessage(config.isEnabled(index) ? Text.literal("§aВКЛ") : Text.literal("§cВЫКЛ"));
                    })
                    .dimensions(centerX - 115, yPos, 45, 20)
                    .build());
            
            // Кнопка клавиши
            keyButtons[i] = addDrawableChild(ButtonWidget.builder(
                    getKeyNameOrWaiting(index),
                    button -> {
                        config.setWaitingForBind(index);
                        updateAllButtons();
                    })
                    .dimensions(centerX - 65, yPos, 70, 20)
                    .build());
            
            // Кнопка слота
            slotButtons[i] = addDrawableChild(ButtonWidget.builder(
                    Text.literal("§eСлот §f" + (config.getTargetSlot(index) + 1)),
                    button -> {
                        int currentSlot = config.getTargetSlot(index);
                        config.setTargetSlot(index, (currentSlot + 1) % 9);
                        updateAllButtons();
                    })
                    .dimensions(centerX + 10, yPos, 70, 20)
                    .build());
            
            // Быстрые кнопки слотов (1-9)
            for (int s = 0; s < 9; s++) {
                final int slot = s;
                int slotBtnX = centerX + 85 + s * 18;
                
                ButtonWidget slotBtn = ButtonWidget.builder(
                        Text.literal(String.valueOf(s + 1)),
                        btn -> {
                            config.setTargetSlot(index, slot);
                            updateAllButtons();
                        })
                        .dimensions(slotBtnX, yPos, 16, 20)
                        .build();
                
                if (s == config.getTargetSlot(index)) {
                    slotBtn.setMessage(Text.literal("§a" + (s + 1)));
                }
                
                addDrawableChild(slotBtn);
            }
            
            // Кнопка теста
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("§bТест"),
                    button -> {
                        StaffQuickUse.useStaff(config.getTargetSlot(index));
                    })
                    .dimensions(centerX + 250, yPos, 40, 20)
                    .build());
        }
        
        // Кнопка "Назад"
        addDrawableChild(ButtonWidget.builder(
                Text.literal("§7Назад"),
                button -> MinecraftClient.getInstance().setScreen(parent))
                .dimensions(centerX - 100, startY + 5 * rowHeight + 20, 200, 20)
                .build());
        
        // Кнопка "Сбросить всё"
        addDrawableChild(ButtonWidget.builder(
                Text.literal("§cСброс"),
                button -> {
                    config.setWaitingForBind(-1);
                    // Сброс к значениям по умолчанию
                    int[] defaultKeys = new int[]{GLFW.GLFW_KEY_R, GLFW.GLFW_KEY_F, GLFW.GLFW_KEY_V, GLFW.GLFW_KEY_C, GLFW.GLFW_KEY_X};
                    int[] defaultSlots = new int[]{4, 5, 6, 7, 8};
                    for (int i = 0; i < 5; i++) {
                        config.setKeyCode(i, defaultKeys[i]);
                        config.setTargetSlot(i, defaultSlots[i]);
                        config.setEnabled(i, true);
                    }
                    config.save();
                    updateAllButtons();
                })
                .dimensions(centerX - 100, startY + 5 * rowHeight + 45, 200, 20)
                .build());
    }
    
    private Text getKeyNameOrWaiting(int index) {
        if (config.isWaitingForBind() && config.getWaitingForBind() == index) {
            return Text.literal("§eНажми...");
        }
        return Text.literal("§b" + config.getKeyName(index));
    }
    
    private void updateAllButtons() {
        for (int i = 0; i < 5; i++) {
            if (keyButtons[i] != null) {
                keyButtons[i].setMessage(getKeyNameOrWaiting(i));
            }
            if (slotButtons[i] != null) {
                slotButtons[i].setMessage(Text.literal("§eСлот §f" + (config.getTargetSlot(i) + 1)));
            }
            if (enableButtons[i] != null) {
                enableButtons[i].setMessage(config.isEnabled(i) ? Text.literal("§aВКЛ") : Text.literal("§cВЫКЛ"));
            }
        }
        // Перерисовка кнопок слотов 1-9
        init();
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Если ждём ввода клавиши
        if (config.isWaitingForBind()) {
            int waitingIndex = config.getWaitingForBind();
            
            // Escape для отмены
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                config.setWaitingForBind(-1);
                updateAllButtons();
                return true;
            }
            
            // Сохраняем новую клавишу
            if (keyCode != GLFW.GLFW_KEY_UNKNOWN) {
                config.setKeyCode(waitingIndex, keyCode);
                config.setWaitingForBind(-1);
                updateAllButtons();
            }
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // Заголовок
        context.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, 15, Formatting.GREEN.getColorValue());

        // Подсказка
        context.drawCenteredTextWithShadow(textRenderer, "§7Нажмите на кнопку клавиши чтобы изменить её",
                width / 2, height / 4 - 20, Formatting.GRAY.getColorValue());
        
        // Заголовки колонок
        int startY = height / 4;
        context.drawText(textRenderer, "§fБинд", width / 2 - 175, startY - 12, 0xFFFFFF, true);
        context.drawText(textRenderer, "§fСтатус", width / 2 - 105, startY - 12, 0xFFFFFF, true);
        context.drawText(textRenderer, "§fКлавиша", width / 2 - 55, startY - 12, 0xFFFFFF, true);
        context.drawText(textRenderer, "§fСлот", width / 2 + 20, startY - 12, 0xFFFFFF, true);
        context.drawText(textRenderer, "§fБыстрые слоты", width / 2 + 90, startY - 12, 0xFFFFFF, true);
    }
    
    @Override
    public void close() {
        config.setWaitingForBind(-1);
        config.save();
        super.close();
    }
}
