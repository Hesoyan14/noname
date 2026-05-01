package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import itz.silentcore.utils.render.RenderContext;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;

@Setter
@Getter
public class TabListRenderEvent extends Event {
    private RenderContext context;
    private int scaledWindowWidth;
    private Scoreboard scoreboard;
    private ScoreboardObjective objective;
    private PlayerListHud playerListHud;

    public TabListRenderEvent(RenderContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, PlayerListHud playerListHud) {
        super(true);
        this.context = context;
        this.scaledWindowWidth = scaledWindowWidth;
        this.scoreboard = scoreboard;
        this.objective = objective;
        this.playerListHud = playerListHud;
    }
}
