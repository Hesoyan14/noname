package itz.silentcore.feature.command.impl;

import itz.silentcore.SilentCore;
import itz.silentcore.feature.command.Command;
import itz.silentcore.manager.WaypointManager;
import itz.silentcore.utils.client.ClientUtility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class GPSCommand extends Command {
    
    private final WaypointManager waypointManager;
    
    public GPSCommand() {
        super("gps", "Управление точками на карте", "way", "waypoint");
        this.waypointManager = SilentCore.getInstance().waypointManager;
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            ClientUtility.sendMessage("§fИспользование:");
            ClientUtility.sendMessage("§7.gps add <название> <x> <y> <z> §f- Добавить точку");
            ClientUtility.sendMessage("§7.gps remove <название> §f- Удалить точку");
            ClientUtility.sendMessage("§7.gps list §f- Список точек");
            ClientUtility.sendMessage("§7.gps clear §f- Очистить все точки");
            return;
        }

        String action = args[0].toLowerCase();
        
        switch (action) {
            case "add" -> handleAdd(args);
            case "remove", "delete" -> handleRemove(args);
            case "list" -> handleList();
            case "clear" -> handleClear();
            default -> ClientUtility.sendMessage("§cНеизвестная команда. Используйте: add, remove, list, clear");
        }
    }
    
    private void handleAdd(String[] args) {
        if (args.length < 5) {
            ClientUtility.sendMessage("§cИспользование: .gps add <название> <x> <y> <z>");
            return;
        }
        
        String name = args[1];
        
        if (waypointManager.hasWaypoint(name)) {
            ClientUtility.sendMessage("§cТочка с таким названием уже существует!");
            return;
        }
        
        try {
            int x = Integer.parseInt(args[2]);
            int y = Integer.parseInt(args[3]);
            int z = Integer.parseInt(args[4]);
            
            MinecraftClient mc = MinecraftClient.getInstance();
            String server = "vanilla";
            if (mc.getNetworkHandler() != null && mc.getNetworkHandler().getServerInfo() != null) {
                server = mc.getNetworkHandler().getServerInfo().address;
            }
            
            waypointManager.addWaypoint(name, new BlockPos(x, y, z), server);
            ClientUtility.sendMessage("§aТочка §f" + name + " §aдобавлена: §7(" + x + ", " + y + ", " + z + ")");
            ClientUtility.sendMessage("§7Сервер: " + server);
        } catch (NumberFormatException e) {
            ClientUtility.sendMessage("§cНеверные координаты! Используйте целые числа.");
        }
    }
    
    private void handleRemove(String[] args) {
        if (args.length < 2) {
            ClientUtility.sendMessage("§cИспользование: .gps remove <название>");
            return;
        }
        
        String name = args[1];
        
        if (!waypointManager.hasWaypoint(name)) {
            ClientUtility.sendMessage("§cТочка с названием '" + name + "' не найдена!");
            return;
        }
        
        waypointManager.removeWaypoint(name);
        ClientUtility.sendMessage("§aТочка §c" + name + " §aудалена!");
    }
    
    private void handleList() {
        List<WaypointManager.Waypoint> waypoints = waypointManager.getWaypoints();
        
        if (waypoints.isEmpty()) {
            ClientUtility.sendMessage("§7Список точек пуст");
            return;
        }
        
        ClientUtility.sendMessage("§7§m                                    ");
        ClientUtility.sendMessage("§fСписок точек:");
        ClientUtility.sendMessage("");
        
        for (WaypointManager.Waypoint waypoint : waypoints) {
            BlockPos pos = waypoint.pos();
            ClientUtility.sendMessage("§a" + waypoint.name() + " §7- §f(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")");
            ClientUtility.sendMessage("  §7Сервер: §f" + waypoint.server());
        }
        
        ClientUtility.sendMessage("");
        ClientUtility.sendMessage("§7§m                                    ");
    }
    
    private void handleClear() {
        waypointManager.clear();
        ClientUtility.sendMessage("§aВсе точки удалены!");
    }
    
    @Override
    public List<String> getSuggestions(String[] args) {
        List<String> suggestions = new ArrayList<>();
        
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            for (String action : new String[]{"add", "remove", "list", "clear"}) {
                if (action.startsWith(prefix)) {
                    suggestions.add(action);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            String prefix = args[1].toLowerCase();
            for (WaypointManager.Waypoint waypoint : waypointManager.getWaypoints()) {
                if (waypoint.name().toLowerCase().startsWith(prefix)) {
                    suggestions.add(waypoint.name());
                }
            }
        }
        
        return suggestions;
    }
}
