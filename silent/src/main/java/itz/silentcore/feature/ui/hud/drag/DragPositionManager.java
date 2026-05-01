package itz.silentcore.feature.ui.hud.drag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DragPositionManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("silentcore/hud.silentcore");

    public static void save(List<DragComponent> components) {
        try {
            CONFIG_FILE.getParentFile().mkdirs();

            Map<String, PositionData> positions = new HashMap<>();
            for (DragComponent component : components) {
                String id = component.getId();
                if (id != null && !id.isEmpty()) {
                    positions.put(id, new PositionData(component.getX(), component.getY()));
                }
            }

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(positions, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void load(List<DragComponent> components) {
        if (!CONFIG_FILE.exists()) return;

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Type type = new TypeToken<Map<String, PositionData>>(){}.getType();
            Map<String, PositionData> positions = GSON.fromJson(reader, type);

            if (positions == null) return;

            for (DragComponent component : components) {
                String id = component.getId();
                if (id != null && positions.containsKey(id)) {
                    PositionData data = positions.get(id);
                    component.setX(data.x);
                    component.setY(data.y);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class PositionData {
        float x;
        float y;

        PositionData(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}