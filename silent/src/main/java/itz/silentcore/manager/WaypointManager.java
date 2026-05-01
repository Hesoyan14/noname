package itz.silentcore.manager;

import lombok.Getter;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WaypointManager {
    
    @Getter
    private final List<Waypoint> waypoints = new ArrayList<>();
    
    public void addWaypoint(String name, BlockPos pos, String server) {
        waypoints.add(new Waypoint(name, pos, server));
    }
    
    public void removeWaypoint(String name) {
        waypoints.removeIf(w -> w.name().equalsIgnoreCase(name));
    }
    
    public boolean hasWaypoint(String name) {
        return waypoints.stream().anyMatch(w -> w.name().equalsIgnoreCase(name));
    }
    
    public Optional<Waypoint> getWaypoint(String name) {
        return waypoints.stream()
            .filter(w -> w.name().equalsIgnoreCase(name))
            .findFirst();
    }
    
    public void clear() {
        waypoints.clear();
    }
    
    public record Waypoint(String name, BlockPos pos, String server) {}
}
