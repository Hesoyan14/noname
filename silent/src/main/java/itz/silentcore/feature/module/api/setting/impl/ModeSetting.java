package itz.silentcore.feature.module.api.setting.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import itz.silentcore.feature.module.api.setting.Setting;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Getter
public class ModeSetting extends Setting {
    private final List<Value> values = new ArrayList<>();
    @Setter
    private Value value;
    private boolean expanded;

    public ModeSetting(String name, String... modes) {
        super(name);
        for (String mode : modes) {
            if(mode.isEmpty()) continue;
            new Value(this, mode);
        }
        if (!values.isEmpty()) {
            value = values.getFirst();
        }
    }

    public ModeSetting(String name, Supplier<Boolean> visible, String... modes) {
        super(name);
        for (String mode : modes) {
            if(mode.isEmpty()) continue;
            new Value(this, mode);
        }
        if (!values.isEmpty()) {
            value = values.getFirst();
        }
        setVisible(visible);
    }

    public void set(String mode) {
        values.stream()
                .filter(v -> v.name().equals(mode))
                .findFirst()
                .ifPresent(v -> this.value = v);
    }

    public String get() {
        return value != null ? value.name() : "";
    }

    public boolean is(String mode) {
        return value != null && value.name().equals(mode);
    }

    public boolean is(Value otherValue) {
        return this.value == otherValue;
    }

    @Override
    public void safe(JsonObject propertiesObject) {
        propertiesObject.addProperty(String.valueOf(name), get());
    }

    @Override
    public void load(JsonObject propertiesObject) {
        this.set(propertiesObject.get(String.valueOf(name)).getAsString());
    }

    public record Value(ModeSetting parent, String name, String description) {
        public Value(ModeSetting parent, String name) {
            this(parent, name, "");
            // Не добавляем здесь, так как основной конструктор уже добавил
        }

        public Value(ModeSetting parent, String name, String description) {
            this.parent = parent;
            this.name = name;
            this.description = description;
            if (parent.values.isEmpty()) {
                this.select();
            }
            parent.values.add(this);
        }

        public Value select() {
            parent.setValue(this);
            return this;
        }

        public boolean isSelected() {
            return parent.getValue() == this;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}