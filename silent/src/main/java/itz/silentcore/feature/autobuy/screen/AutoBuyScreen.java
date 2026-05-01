package itz.silentcore.feature.autobuy.screen;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.SilentCore;
import itz.silentcore.feature.autobuy.items.AutoBuyableItem;
import itz.silentcore.feature.autobuy.manager.AutoBuyManager;
import itz.silentcore.feature.event.impl.TickEvent;
import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.Fonts;
import itz.silentcore.utils.render.RenderContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoBuyScreen extends Screen implements IMinecraft {
    public static final AutoBuyScreen INSTANCE = new AutoBuyScreen();
    
    private final Animation openAnimation = new Animation(300, Easing.CUBIC_OUT);
    private final AutoBuyManager autoBuyManager = AutoBuyManager.getInstance();
    
    private float x, y, width = 600, height = 450;
    private float scroll = 0f;
    
    private AutoBuyableItem selectedItem = null;
    private final Map<AutoBuyableItem, Integer> customPrices = new HashMap<>();
    private String priceInput = "";
    private boolean isEditingPrice = false;
    
    private static final float ITEM_SIZE = 36;
    private static final float ITEM_SPACING = 8;
    private static final int ITEMS_PER_ROW = 12;
    
    private AutoBuyScreen() {
        super(Text.of("AutoBuy"));
    }
    
    public void openGui() {
        openAnimation.animate(1);
        mc.setScreen(this);
        SilentCore.getInstance().eventBus.register(this);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        openAnimation.update();
        
        if (openAnimation.getValue() < 0.01f) {
            super.close();
            return;
        }
        
        MatrixStack ms = context.getMatrices();
        RenderContext renderContext = new RenderContext(context);
        
        // Затемнение
        renderContext.drawRect(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), 0, 
            ColorRGBA.of(0, 0, 0, (int)(140 * openAnimation.getValue())));
        
        ms.push();
        
        float centerX = context.getScaledWindowWidth() / 2f;
        float centerY = context.getScaledWindowHeight() / 2f;
        
        ms.translate(centerX, centerY, 0);
        float scale = openAnimation.getValue();
        ms.scale(scale, scale, 1);
        ms.translate(-width / 2f, -height / 2f, 0);
        
        x = 0;
        y = 0;
        
        // Трансформируем координаты мыши для правильного определения hover
        float transformedMouseX = (float)((mouseX - centerX) / scale + width / 2f);
        float transformedMouseY = (float)((mouseY - centerY) / scale + height / 2f);
        
        // Фон
        renderContext.drawRect(x, y, width, height, 12, ColorRGBA.of(20, 20, 25, 255));
        
        // Заголовок
        renderHeader(renderContext, transformedMouseX, transformedMouseY);
        
        // Список предметов
        renderItemsList(context, renderContext, transformedMouseX, transformedMouseY);
        
        // Панель настроек
        if (selectedItem != null) {
            renderSettingsPanel(context, renderContext, transformedMouseX, transformedMouseY);
        }
        
        ms.pop();
    }
    
    private void renderHeader(RenderContext context, float mouseX, float mouseY) {
        ColorRGBA themeColor = itz.silentcore.utils.client.ClientUtility.getThemePrimaryColorRGBA();
        
        // Заголовок
        context.drawRect(x, y, width, 60, 12, themeColor);
        
        // Текст
        context.drawText("AutoBuy System", Fonts.sf_pro, x + 25, y + 18, 11f, 0.04f,
            ColorRGBA.of(255, 255, 255, 255));
        
        // Статус
        boolean enabled = autoBuyManager.isEnabled();
        String status = enabled ? "Включен" : "Выключен";
        ColorRGBA statusColor = enabled ? ColorRGBA.of(100, 255, 100, 255) : ColorRGBA.of(255, 100, 100, 255);
        context.drawText("Статус: ", Fonts.sf_pro, x + 25, y + 38, 7f, 0.04f,
            ColorRGBA.of(200, 200, 200, 255));
        context.drawText(status, Fonts.sf_pro, x + 70, y + 38, 7f, 0.04f, statusColor);
        
        // Кнопка переключения
        float btnX = x + width - 140;
        float btnY = y + 15;
        float btnW = 100;
        float btnH = 30;
        
        boolean btnHovered = isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH);
        ColorRGBA btnColor = enabled ? 
            ColorRGBA.of(100, 200, 100, btnHovered ? 255 : 200) : 
            ColorRGBA.of(200, 100, 100, btnHovered ? 255 : 200);
        
        context.drawRect(btnX, btnY, btnW, btnH, 6, btnColor);
        
        String btnText = enabled ? "Выключить" : "Включить";
        float textWidth = Fonts.sf_pro.getWidth(btnText, 7.5f);
        context.drawText(btnText, Fonts.sf_pro, btnX + (btnW - textWidth) / 2, btnY + 11, 7.5f, 0.04f,
            ColorRGBA.of(255, 255, 255, 255));
        
        // Кнопка закрытия
        float closeX = x + width - 35;
        float closeY = y + 15;
        boolean closeHovered = isHovered(mouseX, mouseY, closeX, closeY, 30, 30);
        
        context.drawRect(closeX, closeY, 30, 30, 6, 
            ColorRGBA.of(200, 50, 50, closeHovered ? 255 : 200));
        context.drawText("X", Fonts.sf_pro, closeX + 10, closeY + 9, 9f, 0.04f,
            ColorRGBA.of(255, 255, 255, 255));
    }
    
    private void renderItemsList(DrawContext drawContext, RenderContext context, float mouseX, float mouseY) {
        List<AutoBuyableItem> items = autoBuyManager.getAllItems();
        
        float listX = x + 15;
        float listY = y + 75;
        float listWidth = selectedItem != null ? width - 250 : width - 30;
        float listHeight = height - 90;
        
        // Фон списка
        context.drawRect(listX, listY, listWidth, listHeight, 8, ColorRGBA.of(30, 30, 35, 255));
        
        // Скролл
        int rows = (int) Math.ceil((double) items.size() / ITEMS_PER_ROW);
        float contentHeight = rows * (ITEM_SIZE + ITEM_SPACING) + 20;
        float maxScroll = Math.max(0, contentHeight - listHeight);
        scroll = MathHelper.clamp(scroll, -maxScroll, 0);
        
        // Рисуем предметы
        float currentX = listX + 10;
        float currentY = listY + 10 + scroll;
        
        for (int i = 0; i < items.size(); i++) {
            AutoBuyableItem item = items.get(i);
            
            if (currentY + ITEM_SIZE >= listY && currentY <= listY + listHeight) {
                renderItem(drawContext, context, item, currentX, currentY, mouseX, mouseY);
            }
            
            currentX += ITEM_SIZE + ITEM_SPACING;
            if ((i + 1) % ITEMS_PER_ROW == 0) {
                currentX = listX + 10;
                currentY += ITEM_SIZE + ITEM_SPACING;
            }
        }
    }
    
    private void renderItem(DrawContext drawContext, RenderContext context, AutoBuyableItem item, 
                           float itemX, float itemY, float mouseX, float mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, itemX, itemY, ITEM_SIZE, ITEM_SIZE);
        boolean enabled = item.isEnabled();
        boolean selected = selectedItem == item;
        
        // Фон
        ColorRGBA bgColor;
        if (selected) {
            ColorRGBA themeColor = itz.silentcore.utils.client.ClientUtility.getThemePrimaryColorRGBA();
            bgColor = ColorRGBA.of(themeColor.getR(), themeColor.getG(), themeColor.getB(), 255);
        } else if (enabled) {
            bgColor = ColorRGBA.of(70, 200, 70, hovered ? 220 : 180);
        } else {
            bgColor = ColorRGBA.of(50, 50, 55, hovered ? 180 : 140);
        }
        
        context.drawRect(itemX, itemY, ITEM_SIZE, ITEM_SIZE, 7, bgColor);
        
        // Предмет
        try {
            ItemStack stack = item.createItemStack();
            if (stack != null) {
                MatrixStack ms = drawContext.getMatrices();
                ms.push();
                float scale = (ITEM_SIZE - 10) / 16f;
                ms.translate(itemX + 5, itemY + 5, 100);
                ms.scale(scale, scale, 1);
                drawContext.drawItem(stack, 0, 0);
                ms.pop();
            }
        } catch (Exception ignored) {}
        
        // Индикатор
        if (enabled) {
            context.drawRect(itemX + ITEM_SIZE - 10, itemY + 3, 7, 7, 4, 
                ColorRGBA.of(100, 255, 100, 255));
        }
    }
    
    private void renderSettingsPanel(DrawContext drawContext, RenderContext context, float mouseX, float mouseY) {
        float panelX = x + width - 220;
        float panelY = y + 75;
        float panelW = 205;
        float panelH = height - 90;
        
        // Фон
        context.drawRect(panelX, panelY, panelW, panelH, 8, ColorRGBA.of(30, 30, 35, 255));
        
        // Заголовок
        ColorRGBA themeColor = itz.silentcore.utils.client.ClientUtility.getThemePrimaryColorRGBA();
        context.drawRect(panelX + 8, panelY + 8, panelW - 16, 35, 6, themeColor);
        context.drawText("Настройки", Fonts.sf_pro, panelX + 20, panelY + 20, 8.5f, 0.04f,
            ColorRGBA.of(255, 255, 255, 255));
        
        float settingsY = panelY + 55;
        
        // Название предмета
        String itemName = selectedItem.getDisplayName();
        if (itemName.length() > 20) {
            itemName = itemName.substring(0, 17) + "...";
        }
        context.drawText(itemName, Fonts.sf_pro, panelX + 15, settingsY, 7.5f, 0.04f,
            ColorRGBA.of(230, 230, 230, 255));
        settingsY += 30;
        
        // Кнопка вкл/выкл
        float btnX = panelX + 15;
        float btnY = settingsY;
        float btnW = panelW - 30;
        float btnH = 28;
        
        boolean btnHovered = isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH);
        boolean itemEnabled = selectedItem.isEnabled();
        
        // Зеленый цвет для включенного состояния
        ColorRGBA btnColor;
        if (itemEnabled) {
            btnColor = ColorRGBA.of(70, 220, 70, btnHovered ? 255 : 220);
        } else {
            btnColor = ColorRGBA.of(200, 70, 70, btnHovered ? 255 : 200);
        }
        
        context.drawRect(btnX, btnY, btnW, btnH, 6, btnColor);
        
        String btnText = itemEnabled ? "✓ Включен" : "✗ Выключен";
        float textWidth = Fonts.sf_pro.getWidth(btnText, 7.5f);
        context.drawText(btnText, Fonts.sf_pro, btnX + (btnW - textWidth) / 2, btnY + 10, 7.5f, 0.04f,
            ColorRGBA.of(255, 255, 255, 255));
        settingsY += 40;
        
        // Цена
        context.drawText("Макс. цена:", Fonts.sf_pro, panelX + 15, settingsY, 7f, 0.04f,
            ColorRGBA.of(180, 180, 180, 255));
        settingsY += 20;
        
        // Поле ввода
        float inputX = panelX + 15;
        float inputY = settingsY;
        float inputW = panelW - 30;
        float inputH = 28;
        
        boolean inputHovered = isHovered(mouseX, mouseY, inputX, inputY, inputW, inputH);
        ColorRGBA inputBg = isEditingPrice ? 
            ColorRGBA.of(60, 60, 70, 255) : 
            ColorRGBA.of(40, 40, 45, inputHovered ? 255 : 230);
        
        // Рамка для активного поля
        if (isEditingPrice) {
            context.drawRect(inputX - 2, inputY - 2, inputW + 4, inputH + 4, 7, 
                ColorRGBA.of(100, 200, 255, 180));
        }
        
        context.drawRect(inputX, inputY, inputW, inputH, 6, inputBg);
        
        int currentPrice = customPrices.getOrDefault(selectedItem, selectedItem.getPrice());
        String priceText = isEditingPrice ? priceInput : formatPrice(currentPrice);
        
        // Добавляем курсор при редактировании
        if (isEditingPrice) {
            priceText = priceText + (System.currentTimeMillis() % 1000 < 500 ? "|" : "");
        }
        
        context.drawText(priceText, Fonts.sf_pro, inputX + 10, inputY + 10, 7.5f, 0.04f,
            ColorRGBA.of(255, 255, 255, 255));
        
        // Подсказка
        if (!isEditingPrice) {
            context.drawText("Клик для изменения", Fonts.sf_pro, inputX + 10, inputY + inputH + 5, 6f, 0.04f,
                ColorRGBA.of(150, 150, 150, 200));
        }
        
        settingsY += 50;
        
        // Кнопки сохранения
        if (isEditingPrice) {
            float saveW = (panelW - 40) / 2;
            float saveH = 25;
            
            // Сохранить
            float saveX = panelX + 15;
            float saveY = settingsY;
            boolean saveHovered = isHovered(mouseX, mouseY, saveX, saveY, saveW, saveH);
            
            context.drawRect(saveX, saveY, saveW, saveH, 5, 
                ColorRGBA.of(70, 220, 70, saveHovered ? 255 : 200));
            float saveTextWidth = Fonts.sf_pro.getWidth("✓", 8f);
            context.drawText("✓", Fonts.sf_pro, saveX + (saveW - saveTextWidth) / 2, saveY + 7, 8f, 0.04f,
                ColorRGBA.of(255, 255, 255, 255));
            
            // Отмена
            float cancelX = saveX + saveW + 10;
            boolean cancelHovered = isHovered(mouseX, mouseY, cancelX, saveY, saveW, saveH);
            
            context.drawRect(cancelX, saveY, saveW, saveH, 5, 
                ColorRGBA.of(200, 70, 70, cancelHovered ? 255 : 200));
            float cancelTextWidth = Fonts.sf_pro.getWidth("✗", 8f);
            context.drawText("✗", Fonts.sf_pro, cancelX + (saveW - cancelTextWidth) / 2, saveY + 7, 8f, 0.04f,
                ColorRGBA.of(255, 255, 255, 255));
        }
    }
    
    @Subscribe
    public void onTick(TickEvent event) {
        if (!autoBuyManager.isEnabled()) return;
        if (!(mc.currentScreen instanceof GenericContainerScreen)) return;
        
        GenericContainerScreen screen = (GenericContainerScreen) mc.currentScreen;
        String title = screen.getTitle().getString();
        
        if (!title.contains("Аукцион") && !title.contains("Поиск")) return;
        
        List<Slot> slots = screen.getScreenHandler().slots;
        
        for (Slot slot : slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;
            
            for (AutoBuyableItem item : autoBuyManager.getAllItems()) {
                if (!item.isEnabled()) continue;
                
                String stackName = stack.getName().getString();
                if (stackName.contains(item.getDisplayName())) {
                    int price = extractPrice(stack);
                    int maxPrice = customPrices.getOrDefault(item, item.getPrice());
                    
                    if (price > 0 && price <= maxPrice) {
                        mc.interactionManager.clickSlot(screen.getScreenHandler().syncId, 
                            slot.id, 0, net.minecraft.screen.slot.SlotActionType.PICKUP, mc.player);
                        return;
                    }
                }
            }
        }
    }
    
    private int extractPrice(ItemStack stack) {
        var lore = stack.get(net.minecraft.component.DataComponentTypes.LORE);
        if (lore != null && !lore.lines().isEmpty()) {
            for (Text line : lore.lines()) {
                String text = line.getString();
                if (text.contains("$") || text.contains("Цена")) {
                    try {
                        String priceStr = text.replaceAll("[^0-9]", "");
                        if (!priceStr.isEmpty()) {
                            return Integer.parseInt(priceStr);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        return 0;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Трансформируем координаты мыши в систему координат экрана
        float scale = openAnimation.getValue();
        float centerX = mc.getWindow().getScaledWidth() / 2f;
        float centerY = mc.getWindow().getScaledHeight() / 2f;
        
        // Обратная трансформация координат мыши
        float transformedX = (float)((mouseX - centerX) / scale + width / 2f);
        float transformedY = (float)((mouseY - centerY) / scale + height / 2f);
        
        // Закрытие
        float closeX = x + width - 35;
        float closeY = y + 15;
        if (isHovered(transformedX, transformedY, closeX, closeY, 30, 30)) {
            openAnimation.animate(0);
            return true;
        }
        
        // Переключение AutoBuy
        float btnX = x + width - 140;
        float btnY = y + 15;
        if (isHovered(transformedX, transformedY, btnX, btnY, 100, 30)) {
            autoBuyManager.setEnabled(!autoBuyManager.isEnabled());
            return true;
        }
        
        // Клик по предметам
        List<AutoBuyableItem> items = autoBuyManager.getAllItems();
        float listX = x + 15;
        float listY = y + 75;
        
        float currentX = listX + 10;
        float currentY = listY + 10 + scroll;
        
        for (int i = 0; i < items.size(); i++) {
            AutoBuyableItem item = items.get(i);
            
            if (isHovered(transformedX, transformedY, currentX, currentY, ITEM_SIZE, ITEM_SIZE)) {
                if (button == 0) {
                    selectedItem = item;
                    isEditingPrice = false;
                } else if (button == 1) {
                    item.setEnabled(!item.isEnabled());
                }
                return true;
            }
            
            currentX += ITEM_SIZE + ITEM_SPACING;
            if ((i + 1) % ITEMS_PER_ROW == 0) {
                currentX = listX + 10;
                currentY += ITEM_SIZE + ITEM_SPACING;
            }
        }
        
        // Панель настроек
        if (selectedItem != null) {
            float panelX = x + width - 220;
            float panelY = y + 75;
            float panelW = 205;
            
            // Кнопка вкл/выкл предмета
            float itemBtnX = panelX + 15;
            float itemBtnY = panelY + 85;
            if (isHovered(transformedX, transformedY, itemBtnX, itemBtnY, panelW - 30, 28)) {
                selectedItem.setEnabled(!selectedItem.isEnabled());
                return true;
            }
            
            // Поле ввода цены
            float inputX = panelX + 15;
            float inputY = panelY + 145;
            if (isHovered(transformedX, transformedY, inputX, inputY, panelW - 30, 28)) {
                if (!isEditingPrice) {
                    isEditingPrice = true;
                    int currentPrice = customPrices.getOrDefault(selectedItem, selectedItem.getPrice());
                    priceInput = String.valueOf(currentPrice);
                }
                return true;
            }
            
            // Кнопки сохранения
            if (isEditingPrice) {
                float saveW = (panelW - 40) / 2;
                float saveY = panelY + 185;
                
                // Сохранить
                if (isHovered(transformedX, transformedY, inputX, saveY, saveW, 25)) {
                    try {
                        if (priceInput.isEmpty()) {
                            priceInput = "0";
                        }
                        int newPrice = Integer.parseInt(priceInput);
                        if (newPrice >= 0) {
                            customPrices.put(selectedItem, newPrice);
                            isEditingPrice = false;
                            priceInput = "";
                        }
                    } catch (Exception ignored) {
                        priceInput = String.valueOf(customPrices.getOrDefault(selectedItem, selectedItem.getPrice()));
                    }
                    return true;
                }
                
                // Отмена
                float cancelX = inputX + saveW + 10;
                if (isHovered(transformedX, transformedY, cancelX, saveY, saveW, 25)) {
                    isEditingPrice = false;
                    priceInput = "";
                    return true;
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Трансформируем координаты мыши
        float scale = openAnimation.getValue();
        float centerX = mc.getWindow().getScaledWidth() / 2f;
        float centerY = mc.getWindow().getScaledHeight() / 2f;
        
        float transformedX = (float)((mouseX - centerX) / scale + width / 2f);
        float transformedY = (float)((mouseY - centerY) / scale + height / 2f);
        
        float listX = x + 15;
        float listY = y + 75;
        float listWidth = selectedItem != null ? width - 250 : width - 30;
        float listHeight = height - 90;
        
        if (isHovered(transformedX, transformedY, listX, listY, listWidth, listHeight)) {
            scroll += verticalAmount * 30;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    
    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (isEditingPrice) {
            if (Character.isDigit(chr)) {
                if (priceInput.length() < 10) { // Ограничение на длину
                    priceInput += chr;
                }
                return true;
            }
        }
        return super.charTyped(chr, modifiers);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isEditingPrice) {
            if (keyCode == 259) { // BACKSPACE
                if (!priceInput.isEmpty()) {
                    priceInput = priceInput.substring(0, priceInput.length() - 1);
                }
                return true;
            } else if (keyCode == 257 || keyCode == 335) { // ENTER
                try {
                    if (priceInput.isEmpty()) {
                        priceInput = "0";
                    }
                    int newPrice = Integer.parseInt(priceInput);
                    if (newPrice >= 0) {
                        customPrices.put(selectedItem, newPrice);
                        isEditingPrice = false;
                        priceInput = "";
                    }
                } catch (Exception ignored) {
                    priceInput = String.valueOf(customPrices.getOrDefault(selectedItem, selectedItem.getPrice()));
                }
                return true;
            } else if (keyCode == 256) { // ESC при редактировании
                isEditingPrice = false;
                priceInput = "";
                return true;
            }
        }
        
        if (keyCode == 256) { // ESC
            openAnimation.animate(0);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
    
    @Override
    public void close() {
        if (openAnimation.getValue() < 0.01f) {
            SilentCore.getInstance().eventBus.unregister(this);
            super.close();
        }
    }
    
    @Override
    public void tick() {
        close();
        super.tick();
    }
    
    private String formatPrice(int price) {
        return String.format("%,d $", price).replace(',', ' ');
    }
    
    private boolean isHovered(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    public Map<AutoBuyableItem, Integer> getCustomPrices() {
        return customPrices;
    }
}
