package itz.silentcore.utils.script;

import java.util.ArrayList;
import java.util.List;

public class Script {
    private final List<ScriptStep> steps = new ArrayList<>();
    private int currentTick = 0;
    private boolean finished = true;

    public Script cleanup() {
        steps.clear();
        currentTick = 0;
        finished = false;
        return this;
    }

    public Script addTickStep(int tickDelay, Runnable action) {
        steps.add(new ScriptStep(tickDelay, action));
        return this;
    }

    public void update() {
        if (finished || steps.isEmpty()) return;
        
        currentTick++;
        
        List<ScriptStep> toRemove = new ArrayList<>();
        for (ScriptStep step : steps) {
            if (currentTick >= step.tickDelay) {
                step.action.run();
                toRemove.add(step);
            }
        }
        
        steps.removeAll(toRemove);
        
        if (steps.isEmpty()) {
            finished = true;
            currentTick = 0;
        }
    }

    public boolean isFinished() {
        return finished;
    }

    private record ScriptStep(int tickDelay, Runnable action) {}
}
