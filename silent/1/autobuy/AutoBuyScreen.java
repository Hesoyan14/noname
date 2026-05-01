package code.essence.display.screens.autobuy;

import code.essence.common.animation.Easy.Direction;
import code.essence.common.animation.Easy.EaseBackIn;
import code.essence.display.screens.autobuy.history.HistoryRenderer;
import code.essence.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import code.essence.display.screens.clickgui.components.implement.autobuy.manager.AutoBuyManager;
import code.essence.display.screens.clickgui.components.implement.autobuy.originalitems.ItemRegistry;
import code.essence.features.impl.misc.AutoBuy;
import code.essence.features.impl.misc.autobuy.CommandSender;
import code.essence.features.impl.render.Hud;
import code.essence.features.module.setting.implement.BooleanSetting;
import code.essence.features.module.setting.implement.SliderSettings;
import code.essence.utils.display.color.ColorAssist;
import code.essence.utils.display.render.font.FontRenderer;
import code.essence.utils.display.render.font.Fonts;
import code.essence.utils.display.render.geometry.Render2D;
import code.essence.utils.display.interfaces.QuickImports;
import code.essence.utils.display.render.shape.ShapeProperties;
import code.essence.utils.display.render.post.KawaseBlur;
import code.essence.utils.display.render.shape.implement.Rectangle;
import code.essence.utils.display.scissor.ScissorAssist;
import code.essence.utils.math.calc.Calculate;
import code.essence.Essence;
import code.essence.utils.theme.ThemeManager;
import code.essence.utils.animation.AnimationHelper;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.*;
import java.util.List;

import static code.essence.utils.display.render.font.Fonts.Type.ESSENCE;
import static code.essence.utils.display.render.font.Fonts.Type.SuisseIntlSemiBold;

@Setter
@Getter
public class AutoBuyScreen extends Screen implements QuickImports {
    public static AutoBuyScreen INSTANCE = new AutoBuyScreen();
    public final EaseBackIn animation = new EaseBackIn(400 / 2, 1f, 1.5f);
    private final Rectangle rectangle = new Rectangle();
    private final AutoBuyManager autoBuyManager = AutoBuyManager.getInstance();
    
    private float x, y, width = 284.5f, height = 290;
    private float scroll = 0f;
    private float smoothedScroll = 0f;
    
    
    private AutoBuyableItem settingsItem = null; 
    private AutoBuyableItem previousSettingsItem = null; 
    private AutoBuyableItem settingsAnimatedItem = null; 
    private final EaseBackIn settingsPanelAnimation = new EaseBackIn(400 / 2, 1f, 1.1f); 
    private final AutoBuySettingsPanelRender settingsPanelRenderer;

    private static final float SCROLLBAR_WIDTH = 2f; 
    private static final int SCROLLBAR_BG_COLOR = new Color(27, 27, 30, 255).getRGB(); 
    
    private AutoBuyableItem selectedItem = null;
    private String priceInput = "";
    private boolean isEditingPrice = false;
    private boolean isTextSelected = false; 
    private int cursorPosition = 0; 
    private final java.util.HashMap<AutoBuyableItem, Integer> customPrices = new java.util.HashMap<>();
    
    private final Map<String, Set<String>> parserEnabledItemsByServer = new HashMap<>();
    private final Map<String, Map<String, Float>> durabilitySettingsByServer = new HashMap<>();
    private final java.util.HashMap<AutoBuyableItem, code.essence.features.module.setting.implement.BooleanSetting> parserSettings = new java.util.HashMap<>();
    private final java.util.HashMap<AutoBuyableItem, code.essence.features.module.setting.implement.SliderSettings> durabilitySettings = new java.util.HashMap<>();
    private final java.util.HashMap<AutoBuyableItem, code.essence.display.screens.clickgui.components.implement.settings.CheckboxComponent> parserComponents = new java.util.HashMap<>();
    private final java.util.HashMap<AutoBuyableItem, code.essence.display.screens.clickgui.components.implement.settings.SliderComponent> durabilityComponents = new java.util.HashMap<>();
    private final java.util.HashMap<AutoBuyableItem, Boolean> sliderDragging = new java.util.HashMap<>(); 
    private final java.util.HashMap<AutoBuyableItem, Float> sliderScrollPosition = new java.util.HashMap<>(); 
    private long buttonClickTime = 0;
    
    public AutoBuyScreen() {
        super(Text.of("AutoBuyScreen"));
        
        this.settingsPanelRenderer = new AutoBuySettingsPanelRender(
                settingsPanelAnimation,
                this::getOrCreateParserSetting,
                this::getOrCreateDurabilitySetting,
                sliderScrollPosition
        );

        
        syncParserSettingsWithServerData();
    }
    
    public void openGui() {
        
        cleanupDuplicatePrices();
        animation.setDirection(Direction.FORWARDS);
        mc.setScreen(this);
    }
    
    
    private void cleanupDuplicatePrices() {
        Map<String, AutoBuyableItem> latestItems = new HashMap<>();
        Map<String, Integer> latestPrices = new HashMap<>();
        
        
        for (Map.Entry<AutoBuyableItem, Integer> entry : customPrices.entrySet()) {
            AutoBuyableItem item = entry.getKey();
            if (item != null && item.getDisplayName() != null) {
                String name = item.getDisplayName();
                latestItems.put(name, item);
                latestPrices.put(name, entry.getValue());
            }
        }
        
        
        customPrices.clear();
        for (Map.Entry<String, AutoBuyableItem> entry : latestItems.entrySet()) {
            String name = entry.getKey();
            AutoBuyableItem item = entry.getValue();
            Integer price = latestPrices.get(name);
            if (price != null && price > 0) {
                customPrices.put(item, price);
            }
        }
    }
    
    public float getScaleAnimation() {
        return (float) animation.getOutput();
    }
    
    public java.util.Map<AutoBuyableItem, Integer> getCustomPrices() {
        return customPrices;
    }

    
    public List<AutoBuyableItem> getItemsForCurrentProfile() {
        AutoBuy autoBuy = AutoBuy.getInstance();
        if (autoBuy == null) return ItemRegistry.getFunTimeItems();
        
        AutoBuy.ServerMode mode = autoBuy.getServerMode();
        switch (mode) {
            case FUNTIME:
                return ItemRegistry.getFunTimeItems(); 
            case SPOOKYTIME:
                return ItemRegistry.getSpookyTime();
            case HOLYWORLD:
                return ItemRegistry.getHolyWorld();
            default:
                return ItemRegistry.getFunTimeItems();
        }
    }
    
    
    private AutoBuy.ServerMode getCurrentServerMode() {
        AutoBuy autoBuy = AutoBuy.getInstance();
        return autoBuy != null ? autoBuy.getServerMode() : AutoBuy.ServerMode.FUNTIME;
    }

    public boolean isParserEnabled(AutoBuyableItem item) {
        if (item == null) {
            return false;
        }
        
        code.essence.features.module.setting.implement.BooleanSetting parserSetting = parserSettings.get(item);
        if (parserSetting != null) {
            return parserSetting.isValue();
        }
        
        String name = item.getDisplayName();
        if (name == null) {
            return false;
        }
        String key = name.toLowerCase();
        String serverKey = getCurrentServerMode().name();
        Set<String> parserEnabledItems = parserEnabledItemsByServer.getOrDefault(serverKey, new HashSet<>());
        return parserEnabledItems.contains(key);
    }

    public void toggleParserEnabled(AutoBuyableItem item) {
        if (item == null) {
            return;
        }
        String name = item.getDisplayName();
        if (name == null) {
            return;
        }
        String key = name.toLowerCase();
        String serverKey = getCurrentServerMode().name();
        Set<String> parserEnabledItems = parserEnabledItemsByServer.computeIfAbsent(serverKey, k -> new HashSet<>());
        boolean wasEnabled = parserEnabledItems.contains(key);
        if (wasEnabled) {
            parserEnabledItems.remove(key);
        } else {
            parserEnabledItems.add(key);
        }
        
        
        BooleanSetting parserSetting = parserSettings.get(item);
        if (parserSetting != null) {
            parserSetting.setValue(!wasEnabled);
        }
        
        
        try {
            Essence.getInstance().getFileController().saveFile(code.essence.utils.client.managers.file.impl.AutoBuyConfigFile.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getParserEnabledItemNames() {
        
        String serverKey = getCurrentServerMode().name();
        Set<String> parserEnabledItems = parserEnabledItemsByServer.getOrDefault(serverKey, new HashSet<>());
        
        
        Set<String> uniqueNames = new LinkedHashSet<>();
        List<String> out = new ArrayList<>();
        
        
        
        List<AutoBuyableItem> currentProfileItems = getItemsForCurrentProfile();
        
        
        
        for (AutoBuyableItem item : currentProfileItems) {
            if (item == null || item.getDisplayName() == null) continue;
            String nameLower = item.getDisplayName().toLowerCase();
            
            
            if (parserEnabledItems.contains(nameLower)) {
                
                if (uniqueNames.add(nameLower)) {
                    out.add(item.getDisplayName()); 
                }
            }
        }
        
        return out;
    }

    public void setParserEnabledItemNames(Collection<String> names) {
        String serverKey = getCurrentServerMode().name();
        Set<String> parserEnabledItems = parserEnabledItemsByServer.computeIfAbsent(serverKey, k -> new HashSet<>());
        parserEnabledItems.clear();
        if (names == null) return;
        for (String name : names) {
            if (name == null) continue;
            String key = name.trim().toLowerCase();
            if (!key.isEmpty()) {
                parserEnabledItems.add(key);
            }
        }
    }
    
    public Map<String, Set<String>> getParserEnabledItemsByServer() {
        return parserEnabledItemsByServer;
    }

    
    private String normalizeDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return displayName;
        }

        
        return displayName
                .replace("ᴀ", "a").replace("ʙ", "b").replace("ᴄ", "c").replace("ᴅ", "d").replace("ᴇ", "e")
                .replace("ꜰ", "f").replace("ɢ", "g").replace("ʜ", "h").replace("ɪ", "i").replace("ᴊ", "j")
                .replace("ᴋ", "k").replace("ʟ", "l").replace("ᴍ", "m").replace("ɴ", "n").replace("ᴏ", "o")
                .replace("ᴘ", "p").replace("ʀ", "r").replace("ꜱ", "s").replace("ᴛ", "t").replace("ᴜ", "u")
                .replace("ᴠ", "v").replace("ᴡ", "w").replace("ʏ", "y").replace("ᴢ", "z")
                
                .replace("ᴀʀᴍᴏʀᴛᴀʟɪᴛʏ", "armortality")
                .replace("ɪɴꜰɪɴɪᴛʏ", "infinity")
                .replace("ᴇɴᴅʟᴇꜱꜱ", "endless")
                .replace("ꜰᴏʀᴛᴜɴᴇ", "fortune")
                .replace("ᴇꜰꜰɪᴄɪᴇɴᴄʏ", "efficiency");
    }

    public void setParserEnabledItemsByServer(Map<String, Set<String>> itemsByServer) {
        
        
        if (itemsByServer != null) {
            for (Map.Entry<String, Set<String>> entry : itemsByServer.entrySet()) {
                parserEnabledItemsByServer.put(entry.getKey(), entry.getValue());
            }
        }
        
        syncParserSettingsWithServerData();
    }
    
    
    public void replaceParserEnabledItemsByServer(Map<String, Set<String>> itemsByServer) {
        parserEnabledItemsByServer.clear();
        if (itemsByServer != null) {
            parserEnabledItemsByServer.putAll(itemsByServer);
        }
        syncParserSettingsWithServerData();
    }

    
    public void syncParserSettingsWithServerData() {
        String serverKey = getCurrentServerMode().name();
        Set<String> parserEnabledItems = parserEnabledItemsByServer.getOrDefault(serverKey, new HashSet<>());

        
        for (var entry : parserSettings.entrySet()) {
            AutoBuyableItem item = entry.getKey();
            BooleanSetting setting = entry.getValue();

            if (item != null && item.getDisplayName() != null) {
                String key = item.getDisplayName().toLowerCase();
                boolean shouldBeEnabled = parserEnabledItems.contains(key);
                setting.setValue(shouldBeEnabled);
            }
        }
    }

    
    public Map<String, Map<String, Float>> getDurabilitySettingsByServer() {
        return new HashMap<>(durabilitySettingsByServer);
    }

    
    public void setDurabilitySettingsByServer(Map<String, Map<String, Float>> settingsByServer) {
        durabilitySettingsByServer.clear();
        if (settingsByServer != null) {
            durabilitySettingsByServer.putAll(settingsByServer);
        }
    }

    
    public void setDurabilitySetting(String serverKey, String itemName, float durabilityValue) {
        Map<String, Float> serverSettings = durabilitySettingsByServer.computeIfAbsent(serverKey, k -> new HashMap<>());
        serverSettings.put(itemName.toLowerCase(), durabilityValue);
    }

    
    public void setCustomPrice(AutoBuyableItem item, int price, boolean broadcast) {
        if (item == null) return;
        
        
        String itemName = item.getDisplayName();
        if (itemName != null) {
            customPrices.entrySet().removeIf(entry -> {
                AutoBuyableItem key = entry.getKey();
                return key != null && key.getDisplayName() != null && key.getDisplayName().equals(itemName);
            });
        }
        
        if (price > 0) {
            customPrices.put(item, price);
            item.getSettings().setBuyBelow(price);
        } else {
            
            customPrices.remove(item);
            item.getSettings().setBuyBelow(0);
        }

        if (!broadcast) return;

        AutoBuy autoBuy = AutoBuy.getInstance();
        if (autoBuy != null && autoBuy.isState() && autoBuy.isBuyerMode()) {
            try {
                autoBuy.getNetworkManager().sendToAllClients("price_sync:" + item.getDisplayName() + "|" + price);
            } catch (Exception ignored) {}
        }
    }

    
    public void applySyncedPrice(String itemName, int price) {
        if (itemName == null || itemName.isEmpty()) return;

        AutoBuyableItem found = null;
        for (AutoBuyableItem it : ItemRegistry.getAllItems()) {
            if (it == null) continue;
            String name = it.getDisplayName();
            if (name != null && name.equalsIgnoreCase(itemName)) {
                found = it;
                break;
            }
        }

        if (found != null) {
            
            customPrices.entrySet().removeIf(entry -> {
                AutoBuyableItem key = entry.getKey();
                return key != null && key.getDisplayName() != null && key.getDisplayName().equalsIgnoreCase(itemName);
            });
            setCustomPrice(found, price, false);
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        try {
            if (context == null || window == null) return;
            
            MatrixStack matrix = context.getMatrices();
            if (matrix == null) return;
            
            float scaleAnimation = getScaleAnimation();
            
            
            
            if (scaleAnimation < 0.01f) {
                rectangle.render(ShapeProperties.create(matrix, 0, 0, window.getScaledWidth(), window.getScaledHeight())
                        .color(Calculate.applyOpacity(0xFF000000, 100 * scaleAnimation))
                        .build());
                return;
            }
            
            rectangle.render(ShapeProperties.create(matrix, 0, 0, window.getScaledWidth(), window.getScaledHeight())
                    .color(Calculate.applyOpacity(0xFF000000, 100 * scaleAnimation))
                    .build());
            
            Calculate.scale(matrix, window.getScaledWidth() / 2f, window.getScaledHeight() / 2f, scaleAnimation, () -> {
                x = (window.getScaledWidth() - width) / 2f;
                y = (window.getScaledHeight() - height) / 2f;
                
                renderMainPanel(context, matrix, mouseX, mouseY, delta);
                
                
                if (settingsPanelRenderer.render(context, matrix, mouseX, mouseY, delta, settingsItem, settingsAnimatedItem, x, y, width, height)) {
                    
                    settingsAnimatedItem = null;
                }
            });
            
            
            HistoryRenderer.getInstance().render(context, mouseX, mouseY, delta);
        } catch (Exception e) {
            e.printStackTrace();
        }
   }
    
    private void renderMainPanel(DrawContext context, MatrixStack matrix, int mouseX, int mouseY, float delta) {

        if (Hud.blur.isValue()) {
            blur(context, matrix, mouseX, mouseY);
        }

        rectangle.render(ShapeProperties.create(matrix, x, y, width, height)
                .round(6)
                .color(ThemeManager.BackgroundGui.getColor())
                .build());
        
        
        renderItemsList(context, matrix, mouseX, mouseY, delta);

        renderPriceInput(context, matrix, mouseX, mouseY);

        
        renderCategory(context, matrix, mouseX, mouseY);
        
        
        renderScrollbar(context, matrix);
    }

    private void blur(DrawContext context, MatrixStack matrix, int mouseX, int mouseY) {
        Render2D.rectangleWithMask(matrix.peek().getPositionMatrix(), x, y, width, height, 6f,0, KawaseBlur.INSTANCE.fbos.getFirst().getColorAttachment());

    }

    private void renderCategory(DrawContext context, MatrixStack matrix, int mouseX, int mouseY) {
        float categoryWidth = width - 10;
        float categoryX = x + 5;
        float categoryY = y + 4;

        
        matrix.push();
        matrix.translate(0, 0, 200);

        rectangle.render(ShapeProperties.create(matrix, categoryX, categoryY, categoryWidth, 22)
                .round(4f)
                .softness(4)
                .color(ColorAssist.getClientColor(1f), ColorAssist.getClientColor(1f),
                        ColorAssist.getClientColor2(1f), ColorAssist.getClientColor2(1f))
                .build());

        rectangle.render(ShapeProperties.create(matrix, categoryX - 4, categoryY - 4, categoryWidth + 8, 30)
                .round(8f)
                .softness(4)
                .color(new Color(255, 255, 255, 5).getRGB())
                .build());

        rectangle.render(ShapeProperties.create(matrix, categoryX - 3, categoryY - 3, categoryWidth + 6, 28)
                .round(7f)
                .softness(3)
                .color(ColorAssist.getClientColor(0.1f), ColorAssist.getClientColor(0.1f),
                        ColorAssist.getClientColor2(0.1f), ColorAssist.getClientColor2(0.1f))
                .build());

        rectangle.render(ShapeProperties.create(matrix, categoryX - 2, categoryY - 2, categoryWidth + 4, 26)
                .round(6f)
                .softness(2)
                .color(ColorAssist.getClientColor(0.1f), ColorAssist.getClientColor(0.1f),
                        ColorAssist.getClientColor2(0.1f), ColorAssist.getClientColor2(0.1f))
                .build());

        Fonts.getSize(18, ESSENCE).drawString(context.getMatrices(), "A", x+10, y+13, ThemeManager.textColor.getColor());

        
        AutoBuy.ServerMode currentMode = getCurrentServerMode();
        String modeName = currentMode != null ? currentMode.getDisplayName() : "Unknown";
        if (modeName == null) modeName = "Unknown";
        String serverModeText = "AutoBuy [" + modeName + "]";
        Fonts.getSize(18, SuisseIntlSemiBold).drawString(context.getMatrices(), serverModeText, x + 23, y + 12.5, ThemeManager.textColor.getColor());
        
        matrix.pop();
    }



        private void renderItemsList(DrawContext context, MatrixStack matrix, int mouseX, int mouseY, float delta) {
        float listX = x;
        float listY = y + 31;
        float listWidth = width;
        
        
        float listHeight;
        if (selectedItem != null) {
            float inputHeight = 20;
            float inputY = y + height - 8 - inputHeight;
            float fontSize = 14;
            float nameY = inputY - 8 - fontSize + 2; 
            float itemIconY = nameY + 4.5f; 
            
            listHeight = itemIconY - 8 - listY;
        } else {
            listHeight = height - 85;
        }

        float cardSize = 21;
        float cardSpacing = 6;
        
        float contentHeight = calculateContentHeight();
        
        float maxScrollAmount = Math.max(0f, contentHeight - cardSpacing - listHeight);
        scroll = MathHelper.clamp(scroll, -maxScrollAmount, 0f);
        smoothedScroll = Calculate.interpolate(smoothedScroll, scroll, 0.15f);

        List<AutoBuyableItem> allItems = getItemsForCurrentProfile();
        if (allItems == null) return;
        int CardsCountLine = 10;

        
        float startX = listX + 8;
        float currentX = startX;
        float currentY = listY + smoothedScroll;

        
        ScissorAssist scissor = Essence.getInstance().getScissorManager();
        
        
        
        
        scissor.push(matrix.peek().getPositionMatrix(), listX, listY - 4, listWidth, listHeight + 3);

        for (int i = 0; i < allItems.size(); i++) {
            AutoBuyableItem item = allItems.get(i);
            if (item == null) {
                continue;
            }

            
            if (currentY + cardSize >= listY && currentY <= listY + listHeight) {
                renderItemCard(context, matrix, item, i, currentX, currentY, cardSize, mouseX, mouseY);
            }

            currentX += cardSize + cardSpacing;
            if ((i + 1) % CardsCountLine == 0) {
                currentX = startX;
                currentY += cardSize + cardSpacing;
            }
        }

        scissor.pop();
    }

    private void renderItemCard(DrawContext context, MatrixStack matrix, AutoBuyableItem item, int index,
                                float x, float y, float size, int mouseX, int mouseY) {
        if (item == null) return;
        
        boolean hovered = Calculate.isHovered(mouseX, mouseY, x, y, size, size);
        boolean isSelected = selectedItem == item;

    

        
        
        boolean hasCustomPriceByObject = customPrices.containsKey(item);
        int priceByObject = hasCustomPriceByObject ? customPrices.get(item) : 0;
        
        
        String itemName = item.getDisplayName();
        boolean hasCustomPriceByName = false;
        int priceByName = 0;
        if (itemName != null) {
            for (Map.Entry<AutoBuyableItem, Integer> entry : customPrices.entrySet()) {
                if (entry.getKey() != null && itemName.equals(entry.getKey().getDisplayName())) {
                    hasCustomPriceByName = true;
                    priceByName = entry.getValue();
                    break;
                }
            }
        }
        
        boolean hasCustomPrice = (hasCustomPriceByObject && priceByObject > 0) || (hasCustomPriceByName && priceByName > 0);
        int currentPrice = hasCustomPriceByObject ? priceByObject : (hasCustomPriceByName ? priceByName : item.getPrice());
        boolean hasPrice = currentPrice > 0;
        boolean parserEnabled = isParserEnabled(item);

        
        
        
        int outlineColor = -1;
        float outlineThickness = 0f;
        if (parserEnabled) {
            
            outlineColor = new Color(87, 87, 90, 255).getRGB();
            outlineThickness = 2f;
        }
        
        
        
        if (outlineColor != -1) {
            float outlineOffset = outlineThickness + 0.01f;
            rectangle.render(ShapeProperties.create(matrix, x - outlineOffset, y - outlineOffset, size + outlineOffset * 2, size + outlineOffset * 2)
                    .round(6f)
                    .softness(2)
                    .thickness(outlineThickness)
                    .outlineColor(outlineColor)
                    .color(new Color(0, 0, 0, 0).getRGB()) 
                    .build());
        }
        
        
        ShapeProperties.ShapePropertiesBuilder builder = ShapeProperties.create(matrix, x, y, size, size)
                .round(4f)
                .softness(4);
        
        
        if (hasCustomPrice && hasPrice) {
            builder.color(ColorAssist.getClientColor(1f), ColorAssist.getClientColor(1f),
                         ColorAssist.getClientColor2(1f), ColorAssist.getClientColor2(1f));
        } else {
            
            int bgColor = ThemeManager.BackgroundSettings.getColor();
            if (hovered || isSelected) {
                Color color = new Color(bgColor, true);
                int r = Math.min(255, (int)(color.getRed() * 1.2));
                int g = Math.min(255, (int)(color.getGreen() * 1.2));
                int b = Math.min(255, (int)(color.getBlue() * 1.2));
                bgColor = new Color(r, g, b, color.getAlpha()).getRGB();
            }
            builder.color(bgColor);
        }
        
        rectangle.render(builder.build());

        

        try {
            ItemStack stack = item.createItemStack();
            if (stack != null) {
                float itemX = x + (size - 16) / 2f;
                float itemY = y + (size - 16) / 2f;
                
                matrix.push();
                matrix.translate(0, 0, -100);
                context.drawItem(stack, (int)itemX, (int)itemY);
                matrix.pop();
            }
        } catch (Exception e) {
            
        }
    }

    
    private void renderPriceInput(DrawContext context, MatrixStack matrix, int mouseX, int mouseY) {
        float inputHeight = 20;
        float buttonWidth = 70;
        float buttonHeight = 20;
        
        
        float inputY = y + height - 8 - inputHeight;
        float buttonY = y + height - 8 - buttonHeight;
        
        
        float inputX = x + 8;
        
        
        float inputWidth = width - 8 - buttonWidth - 8 - 8; 
        
        
        float buttonX = inputX + inputWidth + 8;
        
        if (selectedItem != null) {
            String rawName = selectedItem.getDisplayName();
            String itemName = rawName != null ? normalizeDisplayName(rawName) : "Unknown";
            if (itemName == null) itemName = "Unknown";
            
            
            float nameX = x + 8;
            
            float fontSize = 14;
            float nameY = inputY - 8 - fontSize + 2; 

            ItemStack stack = selectedItem.createItemStack();
            if (stack != null) {
                float itemIconX = nameX;
                float itemIconY = nameY + 4.5f; 
                matrix.push();
                matrix.scale(0.7f, 0.7f, 1f);
                context.drawItem(stack, (int)(itemIconX / 0.7f), (int)(itemIconY / 0.7f));
                matrix.pop();
            }

            
            
            float iconWidth = 16 * 0.7f; 
            float textX = nameX + iconWidth + 2.5f; 
            Fonts.getSize(14, SuisseIntlSemiBold).drawString(matrix, itemName, textX, nameY + 9, ThemeManager.textColor.getColor());
        }
        
        boolean inputHovered = Calculate.isHovered(mouseX, mouseY, inputX, inputY, inputWidth, inputHeight);
        
        rectangle.render(ShapeProperties.create(matrix, inputX, inputY, inputWidth, inputHeight)
                .round(6f)
                .softness(1)
                .thickness(2)
                .outlineColor(new Color(47, 47, 50, 255).getRGB())
                .color(ThemeManager.offModuleColor.getColor())
                .build());
        
        String displayText;
        if (selectedItem != null && isEditingPrice) {
            displayText = priceInput.isEmpty() ? "" : formatPriceWithDots(priceInput);
        } else if (selectedItem != null) {
            int currentPrice = customPrices.getOrDefault(selectedItem, selectedItem.getPrice());
            displayText = currentPrice > 0 ? formatPrice(currentPrice) : "Введите цену...";
        } else {
            displayText = "Выберите предмет";
        }
        
        ScissorAssist scissor = Essence.getInstance().getScissorManager();
        scissor.push(matrix.peek().getPositionMatrix(), inputX - 1, inputY, inputWidth, inputHeight);
        
        float textX = inputX + 5;
        int textColor = isEditingPrice ? ThemeManager.textColor.getColor() : new Color(137, 137, 140, 255).getRGB();
         Fonts.getSize(15, SuisseIntlSemiBold).drawString(matrix, displayText, textX, inputY + (inputHeight / 2) - 1.5F, textColor);
        
        long cursorTime = System.currentTimeMillis();
        boolean focused = isEditingPrice && (cursorTime % 1000 < 500);
        if (focused) {
            FontRenderer textFont = Fonts.getSize(15, SuisseIntlSemiBold);
            
            
            String textBeforeCursor = priceInput.substring(0, Math.min(cursorPosition, priceInput.length()));
            String formattedBeforeCursor = formatPriceWithDots(textBeforeCursor);
            float cursorX = textFont.getStringWidth(formattedBeforeCursor);
            float leftCursorX = inputX + 5 + cursorX;
            rectangle.render(ShapeProperties.create(matrix, leftCursorX, inputY + (inputHeight / 2) - 3.5F, 0.5F, 7)
                    .color(ColorAssist.getText(1F))
                    .build());
        }
        
        scissor.pop();
        
        renderSearchButton(context, matrix, mouseX, mouseY);
    }
    
    private void renderSearchButton(DrawContext context, MatrixStack matrix, int mouseX, int mouseY) {
        float buttonWidth = 70;
        float buttonHeight = 20;
        
        
        float buttonY = y + height - 8 - buttonHeight;
        
        
        float inputX = x + 8;
        float inputWidth = width - 8 - buttonWidth - 8 - 8; 
        float buttonX = inputX + inputWidth + 8; 
        
        boolean buttonHovered = Calculate.isHovered(mouseX, mouseY, buttonX, buttonY, buttonWidth, buttonHeight);
        long currentTime = System.currentTimeMillis();
        boolean isClicked = (currentTime - buttonClickTime) < 200;
        
        int[] colors;
        if (isClicked) {
            colors = new int[]{-1, -1, -1, 5};
        } else {
            colors = new int[]{ColorAssist.getClientColor(), ColorAssist.getClientColor2(),
                    ColorAssist.getClientColor(), ColorAssist.getClientColor2()};
        }
        
        String buttonAnimationKey = "autobuy_search_button_" + System.identityHashCode(this);
        float buttonScale = AnimationHelper.getAnimationValue(buttonAnimationKey, 1f);
        
        float scaledButtonWidth = buttonWidth * buttonScale;
        float scaledButtonHeight = buttonHeight * buttonScale;
        float scaledButtonX = buttonX + (buttonWidth - scaledButtonWidth) / 2f;
        float scaledButtonY = buttonY + (buttonHeight - scaledButtonHeight) / 2f;
        
        rectangle.render(ShapeProperties.create(matrix, scaledButtonX, scaledButtonY, scaledButtonWidth, scaledButtonHeight)
                .round(4f)
                .color(colors[0], colors[1], colors[2], colors[3])
                .build());
        
        String buttonText = "Найти на /ah";
        FontRenderer essenceFont = Fonts.getSize(16, ESSENCE);
        FontRenderer textFont = Fonts.getSize(15, SuisseIntlSemiBold);
        
        float iconWidth = essenceFont.getStringWidth("z");
        float textWidth = textFont.getStringWidth(buttonText);
        float totalWidth = iconWidth + 3 + textWidth; 
        
        float startX = buttonX + (buttonWidth - totalWidth) / 2f;
        float textY = buttonY + (buttonHeight - textFont.getStringHeight(buttonText)) / 2f;
        
        
        essenceFont.drawString(matrix, "z", startX, textY + 7, ThemeManager.textColor.getColor());
        
        
        textFont.drawString(matrix, buttonText, startX + iconWidth + 3, textY + 7, ThemeManager.textColor.getColor());
    }
    
    private float calculateContentHeight() {
        List<AutoBuyableItem> allItems = getItemsForCurrentProfile();
        float cardSize = 21;
        float cardSpacing = 6;
        int CardsCountLine = 10;
        int rows = (int)Math.ceil((double)allItems.size() / CardsCountLine);
        return rows * (cardSize + cardSpacing);
    }
    
    private void renderScrollbar(DrawContext context, MatrixStack matrix) {
        List<AutoBuyableItem> allItems = getItemsForCurrentProfile();
        if (allItems.isEmpty()) return;
        
        float listY = y + 31;
        float listHeight;
        if (selectedItem != null) {
            float inputHeight = 20;
            float inputY = y + height - 8 - inputHeight;
            float fontSize = 14;
            float nameY = inputY - 8 - fontSize + 2;
            float itemIconY = nameY + 4.5f;
            listHeight = itemIconY - 8 - listY;
        } else {
            listHeight = height - 85;
        }
        
        
        float scrollbarX = x + width - 8 - SCROLLBAR_WIDTH;
        float scrollbarY = listY;
        float scrollbarHeight = listHeight;
        
        
        rectangle.render(ShapeProperties.create(matrix, scrollbarX+2.5, scrollbarY, SCROLLBAR_WIDTH, scrollbarHeight)
                .round(1f)
                .color(SCROLLBAR_BG_COLOR)
                .build());
        
        
        float contentHeight = calculateContentHeight();
        
        if (contentHeight > listHeight) {
            float maxScroll = contentHeight - listHeight;
            float scrollProgress = Math.abs(smoothedScroll) / maxScroll;
            float thumbHeight = Math.max(15, (listHeight / contentHeight) * scrollbarHeight);
            float thumbY = scrollbarY + scrollProgress * (scrollbarHeight - thumbHeight);
            
            rectangle.render(ShapeProperties.create(matrix, scrollbarX+2.5, thumbY, SCROLLBAR_WIDTH, thumbHeight)
                    .round(1f)
                    .color(ColorAssist.getClientColor())
                    .build());
        }
    }
    
    
    
    
    private code.essence.features.module.setting.implement.BooleanSetting getOrCreateParserSetting(AutoBuyableItem item) {
        return parserSettings.computeIfAbsent(item, k -> {
            code.essence.features.module.setting.implement.BooleanSetting setting = 
                new code.essence.features.module.setting.implement.BooleanSetting("Парсить цену", "Включить автопарсинг цены для этого предмета");
            
            String name = item.getDisplayName();
            boolean enabled = false;
            if (name != null) {
                String key = name.toLowerCase();
                String serverKey = getCurrentServerMode().name();
                java.util.Set<String> parserEnabledItems = parserEnabledItemsByServer.getOrDefault(serverKey, new java.util.HashSet<>());
                enabled = parserEnabledItems.contains(key);
            }
            setting.setValue(enabled);
            return setting;
        });
    }
    
    
    private SliderSettings getOrCreateDurabilitySetting(AutoBuyableItem item) {
        return durabilitySettings.computeIfAbsent(item, k -> {
            
            String serverKey = getCurrentServerMode().name();
            String itemName = item.getDisplayName();
            float savedValue = 0f;

            if (itemName != null) {
                Map<String, Float> serverSettings = durabilitySettingsByServer.get(serverKey);
                if (serverSettings != null) {
                    Float saved = serverSettings.get(itemName.toLowerCase());
                    if (saved != null) {
                        savedValue = saved;
                    }
                }
            }

           SliderSettings setting =
                new SliderSettings("Минимальная прочность в %", "Минимальная прочность предмета в процентах")
                .range(0, 100)
                .setValue(savedValue)
                .step(1f);
            return setting;
        });
    }
    
    
    public boolean checkDurability(net.minecraft.item.ItemStack stack, AutoBuyableItem item) {
        
        if (!stack.isDamageable()) {
            return true;
        }

        
        float minDurabilityPercent = getDurabilitySetting(item);
        
        
        if (minDurabilityPercent <= 0) {
            return true;
        }

        
        
        int maxDurability = stack.getMaxDamage();
        if (maxDurability <= 0) {
            return true; 
        }
        
        int currentDamage = stack.getDamage();
        int currentDurability = maxDurability - currentDamage;
        
        
        float durabilityPercent = (currentDurability / (float) maxDurability) * 100.0f;
        
        
        return durabilityPercent >= minDurabilityPercent;
    }

    
    public float getDurabilitySetting(AutoBuyableItem item) {
        var durabilitySetting = durabilitySettings.get(item);
        if (durabilitySetting != null && durabilitySetting.getValue() > 0) {
            return durabilitySetting.getValue();
        }

        
        String serverKey = getCurrentServerMode().name();
        String itemName = item.getDisplayName();
        if (itemName != null) {
            Map<String, Float> serverSettings = durabilitySettingsByServer.get(serverKey);
            if (serverSettings != null) {
                Float saved = serverSettings.get(itemName.toLowerCase());
                if (saved != null && saved > 0) {
                    return saved;
                }
            }
        }
        return -1;
    }
    
    
    private code.essence.display.screens.clickgui.components.implement.settings.CheckboxComponent getOrCreateParserComponent(
            AutoBuyableItem item, code.essence.features.module.setting.implement.BooleanSetting setting) {
        return parserComponents.computeIfAbsent(item, k -> {
            code.essence.display.screens.clickgui.components.implement.settings.CheckboxComponent component = 
                new code.essence.display.screens.clickgui.components.implement.settings.CheckboxComponent(setting);
            
            component.setModule(null);
            return component;
        });
    }
    
    
    private code.essence.display.screens.clickgui.components.implement.settings.SliderComponent getOrCreateDurabilityComponent(
            AutoBuyableItem item, code.essence.features.module.setting.implement.SliderSettings setting) {
        return durabilityComponents.computeIfAbsent(item, k -> {
            code.essence.display.screens.clickgui.components.implement.settings.SliderComponent component = 
                new code.essence.display.screens.clickgui.components.implement.settings.SliderComponent(setting);
            
            component.setModule(null);
            return component;
        });
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            float inputHeight = 20;
            float buttonWidth = 70;
            float buttonHeight = 20;
            
            
            float inputY = y + height - 8 - inputHeight;
            float buttonY = y + height - 8 - buttonHeight;
            
            
            float inputX = x + 8;
            
            
            float inputWidth = width - 8 - buttonWidth - 8 - 8; 
            
            
            float buttonX = inputX + inputWidth + 8;
            
            if (Calculate.isHovered(mouseX, mouseY, buttonX, buttonY, buttonWidth, buttonHeight) && selectedItem != null) {
                String buttonAnimationKey = "autobuy_search_button_" + System.identityHashCode(this);
                AnimationHelper.startAnimation(buttonAnimationKey, 1f, 1.1f, 100, AnimationHelper.EasingType.EASE_OUT);
                buttonClickTime = System.currentTimeMillis();
                
                
                String itemName = selectedItem.getSearchName();
                
                String cleanItemName = itemName.replace("[★] ", "").replace("[★]", "").trim();
                
                
                cleanItemName = convertLevelFormat(cleanItemName);
                
                String command = "/ah search " + cleanItemName;
                
                if (mc.player != null && mc.player.networkHandler != null) {
                    CommandSender.sendCommand(mc.player,command);
                }
                return true;
            }
            
            if (Calculate.isHovered(mouseX, mouseY, inputX, inputY, inputWidth, inputHeight) && selectedItem != null) {
                isEditingPrice = true;
                isTextSelected = false;
                int currentPrice = customPrices.getOrDefault(selectedItem, selectedItem.getPrice());
                priceInput = currentPrice > 0 ? String.valueOf(currentPrice) : "";
                cursorPosition = priceInput.length(); 
                return true;
            }
            
            
            float listY = y + 31;
            List<AutoBuyableItem> allItems = getItemsForCurrentProfile();
            
            
            float cardSize = 21;
            float cardSpacing = 6;
            int CardsCountLine = 10;
            
            
            float startX = x + 8;
            float currentX = startX;
            
            float currentY = listY + smoothedScroll;
            
            for (int i = 0; i < allItems.size(); i++) {
                AutoBuyableItem item = allItems.get(i);
                if (item == null) {
                    continue;
                }
                
                if (Calculate.isHovered(mouseX, mouseY, currentX, currentY, cardSize, cardSize)) {
                    
                    if (settingsItem == item) {
                        
                        
                        settingsAnimatedItem = item;
                        settingsItem = null;
                        
                        settingsPanelAnimation.setDirection(Direction.FORWARDS);
                        settingsPanelAnimation.reset();
                        settingsPanelAnimation.setDirection(Direction.BACKWARDS);
                        settingsPanelAnimation.reset();
                    } else {
                        
                        
                        settingsAnimatedItem = item;
                        settingsItem = item;
                        selectedItem = item;
                        isEditingPrice = false;
                        
                        
                        
                        settingsPanelAnimation.setDirection(Direction.BACKWARDS);
                        settingsPanelAnimation.reset();
                        settingsPanelAnimation.setDirection(Direction.FORWARDS);
                        settingsPanelAnimation.reset();
                        
                        
                        getOrCreateParserSetting(item);
                        getOrCreateDurabilitySetting(item);
                    }
                    return true;
                }
                
                currentX += cardSize + cardSpacing;
                if ((i + 1) % CardsCountLine == 0) {
                    currentX = startX;
                    currentY += cardSize + cardSpacing;
                }
            }
        }
        if (button == 1) {
            
            if (settingsItem != null) {
                float panelX = settingsPanelRenderer.getPanelX();
                float panelY = settingsPanelRenderer.getPanelY();
                float panelWidth = settingsPanelRenderer.getPanelWidth();
                float panelHeight = settingsPanelRenderer.getPanelHeight();
                if (Calculate.isHovered(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight)) {
                    
                    return super.mouseClicked(mouseX, mouseY, button);
                }
            }
            
            
            float listY = y + 31;
            List<AutoBuyableItem> allItems = getItemsForCurrentProfile();

            float cardSize = 21;
            float cardSpacing = 6;
            int CardsCountLine = 10;
            float startX = x + 8;
            float currentX = startX;
            
            float currentY = listY + smoothedScroll;

            for (int i = 0; i < allItems.size(); i++) {
                AutoBuyableItem item = allItems.get(i);
                if (item == null) {
                    currentX += cardSize + cardSpacing;
                    if ((i + 1) % CardsCountLine == 0) {
                        currentX = startX;
                        currentY += cardSize + cardSpacing;
                    }
                    continue;
                }
                if (Calculate.isHovered(mouseX, mouseY, currentX, currentY, cardSize, cardSize)) {
                    toggleParserEnabled(item);
                    return true;
                }
                currentX += cardSize + cardSpacing;
                if ((i + 1) % CardsCountLine == 0) {
                    currentX = startX;
                    currentY += cardSize + cardSpacing;
                }
            }
        }
        
        
        if (settingsItem != null && button == 0) {
            
            float panelX = settingsPanelRenderer.getPanelX();
            float panelY = settingsPanelRenderer.getPanelY();
            float panelWidth = settingsPanelRenderer.getPanelWidth();
            float panelHeight = settingsPanelRenderer.getPanelHeight();
            if (Calculate.isHovered(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight)) {
                float categoryY = panelY + 4;
                float settingsStartY = categoryY + 22 + 8;
                float settingsX = panelX + 5;
                float settingsY = settingsStartY;
                float settingsWidth = panelWidth - 10;
                float settingSpacing = 10;
                
                
                BooleanSetting parserSetting = getOrCreateParserSetting(settingsItem);
                float checkboxX = settingsX + settingsWidth - 12.5f;
                float checkboxY = settingsY + 3f;
                float checkboxAreaWidth = checkboxX + 7 - (settingsX + 4); 
                float checkboxAreaHeight = 15; 
                
                
                if (Calculate.isHovered(mouseX, mouseY, settingsX + 4, settingsY, checkboxAreaWidth, checkboxAreaHeight)) {
                    String toggleAnimationKey = "autobuy_checkbox_" + System.identityHashCode(settingsItem);
                    AnimationHelper.startAnimation(toggleAnimationKey, 1f, 1.1f, 100, AnimationHelper.EasingType.EASE_OUT);
                    
                    
                    boolean oldValue = parserSetting.isValue();
                    parserSetting.setValue(!parserSetting.isValue());
                    
                    
                    if (parserSetting.isValue() != oldValue) {
                        String name = settingsItem.getDisplayName();
                        if (name != null) {
                            String key = name.toLowerCase();
                            String serverKey = getCurrentServerMode().name();
                            java.util.Set<String> parserEnabledItems = parserEnabledItemsByServer.computeIfAbsent(serverKey, k -> new java.util.HashSet<>());
                            if (parserSetting.isValue()) {
                                parserEnabledItems.add(key);
                            } else {
                                parserEnabledItems.remove(key);
                            }
                            
                            
                            try {
                                Essence.getInstance().getFileController().saveFile(code.essence.utils.client.managers.file.impl.AutoBuyConfigFile.class);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    return true;
                }
                
                
                
                ItemStack itemStack = settingsItem.createItemStack();
                if (itemStack != null && itemStack.isDamageable()) {
                    settingsY += 15 + settingSpacing;
                   SliderSettings durabilitySetting = getOrCreateDurabilitySetting(settingsItem);
                    float sliderY = settingsY + 6;
                    float sliderWidth = settingsWidth - 10;
                    float sliderHeight = 4;
                    
                    if (Calculate.isHovered(mouseX, mouseY, settingsX + 5, sliderY, sliderWidth, sliderHeight)) {
                        sliderDragging.put(settingsItem, true);
                        
                        updateSliderValue(settingsItem, durabilitySetting, (int)mouseX, settingsX + 5, sliderWidth);
                        return true;
                    }
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        
        if (settingsItem != null && button == 0 && sliderDragging.getOrDefault(settingsItem, false)) {
            ItemStack itemStack = settingsItem.createItemStack();
            if (itemStack != null && itemStack.isDamageable()) {
                code.essence.features.module.setting.implement.SliderSettings durabilitySetting = getOrCreateDurabilitySetting(settingsItem);
                float settingsX = settingsPanelRenderer.getPanelX() + 5;
                float sliderWidth = settingsPanelRenderer.getPanelWidth() - 10;
                updateSliderValue(settingsItem, durabilitySetting, (int)mouseX, settingsX + 5, sliderWidth);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        
        if (settingsItem != null && button == 0) {
            sliderDragging.put(settingsItem, false);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    
    private void updateSliderValue(AutoBuyableItem item, SliderSettings setting, int mouseX, float sliderStartX, float sliderWidth) {
        
        
        
        
        
        float difference = MathHelper.clamp(mouseX - sliderStartX + 0.5f, 0, sliderWidth);
        float value = (difference / sliderWidth) * (setting.getMax() - setting.getMin()) + setting.getMin();
        
        if (setting.isInteger()) {
            value = (int) value;
        } else if (setting.getStep() > 0) {
            float steps = Math.round(value / setting.getStep());
            value = steps * setting.getStep();
        }
        
        
        value = MathHelper.clamp(value, setting.getMin(), setting.getMax());
        setting.setValue(value);

        
        String serverKey = getCurrentServerMode().name();
        String itemName = item.getDisplayName();
        if (itemName != null) {
            setDurabilitySetting(serverKey, itemName, value);
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        
        if (HistoryRenderer.getInstance().mouseScrolled(mouseX, mouseY, vertical)) {
            return true;
        }
        
        if (Calculate.isHovered(mouseX, mouseY, x + 1.5f, y + 1, width - 3, height - 23)) {
            scroll += vertical * 20;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }
    
    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (isEditingPrice && Character.isDigit(chr)) {
            
            if (isTextSelected) {
                priceInput = String.valueOf(chr);
                cursorPosition = 1;
                isTextSelected = false;
            } else {
                
                priceInput = priceInput.substring(0, cursorPosition) + chr + priceInput.substring(cursorPosition);
                cursorPosition++;
            }
            return true;
        }
        return super.charTyped(chr, modifiers);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isEditingPrice) {
            
            if (Screen.hasControlDown()) {
                switch (keyCode) {
                    case GLFW.GLFW_KEY_A: 
                        if (!priceInput.isEmpty()) {
                            isTextSelected = true;
                            GLFW.glfwSetClipboardString(window.getHandle(), priceInput);
                        }
                        return true;
                    case GLFW.GLFW_KEY_C: 
                        if (!priceInput.isEmpty()) {
                            GLFW.glfwSetClipboardString(window.getHandle(), priceInput);
                        }
                        return true;
                    case GLFW.GLFW_KEY_V: 
                        String clipboardText = GLFW.glfwGetClipboardString(window.getHandle());
                        if (clipboardText != null) {
                            
                            StringBuilder digitsOnly = new StringBuilder();
                            for (char c : clipboardText.toCharArray()) {
                                if (Character.isDigit(c)) {
                                    digitsOnly.append(c);
                                }
                            }
                            if (digitsOnly.length() > 0) {
                                if (isTextSelected) {
                                    priceInput = digitsOnly.toString();
                                    cursorPosition = priceInput.length();
                                } else {
                                    priceInput = priceInput.substring(0, cursorPosition) + digitsOnly.toString() + priceInput.substring(cursorPosition);
                                    cursorPosition += digitsOnly.length();
                                }
                            }
                        }
                        isTextSelected = false;
                        return true;
                }
            }
            
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (isTextSelected) {
                    
                    priceInput = "";
                    cursorPosition = 0;
                    isTextSelected = false;
                } else if (!priceInput.isEmpty() && cursorPosition > 0) {
                    
                    priceInput = priceInput.substring(0, cursorPosition - 1) + priceInput.substring(cursorPosition);
                    cursorPosition--;
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
                
                if (!priceInput.isEmpty() && cursorPosition < priceInput.length()) {
                    priceInput = priceInput.substring(0, cursorPosition) + priceInput.substring(cursorPosition + 1);
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
                
                if (cursorPosition > 0) {
                    cursorPosition--;
                }
                isTextSelected = false;
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                
                if (cursorPosition < priceInput.length()) {
                    cursorPosition++;
                }
                isTextSelected = false;
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_UP) {
                
                if (selectedItem != null) {
                    try {
                        int currentPrice = priceInput.isEmpty() ? 0 : Integer.parseInt(priceInput);
                        currentPrice = Math.max(0, currentPrice + 1);
                        priceInput = String.valueOf(currentPrice);
                        cursorPosition = priceInput.length();
                    } catch (NumberFormatException ignored) {
                        priceInput = "1";
                        cursorPosition = 1;
                    }
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
                
                if (selectedItem != null) {
                    try {
                        int currentPrice = priceInput.isEmpty() ? 0 : Integer.parseInt(priceInput);
                        currentPrice = Math.max(0, currentPrice - 1);
                        priceInput = currentPrice == 0 ? "" : String.valueOf(currentPrice);
                        cursorPosition = priceInput.length();
                    } catch (NumberFormatException ignored) {
                        priceInput = "";
                        cursorPosition = 0;
                    }
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER && selectedItem != null) {
                try {
                    if (!priceInput.isEmpty()) {
                        int newPrice = Integer.parseInt(priceInput);
                        setCustomPrice(selectedItem, newPrice, true);
                    } else {
                        setCustomPrice(selectedItem, 0, true);
                    }
                } catch (NumberFormatException ignored) {}
                isEditingPrice = false;
                priceInput = "";
                cursorPosition = 0;
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                isEditingPrice = false;
                priceInput = "";
                cursorPosition = 0;
                return true;
            }
        }
        
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && shouldCloseOnEsc()) {
            animation.setDirection(Direction.BACKWARDS);
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
        if (animation.finished(Direction.BACKWARDS)) {
            super.close();
        }
    }
    
    @Override
    public void tick() {
        close();
        super.tick();
    }
    
    private String formatPrice(int price) {
        StringBuilder result = new StringBuilder();
        String priceStr = String.valueOf(price);
        int count = 0;
        for (int i = priceStr.length() - 1; i >= 0; i--) {
            if (count > 0 && count % 3 == 0) {
                result.insert(0, '.');
            }
            result.insert(0, priceStr.charAt(i));
            count++;
        }
        return result.toString();
    }
    
    private String formatPriceWithDots(String priceInput) {
        if (priceInput == null || priceInput.isEmpty()) return "";
        StringBuilder result = new StringBuilder();
        int count = 0;
        for (int i = priceInput.length() - 1; i >= 0; i--) {
            char c = priceInput.charAt(i);
            if (Character.isDigit(c)) {
                if (count > 0 && count % 3 == 0) {
                    result.insert(0, '.');
                }
                result.insert(0, c);
                count++;
            }
        }
        return result.toString();
    }
    
    
    private String convertLevelFormat(String itemName) {
        if (itemName == null || itemName.isEmpty()) return itemName;
        
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(.+?)\\s*\\[(\\d+)\\s*[уУ]р\\.?\\]");
        java.util.regex.Matcher matcher = pattern.matcher(itemName);
        
        if (matcher.find()) {
            String baseName = matcher.group(1).trim();
            String level = matcher.group(2);
            
            
            baseName = baseName.replace(" опыта", "").replace(" Опыта", "").trim();
            
            return baseName + " с уровнем " + level;
        }
        
        return itemName;
    }
}
