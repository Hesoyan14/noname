package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import itz.silentcore.utils.render.RenderContext;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.scoreboard.ScoreboardObjective;

@Setter
@Getter
public class ScoreboardRenderEvent extends Event {
    private RenderContext context;
    private ScoreboardObjective objective;

    public ScoreboardRenderEvent(RenderContext context, ScoreboardObjective objective) {
        super(false);
        this.context = context;
        this.objective = objective;
    }
}
