package itz.silentcore.feature.ui.hud;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.SilentCore;
import itz.silentcore.feature.event.impl.Render2DEvent;
import itz.silentcore.feature.event.impl.ScoreboardRenderEvent;
import itz.silentcore.feature.ui.hud.drag.DragComponent;
import itz.silentcore.utils.client.ClientUtility;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.RenderContext;
import itz.silentcore.utils.render.Fonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScoreboardRenderer extends DragComponent {

    public ScoreboardRenderer() {
        super("scoreboard");
        setDraggable(true);
        setAllowDragX(true);
        setAllowDragY(true);
        setX(0);
        setY(10);

        try {
            SilentCore.getInstance().eventBus.register(this);
        } catch (Exception e) {
        }
    }

    @Subscribe
    public void onScoreboardRender(ScoreboardRenderEvent event) {
        try {
            RenderContext renderContext = event.getContext();
            DrawContext context = renderContext.getContext();
            ScoreboardObjective objective = event.getObjective();

            if (objective == null) return;

            MinecraftClient mc = MinecraftClient.getInstance();
            TextRenderer tr = mc.textRenderer;
            Scoreboard scoreboard = objective.getScoreboard();

            List<ScoreboardEntry> entries = new ArrayList<>(scoreboard.getScoreboardEntries(objective));
            entries.removeIf(ScoreboardEntry::hidden);
            entries.sort(Comparator.comparingInt(ScoreboardEntry::value).reversed());

            if (entries.size() > 15) {
                entries = entries.subList(0, 15);
            }

            Text title = objective.getDisplayName();
            int titleWidth = tr.getWidth(title);
            int maxContentWidth = titleWidth + 15;

            for (ScoreboardEntry entry : entries) {
                Team team = scoreboard.getScoreHolderTeam(entry.owner());
                Text name = Team.decorateName(team, Text.literal(entry.owner()));

                int nameWidth = tr.getWidth(name);
                maxContentWidth = Math.max(maxContentWidth, nameWidth);
            }

            float paddingX = 3f;
            float paddingY = 3f;
            float radius = 4f;

            int totalHeight = entries.size() * 9 + 12;
            float bgWidth = maxContentWidth + paddingX * 2 + 6;
            float bgHeight = totalHeight + paddingY * 2;

            float bgX = getX();
            float bgY = getY();

            setWidth(bgWidth);
            setHeight(bgHeight);

            renderContext.drawBlur(bgX, bgY, bgWidth, bgHeight, radius, 30, ColorRGBA.of(255, 255, 255, 255));
            renderContext.drawRect(bgX, bgY, bgWidth, bgHeight, radius, ColorRGBA.of(0, 0, 0, 166));
            renderContext.drawBorder(bgX, bgY, bgWidth, bgHeight, radius, 0.5f, ColorRGBA.of(80, 80, 80, 120));

            float contentX = bgX + paddingX + 2;
            float contentY = bgY + paddingY + 1;

            float iconSize = 8f;
            float iconWidth = Fonts.icons.getWidth("P", iconSize);
            ColorRGBA themeColor = ClientUtility.getThemePrimaryColorRGBA();

            renderContext.drawText("P", Fonts.icons, contentX, contentY, iconSize, themeColor);
            context.drawText(tr, title, (int)(contentX + iconWidth + 3), (int)contentY, themeColor.getRGB(), false);

            contentY += 11;

            for (ScoreboardEntry entry : entries) {
                Team team = scoreboard.getScoreHolderTeam(entry.owner());
                Text name = Team.decorateName(team, Text.literal(entry.owner()));

                context.drawText(tr, name, (int)contentX, (int)contentY, 0xFFFFFF, false);

                contentY += 9;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(Render2DEvent event) {
    }
}
