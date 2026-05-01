package itz.silentcore.utils.math;

import itz.silentcore.feature.module.api.Module;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.PriorityQueue;

public class TaskProcessor<T> {
    public int tickCounter = 0;
    public PriorityQueue<Task<T>> activeTasks = new PriorityQueue<>((r1, r2) -> Integer.compare(r2.priority, r1.priority));

    public void tick(int deltaTime) {
        tickCounter += deltaTime;
    }

    public void addTask(Task<T> task) {
        activeTasks.removeIf(r -> r.provider.equals(task.provider));
        task.expiresIn += tickCounter;
        this.activeTasks.add(task);
    }

    public T fetchActiveTaskValue() {
        while (!activeTasks.isEmpty() && activeTasks.peek() != null && 
               (activeTasks.peek().expiresIn <= tickCounter || !activeTasks.peek().provider.isEnabled())) {
            activeTasks.poll();
        }

        if (activeTasks.isEmpty()) {
            return null;
        } else if (activeTasks.peek() != null) {
            return activeTasks.peek().value;
        } else {
            return null;
        }
    }

    @ToString
    @AllArgsConstructor
    public static class Task<T> {
        int expiresIn;
        final int priority;
        final Module provider;
        final T value;
    }
}
