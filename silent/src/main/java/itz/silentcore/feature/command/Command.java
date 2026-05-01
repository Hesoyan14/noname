package itz.silentcore.feature.command;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public abstract class Command {
    private final String name;
    private final String description;
    private final String[] aliases;

    public Command(String name, String description, String... aliases) {
        this.name = name;
        this.description = description;
        this.aliases = aliases;
    }

    public abstract void execute(String[] args);

    public boolean matchesAlias(String input) {
        if (name.equalsIgnoreCase(input)) {
            return true;
        }
        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(input)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getSuggestions(String[] args) {
        return new ArrayList<>();
    }
}
