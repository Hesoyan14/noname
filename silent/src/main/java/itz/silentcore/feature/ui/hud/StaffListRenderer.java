package itz.silentcore.feature.ui.hud;

import itz.silentcore.feature.event.impl.Render2DEvent;
import itz.silentcore.feature.ui.hud.drag.DragComponent;
import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import itz.silentcore.utils.client.ClientUtility;
import itz.silentcore.utils.render.ColorRGBA;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.joml.Quaternionf;

import java.util.*;

import static itz.silentcore.utils.render.Fonts.icons;
import static itz.silentcore.utils.render.Fonts.sf_pro;

public class StaffListRenderer extends DragComponent {

    private final Map<String, Animation> staffAnimations = new HashMap<>();
    private float smoothedHeight = 0f;
    
    private static final Map<String, String> STAFF_PREFIXES = new LinkedHashMap<>();
    
    static {
        STAFF_PREFIXES.put("Owner", "Owner");
        STAFF_PREFIXES.put("Admin", "Admin");
        STAFF_PREFIXES.put("Moderator", "Moder");
        STAFF_PREFIXES.put("Moder", "Moder");
        STAFF_PREFIXES.put("Helper", "Helper");
        STAFF_PREFIXES.put("Хелпер", "Helper");
        STAFF_PREFIXES.put("Модератор", "Moder");
        STAFF_PREFIXES.put("Админ", "Admin");
        STAFF_PREFIXES.put("Владелец", "Owner");
    }

    public StaffListRenderer() {
        super("stafflist");
        setDraggable(true);
        setAllowDragX(true);
        setAllowDragY(true);
        setX(30);
        setY(150);
    }

    @Override
    public void render(Render2DEvent event) {
        var context = event.getContext();
        var mc = MinecraftClient.getInstance();

        float x = getX();
        float y = getY();
        float paddingX = 6f;
        float paddingY = 4f;
        float radius = 5.0f;
        float textSize = 8.0f;
        float iconSize = textSize + 2.0f;
        float gap = 4f;
        float lineSpacing = 2f;

        List<StaffMember> onlineStaff = getOnlineStaff();
        
        // Если нет стафа и чат открыт, показываем тестового
        boolean chatOpen = mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen;
        if (onlineStaff.isEmpty() && chatOpen) {
            onlineStaff.add(new StaffMember("xolizer", "Owner"));
        }
        
        // Если чат закрыт, немедленно удаляем тестового стаффа из анимаций
        if (!chatOpen && staffAnimations.containsKey("xolizer")) {
            staffAnimations.remove("xolizer");
        }

        Set<String> currentStaffNames = new HashSet<>();
        for (StaffMember staff : onlineStaff) {
            currentStaffNames.add(staff.name);
            
            if (!staffAnimations.containsKey(staff.name)) {
                staffAnimations.put(staff.name, new Animation(800, Easing.EXPO_OUT));
            }

            Animation anim = staffAnimations.get(staff.name);
            if (anim.getValue() < 0.5f) {
                anim = new Animation(800, Easing.EXPO_OUT);
                staffAnimations.put(staff.name, anim);
            }

            anim.animate(1.0f);
            anim.update();
        }

        staffAnimations.entrySet().removeIf(entry -> {
            if (!currentStaffNames.contains(entry.getKey())) {
                entry.getValue().animate(0);
                entry.getValue().update();
                return entry.getValue().getValue() <= 0.01;
            }
            return false;
        });

        String headerIconText = "f";
        float headerIconWidth = icons.getWidth(headerIconText, iconSize);
        String headerText = "Staff List";
        float headerTextWidth = sf_pro.getWidth(headerText, textSize);

        float sfHeight = sf_pro.getMetrics().lineHeight() * textSize + 3;
        float iconHeight = icons.getMetrics().lineHeight() * iconSize;
        float lineHeight = Math.max(sfHeight, iconHeight);

        float headerWidth = headerIconWidth + gap + headerTextWidth;
        float maxWidth = headerWidth + 25;

        for (StaffMember staff : onlineStaff) {
            Animation anim = staffAnimations.get(staff.name);
            if (anim == null || anim.getValue() <= 0.01) continue;

            String displayText = staff.name + " [" + staff.prefix + "]";
            float textWidth = sf_pro.getWidth(displayText, textSize);
            maxWidth = Math.max(maxWidth, textWidth);
        }

        float totalWidth = maxWidth + paddingX * 2f + 5;

        float currentY = paddingY;
        currentY += lineHeight + lineSpacing;

        List<StaffRenderData> renderData = new ArrayList<>();
        for (StaffMember staff : onlineStaff) {
            Animation anim = staffAnimations.get(staff.name);
            if (anim == null || anim.getValue() <= 0.01) continue;

            float animValue = (float) anim.getValue();
            renderData.add(new StaffRenderData(staff, animValue, currentY));
            currentY += (lineHeight + lineSpacing) * animValue;
        }

        float targetHeight = currentY + paddingY - lineSpacing;
        if (renderData.isEmpty()) {
            targetHeight = lineHeight + paddingY * 2f;
        }

        smoothedHeight += (targetHeight - smoothedHeight) * 0.15f;

        boolean rotateSelf = DragComponent.getDragging() == this;
        if (rotateSelf) {
            var matrices = context.getContext().getMatrices();
            matrices.push();
            matrices.translate((x + totalWidth / 2f), (y + smoothedHeight / 2f), 0);
            matrices.multiply(new Quaternionf().rotateZ((float) Math.toRadians(getRotation())));
            matrices.translate(-(x + totalWidth / 2f), -(y + smoothedHeight / 2f), 0);
        }

        context.drawBlur(x, y, totalWidth, smoothedHeight, radius, 30, ColorRGBA.of(255, 255, 255, 255));
        context.drawRect(x, y, totalWidth, smoothedHeight, radius, ColorRGBA.of(0, 0, 0, 166));

        float cursorX = x + paddingX;
        float cursorY = y + paddingY;
        float centerY = cursorY + lineHeight / 2f;
        float iconY = centerY - iconHeight / 2f;
        float sfY = centerY - sfHeight / 2f;

        ColorRGBA themeColor = ClientUtility.getThemePrimaryColorRGBA();
        context.drawText(headerIconText, icons, cursorX, iconY, iconSize, themeColor);
        cursorX += headerIconWidth + gap;
        context.drawText(headerText, sf_pro, cursorX, sfY, textSize, ColorRGBA.of(255, 255, 255));

        for (StaffRenderData data : renderData) {
            cursorX = x + paddingX;
            cursorY = y + data.yOffset;
            centerY = cursorY + lineHeight / 2f;
            sfY = centerY - sfHeight / 2f;

            float animValue = data.animValue;
            float offsetX = -5f * (1f - animValue);
            int alpha = (int) (255 * animValue);

            ColorRGBA textColor = ColorRGBA.of(255, 255, 255, alpha);
            ColorRGBA prefixColor = themeColor.withAlpha(alpha);

            float nameWidth = sf_pro.getWidth(data.staff.name, textSize);
            context.drawText(data.staff.name, sf_pro, cursorX + offsetX, sfY, textSize, textColor);
            cursorX += nameWidth + gap;

            String prefixText = "[" + data.staff.prefix + "]";
            context.drawText(prefixText, sf_pro, cursorX + offsetX, sfY, textSize, prefixColor);
        }

        setWidth(totalWidth);
        setHeight(smoothedHeight);

        if (rotateSelf) {
            var matrices = context.getContext().getMatrices();
            matrices.pop();
        }
    }

    private List<StaffMember> getOnlineStaff() {
        List<StaffMember> result = new ArrayList<>();
        var mc = MinecraftClient.getInstance();
        
        if (mc.getNetworkHandler() == null) {
            return result;
        }

        Collection<PlayerListEntry> playerList = mc.getNetworkHandler().getPlayerList();
        
        for (PlayerListEntry entry : playerList) {
            String playerName = entry.getProfile().getName();
            Text displayName = entry.getDisplayName();
            if (displayName == null) {
                continue;
            }
            
            String displayText = displayName.getString();
            
            for (Map.Entry<String, String> prefixEntry : STAFF_PREFIXES.entrySet()) {
                String searchPrefix = prefixEntry.getKey();
                String shortPrefix = prefixEntry.getValue();
                
                if (displayText.contains(searchPrefix)) {
                    result.add(new StaffMember(playerName, shortPrefix));
                    break;
                }
            }
        }
        
        return result;
    }

    private static class StaffMember {
        final String name;
        final String prefix;

        StaffMember(String name, String prefix) {
            this.name = name;
            this.prefix = prefix;
        }
    }

    private static class StaffRenderData {
        final StaffMember staff;
        final float animValue;
        final float yOffset;

        StaffRenderData(StaffMember staff, float animValue, float yOffset) {
            this.staff = staff;
            this.animValue = animValue;
            this.yOffset = yOffset;
        }
    }
}
