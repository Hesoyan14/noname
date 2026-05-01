package itz.silentcore.feature.ui.screen.csgui.component.impl;

import itz.silentcore.feature.autobuy.items.AutoBuyableItem;
import itz.silentcore.feature.autobuy.manager.AutoBuyManager;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.theme.ThemeManager;
import itz.silentcore.feature.ui.screen.csgui.CsGui;
import itz.silentcore.feature.ui.screen.csgui.component.Component;
import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.Fonts;
import itz.silentcore.utils.render.RenderContext;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoBuyCategoryComponent extends Component implements IMinecraft {
    @Getter
    private final Category parent;
    private final Animation animation;
    private final AutoBuyManager autoBuyManager = AutoBuyManager.getInstance();
    
    private static final float CARD_SIZE = 28;
    private static final float CARD_SPACING = 6;
    private static final int CARDS_PER_ROW = 9;
    
    private float scroll = 0f;
    private float smoothScroll = 0f;
    
    private AutoBuyableItem selectedItem = null;
    private String priceInput = "";
    private boolean isEditingPrice = false;
    private final Map<AutoBuyableItem, Integer> customPrices = new HashMap<>();
    
    // Парсер настройки
    private boolean parserEnabled = false;
    private int discountPercent = 10;
    
    public AutoBuyCategoryComponent(float x, float y, Category parent) {
        super(x, y);
        this.parent = parent;
        this.animation = new Animation(250, Easing.EXPO_OUT);
    }

    @Override
    public void render(RenderContext context) {
        animation.update();
        
        boolean isActive = CsGui.category == parent;
        
        ColorRGBA themeColor = ThemeManager.getInstance().getPrimaryColorRGBA();
        
        ColorRGBA back = isActive ? 
            ColorRGBA.of(themeColor.getR(), themeColor.getG(), themeColor.getB(), (int) ((((255 * 0.15) * animation.getValue())) * CsGui.alpha.getValue())) : 
            ColorRGBA.of(233, 233, 233, (int) (255 * 0));
        context.drawRect(getX(), getY(), 78, 23, 6, back);
        
        ColorRGBA iconTextColor = isActive ?
            ColorRGBA.of(themeColor.getR(), themeColor.getG(), themeColor.getB(), (int) (255 * 0.8 * CsGui.alpha.getValue())) :
            ColorRGBA.of(233, 233, 233, (int) (255 * 0.45 * CsGui.alpha.getValue()));
        
        context.drawText(getParent().getIcon(), Fonts.icons, getX() + 1, getY() + 1f, 11, 0.04f, iconTextColor);
        context.drawText(getParent().getName(), Fonts.sf_pro, getX() + 15, getY() + 2.5f, 6.5f, 0.04f, iconTextColor);

        if (CsGui.category == parent) {
            renderAutoBuyContent(context);
        }
    }
    
    private void renderAutoBuyContent(RenderContext context) {
        List<AutoBuyableItem> items = autoBuyManager.getAllItems();
        if (items == null || items.isEmpty()) return;
        
        // Рисуем заголовок с кнопкой парсера
        renderHeader(context);
        
        // Область для отрисовки
        float listX = 87;
        float listY = 50; // Сдвинули вниз для заголовка
        float listWidth = 306.5f;
        float listHeight = selectedItem != null ? 143 : 199.5f;
        
        // Включаем scissor
        int sx = Math.round(listX);
        int sy = Math.round(listY);
        int sw = Math.round(listWidth);
        int sh = Math.round(listHeight);
        context.getContext().enableScissor(sx, sy, sx + sw, sy + sh);
        
        // Обновляем плавный скролл
        smoothScroll = MathHelper.lerp(0.15f, smoothScroll, scroll);
        
        // Рисуем карточки предметов
        float startX = listX + 5;
        float currentX = startX;
        float currentY = listY + 5 + smoothScroll;
        
        for (int i = 0; i < items.size(); i++) {
            AutoBuyableItem item = items.get(i);
            if (item == null) continue;
            
            // Рисуем только видимые карточки
            if (currentY + CARD_SIZE >= listY && currentY <= listY + listHeight) {
                renderItemCard(context, item, currentX, currentY);
            }
            
            currentX += CARD_SIZE + CARD_SPACING;
            if ((i + 1) % CARDS_PER_ROW == 0) {
                currentX = startX;
                currentY += CARD_SIZE + CARD_SPACING;
            }
        }
        
        context.getContext().disableScissor();
        
        // Рисуем панель настроек если предмет выбран
        if (selectedItem != null) {
            renderSettingsPanel(context);
        }
    }
    
    private void renderHeader(RenderContext context) {
        float headerX = 87;
        float headerY = 33;
        float headerW = 306.5f;
        float headerH = 15;
        
        ColorRGBA themeColor = ThemeManager.getInstance().getPrimaryColorRGBA();
        
        // Кнопка "Открыть /ah"
        float ahBtnX = headerX + 5;
        float ahBtnY = headerY + 2;
        float ahBtnW = 70;
        float ahBtnH = 11;
        
        context.drawRect(ahBtnX, ahBtnY, ahBtnW, ahBtnH, 3, 
            ColorRGBA.of(themeColor.getR(), themeColor.getG(), themeColor.getB(), (int) (200 * CsGui.alpha.getValue())));
        
        String ahBtnText = "Открыть /ah";
        context.drawText(ahBtnText, Fonts.sf_pro, ahBtnX + 5, ahBtnY + 3, 5.5f, 0.04f,
            ColorRGBA.of(255, 255, 255, (int) (255 * CsGui.alpha.getValue())));
        
        // Кнопка вкл/выкл AutoBuy
        float toggleBtnX = ahBtnX + ahBtnW + 5;
        float toggleBtnW = 60;
        boolean autoBuyEnabled = autoBuyManager.isEnabled();
        
        ColorRGBA toggleColor = autoBuyEnabled ? 
            ColorRGBA.of(70, 220, 70, (int) (220 * CsGui.alpha.getValue())) : 
            ColorRGBA.of(200, 70, 70, (int) (200 * CsGui.alpha.getValue()));
        
        context.drawRect(toggleBtnX, ahBtnY, toggleBtnW, ahBtnH, 3, toggleColor);
        
        String toggleText = autoBuyEnabled ? "✓ AutoBuy" : "✗ AutoBuy";
        context.drawText(toggleText, Fonts.sf_pro, toggleBtnX + 5, ahBtnY + 3, 5.5f, 0.04f,
            ColorRGBA.of(255, 255, 255, (int) (255 * CsGui.alpha.getValue())));
        
        // Текст "Парсер:"
        float parserTextX = toggleBtnX + toggleBtnW + 8;
        context.drawText("Парсер:", Fonts.sf_pro, parserTextX, headerY + 5, 6f, 0.04f,
            ColorRGBA.of(200, 200, 200, (int) (255 * CsGui.alpha.getValue())));
        
        // Чекбокс парсера
        float checkX = parserTextX + 35;
        float checkY = headerY + 3;
        float checkSize = 9;
        
        if (parserEnabled) {
            context.drawRect(checkX, checkY, checkSize, checkSize, 2, 
                ColorRGBA.of(themeColor.getR(), themeColor.getG(), themeColor.getB(), (int) (255 * CsGui.alpha.getValue())));
            context.drawText("✓", Fonts.sf_pro, checkX + 1, checkY + 1, 6f, 0.04f,
                ColorRGBA.of(255, 255, 255, (int) (255 * CsGui.alpha.getValue())));
        } else {
            context.drawRect(checkX, checkY, checkSize, checkSize, 2, 
                ColorRGBA.of(60, 60, 65, (int) (255 * CsGui.alpha.getValue())));
        }
        
        // Процент скидки
        String discountText = discountPercent + "%";
        float discountX = checkX + checkSize + 5;
        context.drawText(discountText, Fonts.sf_pro, discountX, headerY + 5, 6f, 0.04f,
            ColorRGBA.of(200, 200, 200, (int) (255 * CsGui.alpha.getValue())));
        
        // Кнопки +/-
        float btnY = headerY + 2;
        float btnSize = 10;
        float minusBtnX = headerX + headerW - 25;
        float plusBtnX = headerX + headerW - 12;
        
        context.drawRect(minusBtnX, btnY, btnSize, btnSize, 2, 
            ColorRGBA.of(60, 60, 65, (int) (255 * CsGui.alpha.getValue())));
        context.drawText("-", Fonts.sf_pro, minusBtnX + 2, btnY + 1, 7f, 0.04f,
            ColorRGBA.of(255, 255, 255, (int) (255 * CsGui.alpha.getValue())));
        
        context.drawRect(plusBtnX, btnY, btnSize, btnSize, 2, 
            ColorRGBA.of(60, 60, 65, (int) (255 * CsGui.alpha.getValue())));
        context.drawText("+", Fonts.sf_pro, plusBtnX + 2, btnY + 1, 7f, 0.04f,
            ColorRGBA.of(255, 255, 255, (int) (255 * CsGui.alpha.getValue())));
    }
    
    private void renderItemCard(RenderContext context, AutoBuyableItem item, float x, float y) {
        boolean isSelected = selectedItem == item;
        boolean hasCustomPrice = customPrices.containsKey(item) && customPrices.get(item) > 0;
        
        ColorRGBA themeColor = ThemeManager.getInstance().getPrimaryColorRGBA();
        
        // Фон карточки
        ColorRGBA cardBg;
        if (isSelected) {
            cardBg = ColorRGBA.of(themeColor.getR(), themeColor.getG(), themeColor.getB(), (int) (255 * 0.3 * CsGui.alpha.getValue()));
        } else if (hasCustomPrice) {
            cardBg = ColorRGBA.of(themeColor.getR(), themeColor.getG(), themeColor.getB(), (int) (255 * 0.15 * CsGui.alpha.getValue()));
        } else {
            cardBg = ColorRGBA.of(40, 40, 45, (int) (255 * CsGui.alpha.getValue()));
        }
        
        context.drawRect(x, y, CARD_SIZE, CARD_SIZE, 6, cardBg);
        
        // Рисуем предмет
        try {
            ItemStack stack = item.createItemStack();
            if (stack != null) {
                DrawContext drawContext = context.getContext();
                float itemX = x + (CARD_SIZE - 16) / 2f;
                float itemY = y + (CARD_SIZE - 16) / 2f;
                drawContext.drawItem(stack, (int)itemX, (int)itemY);
            }
        } catch (Exception ignored) {}
        
        // Индикатор включенного состояния
        if (item.isEnabled()) {
            context.drawRect(x + CARD_SIZE - 8, y + 2, 6, 6, 3, 
                ColorRGBA.of(100, 255, 100, (int) (255 * CsGui.alpha.getValue())));
        }
    }
    
    private void renderSettingsPanel(RenderContext context) {
        float panelX = 87;
        float panelY = 33 + 182; // Сдвинули вниз
        float panelW = 306.5f;
        float panelH = 51.5f;
        
        ColorRGBA themeColor = ThemeManager.getInstance().getPrimaryColorRGBA();
        
        // Фон панели
        context.drawRect(panelX, panelY, panelW, panelH, 6, 
            ColorRGBA.of(30, 30, 35, (int) (255 * CsGui.alpha.getValue())));
        
        // Название предмета
        String itemName = selectedItem.getDisplayName();
        if (itemName != null && itemName.length() > 30) {
            itemName = itemName.substring(0, 27) + "...";
        }
        context.drawText(itemName != null ? itemName : "Unknown", Fonts.sf_pro, 
            panelX + 8, panelY + 8, 7f, 0.04f, 
            ColorRGBA.of(230, 230, 230, (int) (255 * CsGui.alpha.getValue())));
        
        // Кнопка вкл/выкл
        float btnX = panelX + 8;
        float btnY = panelY + 22;
        float btnW = 70;
        float btnH = 22;
        
        boolean enabled = selectedItem.isEnabled();
        ColorRGBA btnColor = enabled ? 
            ColorRGBA.of(70, 220, 70, (int) (220 * CsGui.alpha.getValue())) : 
            ColorRGBA.of(200, 70, 70, (int) (200 * CsGui.alpha.getValue()));
        
        context.drawRect(btnX, btnY, btnW, btnH, 5, btnColor);
        
        String btnText = enabled ? "✓ Вкл" : "✗ Выкл";
        float textWidth = Fonts.sf_pro.getWidth(btnText, 6.5f);
        context.drawText(btnText, Fonts.sf_pro, btnX + (btnW - textWidth) / 2f, btnY + 8, 6.5f, 0.04f,
            ColorRGBA.of(255, 255, 255, (int) (255 * CsGui.alpha.getValue())));
        
        // Поле ввода цены
        float inputX = btnX + btnW + 8;
        float inputY = btnY;
        float inputW = panelW - btnW - 24;
        float inputH = btnH;
        
        ColorRGBA inputBg = isEditingPrice ? 
            ColorRGBA.of(60, 60, 70, (int) (255 * CsGui.alpha.getValue())) : 
            ColorRGBA.of(40, 40, 45, (int) (230 * CsGui.alpha.getValue()));
        
        context.drawRect(inputX, inputY, inputW, inputH, 5, inputBg);
        
        // Текст цены
        int currentPrice = customPrices.getOrDefault(selectedItem, selectedItem.getPrice());
        String priceText = isEditingPrice ? 
            (priceInput.isEmpty() ? "" : priceInput) : 
            (currentPrice > 0 ? formatPrice(currentPrice) : "Цена...");
        
        ColorRGBA textColor = isEditingPrice || currentPrice > 0 ? 
            ColorRGBA.of(255, 255, 255, (int) (255 * CsGui.alpha.getValue())) : 
            ColorRGBA.of(150, 150, 150, (int) (200 * CsGui.alpha.getValue()));
        
        context.drawText(priceText, Fonts.sf_pro, inputX + 8, inputY + 8, 6.5f, 0.04f, textColor);
        
        // Курсор при редактировании
        if (isEditingPrice && System.currentTimeMillis() % 1000 < 500) {
            float cursorX = inputX + 8 + Fonts.sf_pro.getWidth(priceText, 6.5f);
            context.drawRect(cursorX, inputY + 6, 1, inputH - 12, 0, 
                ColorRGBA.of(255, 255, 255, (int) (255 * CsGui.alpha.getValue())));
        }
    }
    
    private String formatPrice(int price) {
        return String.format("%,d $", price).replace(',', ' ');
    }
    
    private float calculateContentHeight() {
        List<AutoBuyableItem> items = autoBuyManager.getAllItems();
        if (items == null || items.isEmpty()) return 0;
        
        int rows = (int) Math.ceil((double) items.size() / CARDS_PER_ROW);
        return rows * (CARD_SIZE + CARD_SPACING) + 10;
    }

    @Override
    public void click(double mouseX, double mouseY, int button) {
        // Клик по категории
        if (mouseX >= getX() && mouseX <= getX() + 78 && mouseY >= getY() && mouseY <= getY() + 23) {
            if (button == 0) CsGui.category = parent;
            return;
        }
        
        if (CsGui.category != parent) return;
        
        // Клик по кнопке "Открыть /ah"
        float headerX = 87;
        float headerY = 33;
        float ahBtnX = headerX + 5;
        float ahBtnY = headerY + 2;
        float ahBtnW = 70;
        float ahBtnH = 11;
        
        if (mouseX >= ahBtnX && mouseX <= ahBtnX + ahBtnW && 
            mouseY >= ahBtnY && mouseY <= ahBtnY + ahBtnH && button == 0) {
            // Отправляем команду /ah
            if (mc.player != null && mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendChatCommand("ah");
            }
            return;
        }
        
        // Клик по кнопке вкл/выкл AutoBuy
        float toggleBtnX = ahBtnX + ahBtnW + 5;
        float toggleBtnW = 60;
        
        if (mouseX >= toggleBtnX && mouseX <= toggleBtnX + toggleBtnW && 
            mouseY >= ahBtnY && mouseY <= ahBtnY + ahBtnH && button == 0) {
            autoBuyManager.setEnabled(!autoBuyManager.isEnabled());
            return;
        }
        
        // Клик по чекбоксу парсера
        float parserTextX = toggleBtnX + toggleBtnW + 8;
        float checkX = parserTextX + 35;
        float checkY = headerY + 3;
        float checkSize = 9;
        
        if (mouseX >= checkX && mouseX <= checkX + checkSize && 
            mouseY >= checkY && mouseY <= checkY + checkSize && button == 0) {
            parserEnabled = !parserEnabled;
            itz.silentcore.feature.autobuy.parser.PriceParser.setEnabled(parserEnabled);
            return;
        }
        
        // Клик по кнопкам +/-
        float btnY = headerY + 2;
        float btnSize = 10;
        float headerW = 306.5f;
        float minusBtnX = headerX + headerW - 25;
        float plusBtnX = headerX + headerW - 12;
        
        if (button == 0) {
            if (mouseX >= minusBtnX && mouseX <= minusBtnX + btnSize && 
                mouseY >= btnY && mouseY <= btnY + btnSize) {
                discountPercent = Math.max(0, discountPercent - 5);
                itz.silentcore.feature.autobuy.parser.PriceParser.setDiscountPercent(discountPercent);
                return;
            }
            
            if (mouseX >= plusBtnX && mouseX <= plusBtnX + btnSize && 
                mouseY >= btnY && mouseY <= btnY + btnSize) {
                discountPercent = Math.min(100, discountPercent + 5);
                itz.silentcore.feature.autobuy.parser.PriceParser.setDiscountPercent(discountPercent);
                return;
            }
        }
        
        List<AutoBuyableItem> items = autoBuyManager.getAllItems();
        if (items == null) return;
        
        // Клик по карточкам
        float listX = 87;
        float listY = 50; // Обновили координату
        float startX = listX + 5;
        float currentX = startX;
        float currentY = listY + 5 + smoothScroll;
        
        for (int i = 0; i < items.size(); i++) {
            AutoBuyableItem item = items.get(i);
            if (item == null) continue;
            
            if (mouseX >= currentX && mouseX <= currentX + CARD_SIZE && 
                mouseY >= currentY && mouseY <= currentY + CARD_SIZE) {
                if (button == 0) {
                    selectedItem = item;
                    isEditingPrice = false;
                } else if (button == 1) {
                    item.setEnabled(!item.isEnabled());
                }
                return;
            }
            
            currentX += CARD_SIZE + CARD_SPACING;
            if ((i + 1) % CARDS_PER_ROW == 0) {
                currentX = startX;
                currentY += CARD_SIZE + CARD_SPACING;
            }
        }
        
        // Клик по панели настроек
        if (selectedItem != null) {
            float panelX = 87;
            float panelY = 33 + 182; // Обновили координату
            float panelBtnX = panelX + 8;
            float panelBtnY = panelY + 22;
            float btnW = 70;
            float btnH = 22;
            
            // Кнопка вкл/выкл
            if (mouseX >= panelBtnX && mouseX <= panelBtnX + btnW && 
                mouseY >= panelBtnY && mouseY <= panelBtnY + btnH && button == 0) {
                selectedItem.setEnabled(!selectedItem.isEnabled());
                return;
            }
            
            // Поле ввода
            float inputX = panelBtnX + btnW + 8;
            float inputW = 306.5f - btnW - 24;
            if (mouseX >= inputX && mouseX <= inputX + inputW && 
                mouseY >= panelBtnY && mouseY <= panelBtnY + btnH && button == 0) {
                isEditingPrice = true;
                int currentPrice = customPrices.getOrDefault(selectedItem, selectedItem.getPrice());
                priceInput = currentPrice > 0 ? String.valueOf(currentPrice) : "";
            }
        }
    }

    @Override
    public void key(int button) {
        if (CsGui.category != parent || !isEditingPrice) return;
        
        if (button == 259) { // BACKSPACE
            if (!priceInput.isEmpty()) {
                priceInput = priceInput.substring(0, priceInput.length() - 1);
            }
        } else if (button == 257 || button == 335) { // ENTER
            savePrice();
        } else if (button == 256) { // ESC
            isEditingPrice = false;
            priceInput = "";
        }
    }

    @Override
    public void type(char chr) {
        if (CsGui.category != parent || !isEditingPrice) return;
        
        if (Character.isDigit(chr) && priceInput.length() < 10) {
            priceInput += chr;
        }
    }
    
    private void savePrice() {
        if (selectedItem == null) return;
        
        try {
            if (priceInput.isEmpty()) {
                priceInput = "0";
            }
            int newPrice = Integer.parseInt(priceInput);
            if (newPrice >= 0) {
                customPrices.put(selectedItem, newPrice);
                selectedItem.getSettings().setBuyBelow(newPrice);
                isEditingPrice = false;
                priceInput = "";
            }
        } catch (Exception ignored) {
            priceInput = String.valueOf(customPrices.getOrDefault(selectedItem, selectedItem.getPrice()));
        }
    }

    @Override
    public void scroll(double mouseX, double mouseY, double amount) {
        if (CsGui.category != parent) return;
        
        float listX = 87;
        float listY = 50; // Обновили координату
        float listWidth = 306.5f;
        float listHeight = selectedItem != null ? 143 : 199.5f;
        
        if (mouseX >= listX && mouseX <= listX + listWidth && 
            mouseY >= listY && mouseY <= listY + listHeight) {
            
            float contentHeight = calculateContentHeight();
            float maxScroll = Math.max(0, contentHeight - listHeight);
            
            scroll += amount * 15;
            scroll = MathHelper.clamp(scroll, -maxScroll, 0);
        }
    }

    @Override
    public void dragged(double mouseX, double mouseY, double dX, double dY, int button) {}

    @Override
    public void moved(double mouseX, double mouseY) {}

    @Override
    public void mReleased(double mouseX, double mouseY, int button) {}
    
    public Map<AutoBuyableItem, Integer> getCustomPrices() {
        return customPrices;
    }
}
