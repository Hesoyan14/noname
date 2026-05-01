package code.essence.display.screens.autobuy.history;

import code.essence.common.animation.Easy.Direction;
import code.essence.common.animation.Easy.EaseBackIn;
import code.essence.display.screens.autobuy.AutoBuyScreen;
import code.essence.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import code.essence.features.impl.misc.AutoBuy;
import code.essence.features.impl.render.Hud;
import code.essence.utils.display.color.ColorAssist;
import code.essence.utils.display.render.geometry.Render2D;
import code.essence.utils.display.interfaces.QuickImports;
import code.essence.utils.display.render.shape.ShapeProperties;
import code.essence.utils.display.render.post.KawaseBlur;
import code.essence.utils.display.render.shape.implement.Rectangle;
import code.essence.utils.math.calc.Calculate;
import code.essence.utils.theme.ThemeManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import code.essence.Essence;
import code.essence.utils.display.scissor.ScissorAssist;
import code.essence.utils.display.atlasfont.msdf.MsdfFont;
import code.essence.utils.display.render.systemrender.builders.Builder;
import code.essence.utils.display.render.font.Fonts;
import com.google.common.base.Suppliers;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

import static code.essence.utils.display.render.font.Fonts.Type.SuisseIntlSemiBold;

public class HistoryRenderer implements QuickImports {
    private static HistoryRenderer instance;
    private final Rectangle rectangle = new Rectangle();
    private final HistoryManager historyManager = HistoryManager.getInstance();

    private static final Supplier<MsdfFont> ESSENCE_FONT = Suppliers.memoize(() -> MsdfFont.builder().atlas("essence").data("essence").build());
    private static final Supplier<MsdfFont> SEMIBOLD_FONT = Suppliers.memoize(() -> MsdfFont.builder().atlas("suisseintl-semibold").data("suisseintl-semibold").build());
    private static final Supplier<MsdfFont> MEDIUM_FONT = Suppliers.memoize(() -> MsdfFont.builder().atlas("suisseintlmedium").data("suisseintlmedium").build());
    public final EaseBackIn animation = new EaseBackIn(400 / 2, 1f, 1.5f);

    private float x, y;
    private float lastX, lastY; 
    private final float width = 228f;

    private static final float ITEM_HEIGHT = 26f;
    private static final float ITEM_SPACING = 3f;
    private static final float PADDING = 4f; 
    private static final int VISIBLE_ITEMS = 7; 
    private static final float HEADER_HEIGHT = 32f;
    private static final float BOTTOM_PADDING = 5f;

    
    private static final float LIST_HEIGHT = VISIBLE_ITEMS * ITEM_HEIGHT + (VISIBLE_ITEMS - 1) * ITEM_SPACING;
    
    private final float height = HEADER_HEIGHT + LIST_HEIGHT + BOTTOM_PADDING;

    private float scroll = 0f;
    private float smoothedScroll = 0f;
    private float targetScroll = 0f; 

    private static final float SCROLLBAR_WIDTH = 2f; 
    private static final int SELLER_COLOR = new Color(157, 157, 160, 255).getRGB();
    private static final int SCROLLBAR_BG_COLOR = new Color(27, 27, 30, 255).getRGB(); 

    private HistoryRenderer() {}

    public static HistoryRenderer getInstance() {
        if (instance == null) {
            instance = new HistoryRenderer();
        }
        return instance;
    }

    public float getScaleAnimation() {
        
        if (mc.currentScreen instanceof AutoBuyScreen) {
            return AutoBuyScreen.INSTANCE.getScaleAnimation();
        }
        
        return (float) animation.getOutput();
    }

    public boolean shouldRender() {
        if (mc.currentScreen == null) {
            
            if (animation.getDirection() == Direction.FORWARDS) {
                animation.setDirection(Direction.BACKWARDS);
            }
            return false;
        }
        
        AutoBuy autoBuy = AutoBuy.getInstance();
        if (autoBuy == null || !autoBuy.isState()) {
            if (animation.getDirection() == Direction.FORWARDS) {
                animation.setDirection(Direction.BACKWARDS);
            }
            return false;
        }

        boolean shouldRender = false;

        
        if (mc.currentScreen instanceof AutoBuyScreen) {
            shouldRender = true;
        }
        
        else if (mc.currentScreen instanceof GenericContainerScreen screen) {
            String title = screen.getTitle().getString();
            shouldRender = title.contains("Аукцион") || title.contains("Аукционы") ||
                          title.contains("Поиск") || title.contains("Хранилище");
        }

        
        if (!(mc.currentScreen instanceof AutoBuyScreen)) {
            if (shouldRender && animation.getDirection() != Direction.FORWARDS) {
                animation.setDirection(Direction.FORWARDS);
            } else if (!shouldRender && animation.getDirection() == Direction.FORWARDS) {
                animation.setDirection(Direction.BACKWARDS);
            }
        }

        return shouldRender;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean shouldRender = shouldRender();
        float scaleAnimation = getScaleAnimation();

        
        if (!shouldRender && scaleAnimation <= 0) {
            return;
        }

        
        renderWithAnimation(context, mouseX, mouseY, delta, scaleAnimation);
    }

    private void renderWithAnimation(DrawContext context, int mouseX, int mouseY, float delta, float scaleAnimation) {
        MatrixStack matrix = context.getMatrices();

        
        if (mc.currentScreen instanceof GenericContainerScreen screen) {
            int containerWidth = 176;
            int containerX = (screen.width - containerWidth) / 2;
            x = Math.max(5, containerX - width - 10);
            y = (screen.height - height) / 2f;
            lastX = x;
            lastY = y;
        } else if (mc.currentScreen instanceof AutoBuyScreen) {
            
            float autoBuyWidth = 284.5f;
            float autoBuyX = (mc.getWindow().getScaledWidth() - autoBuyWidth) / 2f;
            x = Math.max(5, autoBuyX - width - 10);
            y = (mc.getWindow().getScaledHeight() - height) / 2f;
            lastX = x;
            lastY = y;
        } else {
            
            if (lastX == 0 && lastY == 0) {
                
                x = (mc.getWindow().getScaledWidth() - width) / 2f;
                y = (mc.getWindow().getScaledHeight() - height) / 2f;
            } else {
                x = lastX;
                y = lastY;
            }
        }

        
        Calculate.scale(matrix, x + width / 2f, y + height / 2f, scaleAnimation, () -> {
            
            if (Hud.blur.isValue()) {
                Render2D.rectangleWithMask(matrix.peek().getPositionMatrix(),  x, y, width, height, 7f,0, KawaseBlur.INSTANCE.fbos.getFirst().getColorAttachment());

            }

            rectangle.render(ShapeProperties.create(matrix, x, y, width, height)
                    .round(7.0f)
                    .color(ThemeManager.BackgroundGui.getColor())
                    .build());

            
            renderHeader(context, matrix);

            
            renderPurchaseList(context, matrix, mouseX, mouseY);

            
            renderScrollbar(matrix);
        });
    }

    private void renderHeader(DrawContext context, MatrixStack matrix) {
        float categoryWidth = width - 10;
        float categoryX = x + 5;
        float categoryY = y + 5;

        rectangle.render(ShapeProperties.create(matrix, categoryX, categoryY, categoryWidth, 22)
                .round(3f)
                .softness(2)
                .color(ColorAssist.getClientColor(1f), ColorAssist.getClientColor(1f),
                        ColorAssist.getClientColor2(1f), ColorAssist.getClientColor2(1f))
                .build());

        Matrix4f matrix4f = matrix.peek().getPositionMatrix();
        
        MsdfFont essenceFont = ESSENCE_FONT.get();
        MsdfFont semiboldFont = SEMIBOLD_FONT.get();
        
        if (essenceFont == null || semiboldFont == null) {
            return;
        }
        
        
        float bSize = 8f;
        float bX = categoryX + 10;
        
        float bY = categoryY + 7.1f;
        
        Builder.text()
                .font(essenceFont)
                .text("B")
                .size(bSize)
                .color(ThemeManager.textColor.getColor())
                .build()
                .render(matrix4f, bX, bY);

        
        float bWidth = essenceFont.getWidth("B", bSize);
        float historyX = bX + bWidth + 5;
        float historySize = 8f;
        float historyY = categoryY + 6.5f;
        
        Builder.text()
                .font(semiboldFont)
                .text("History")
                .size(historySize)
                .color(ThemeManager.textColor.getColor())
                .build()
                .render(matrix4f, historyX, historyY);
    }

    private void renderPurchaseList(DrawContext context, MatrixStack matrix, int mouseX, int mouseY) {
        List<PurchaseRecord> purchases = historyManager.getHistory();

        float listX = x + PADDING;
        float listY = y + HEADER_HEIGHT;

        
        float contentHeight = purchases.size() * (ITEM_HEIGHT + ITEM_SPACING) - ITEM_SPACING;
        float maxScroll = Math.max(0f, contentHeight - LIST_HEIGHT);

        
        targetScroll = MathHelper.clamp(targetScroll, -maxScroll, 0f);
        scroll = MathHelper.clamp(scroll, -maxScroll, 0f);
        smoothedScroll = Calculate.interpolate(smoothedScroll, targetScroll, 0.15f);

        
        ScissorAssist scissor = Essence.getInstance().getScissorManager();
        scissor.push(matrix.peek().getPositionMatrix(), listX, listY, width - PADDING * 2, LIST_HEIGHT);

        float currentY = listY + smoothedScroll;

        for (PurchaseRecord record : purchases) {
            
            if (currentY + ITEM_HEIGHT >= listY - ITEM_HEIGHT && currentY <= listY + LIST_HEIGHT + ITEM_HEIGHT) {
                renderPurchaseItem(context, matrix, record, listX, currentY);
            }
            currentY += ITEM_HEIGHT + ITEM_SPACING;
        }

        scissor.pop();

        
        if (purchases.isEmpty()) {
            String emptyText = "Пусто :(";
            Matrix4f matrix4f = matrix.peek().getPositionMatrix();
            
            MsdfFont mediumFont = MEDIUM_FONT.get();
            if (mediumFont == null) {
                return;
            }
            
            float textWidth = mediumFont.getWidth(emptyText, 9f);
            float textX = listX + (width - PADDING * 2 - textWidth) / 2;
            float textY = listY + LIST_HEIGHT / 2;
            float baselineHeight = mediumFont.getMetrics().baselineHeight();
            float renderY = textY - baselineHeight * 9f;
            
            Builder.text()
                    .font(mediumFont)
                    .text(emptyText)
                    .size(9f)
                    .color(SELLER_COLOR)
                    .build()
                    .render(matrix4f, textX, renderY-4);
        }
    }

    private void renderPurchaseItem(DrawContext context, MatrixStack matrix, PurchaseRecord record, float itemX, float itemY) {
        
        float itemWidth = width - PADDING - PADDING - SCROLLBAR_WIDTH - PADDING;

        
        rectangle.render(ShapeProperties.create(matrix, itemX, itemY, itemWidth, ITEM_HEIGHT)
                .round(3f)
                .color(ThemeManager.offModuleColor.getColor())
                .build());

        
        float iconSize = 14f;
        float iconX = itemX + 8f;
        float iconY = itemY + (ITEM_HEIGHT - iconSize) / 2;

        matrix.push();
        matrix.translate(iconX, iconY - .5, 0);
        float scale = iconSize / 16f;
        matrix.scale(scale, scale, 1f);
        
        net.minecraft.item.ItemStack itemToRender = record.getItem();
        if (itemToRender == null || itemToRender.isEmpty()) {
            itemToRender = new net.minecraft.item.ItemStack(net.minecraft.item.Items.PAPER);
        }
        context.drawItem(itemToRender, 0, 0);
        matrix.pop();

        
        float textX = iconX + iconSize + 4f / 1.2f;
        float nameY = itemY + 9f / 1.2f;
        Fonts.getSize(14, SuisseIntlSemiBold).drawString(matrix, record.getDisplayName(),
                textX, nameY, ThemeManager.textColor.getColor());

        Matrix4f matrix4f = matrix.peek().getPositionMatrix();

        
        float sellerY = nameY + 11f / 1.2f;
        float sellerSize = 6f;
        float baselineHeightSeller = MEDIUM_FONT.get().getMetrics().baselineHeight();
        float sellerRenderY = sellerY - baselineHeightSeller * sellerSize+3;
        Builder.text()
                .font(MEDIUM_FONT.get())
                .text(record.getSellerName())
                .size(sellerSize)
                .color(SELLER_COLOR)
                .build()
                .render(matrix4f, textX, sellerRenderY);

        
        String timeText = record.getFormattedTime();
        float timeSize = 6f;
        float timeWidth = MEDIUM_FONT.get().getWidth(timeText, timeSize);
        float timeX = itemX + itemWidth - 8f - timeWidth;

        
        String priceText = record.getFormattedPrice();
        float priceSize = 7f;
        float priceWidth = SEMIBOLD_FONT.get().getWidth(priceText, priceSize);
        float priceX = timeX + timeWidth - priceWidth; 

        
        float baselineHeightPrice = SEMIBOLD_FONT.get().getMetrics().baselineHeight();
        float priceRenderY = nameY - baselineHeightPrice * priceSize+3;
        Builder.text()
                .font(SEMIBOLD_FONT.get())
                .text(priceText)
                .size(priceSize)
                .rainbow(true)
                .rainbowColors(ColorAssist.getClientColor(), ColorAssist.getClientColor2())
                .build()
                .render(matrix4f, priceX, priceRenderY);

        
        float baselineHeightTime = MEDIUM_FONT.get().getMetrics().baselineHeight();
        float timeRenderY = sellerY - baselineHeightTime * timeSize+3;
        Builder.text()
                .font(MEDIUM_FONT.get())
                .text(timeText)
                .size(timeSize)
                .color(SELLER_COLOR)
                .build()
                .render(matrix4f, timeX, timeRenderY);
    }
    
    private void renderScrollbar(MatrixStack matrix) {
        List<PurchaseRecord> purchases = historyManager.getHistory();
        if (purchases.isEmpty()) return;
        
        
        float scrollbarX = x + width - PADDING - SCROLLBAR_WIDTH;
        float scrollbarY = y + HEADER_HEIGHT;
        float scrollbarHeight = LIST_HEIGHT;
        
        
        rectangle.render(ShapeProperties.create(matrix, scrollbarX, scrollbarY, SCROLLBAR_WIDTH, scrollbarHeight)
                .round(1f)
                .color(SCROLLBAR_BG_COLOR)
                .build());
        
        
        float contentHeight = purchases.size() * (ITEM_HEIGHT + ITEM_SPACING) - ITEM_SPACING;
        
        if (contentHeight > LIST_HEIGHT) {
            float maxScroll = contentHeight - LIST_HEIGHT;
            float scrollProgress = Math.abs(smoothedScroll) / maxScroll;
            float thumbHeight = Math.max(15, (LIST_HEIGHT / contentHeight) * scrollbarHeight);
            float thumbY = scrollbarY + scrollProgress * (scrollbarHeight - thumbHeight);
            
            rectangle.render(ShapeProperties.create(matrix, scrollbarX, thumbY, SCROLLBAR_WIDTH, thumbHeight)
                    .round(1f)
                    .color(ColorAssist.getClientColor())
                    .build());
        }
    }

    public static float getDurabilitySetting(AutoBuyableItem item) {
        if (item == null || AutoBuyScreen.INSTANCE == null) {
            return -1;
        }
        var durabilitySettings = AutoBuyScreen.INSTANCE.getDurabilitySettings();
        if (durabilitySettings == null) {
            return -1;
        }
        var durabilitySetting = durabilitySettings.get(item);
        if (durabilitySetting != null && durabilitySetting.getValue() > 0) {
            return durabilitySetting.getValue();
        }
        return -1;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!shouldRender()) return false;
        
        if (Calculate.isHovered(mouseX, mouseY, x, y, width, height)) {
            
            float scrollStep = ITEM_HEIGHT + ITEM_SPACING;
            targetScroll += amount * scrollStep;
            return true;
        }
        return false;
    }
}


