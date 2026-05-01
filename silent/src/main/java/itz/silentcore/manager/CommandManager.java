package itz.silentcore.manager;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.SilentCore;
import itz.silentcore.feature.command.Command;
import itz.silentcore.feature.command.impl.ConfigCommand;
import itz.silentcore.feature.command.impl.FriendCommand;
import itz.silentcore.feature.command.impl.GPSCommand;
import itz.silentcore.feature.command.impl.HelpCommand;
import itz.silentcore.feature.command.impl.IRCCommand;
import itz.silentcore.feature.command.impl.ThemeCommand;
import itz.silentcore.feature.event.impl.ChatSendEvent;
import itz.silentcore.utils.client.ClientUtility;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CommandManager {

    private final List<Command> commands = new ArrayList<>();
    public final String prefix = ".";

    public CommandManager() {
        registerCommands();
        SilentCore.getInstance().eventBus.register(this);
    }

    private void registerCommands() {
        try {
            commands.add(new HelpCommand());
            commands.add(new ConfigCommand());
            commands.add(new ThemeCommand());
            commands.add(new IRCCommand());
            commands.add(new FriendCommand());
            commands.add(new GPSCommand());
        } catch (Exception e) {
            System.err.println("Ошибка при регистрации команд: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onChatSend(ChatSendEvent event) {
        try {
            String message = event.getMessage();

            if (message == null || message.trim().isEmpty()) {
                return;
            }

            if (!message.startsWith(prefix) && !message.startsWith("@")) {
                return;
            }

            event.cancel();

            String commandString;
            if (message.startsWith("@")) {
                commandString = message.substring(1);
            } else {
                commandString = message.substring(prefix.length());
            }

            if (commandString.trim().isEmpty()) {
                ClientUtility.sendMessage("§cВведите команду после префикса");
                return;
            }

            String[] parts = commandString.split(" ");
            String commandName = parts[0];

            if (commandName.isEmpty()) {
                ClientUtility.sendMessage("§cНеверное имя команды");
                return;
            }

            Command command = getCommand(commandName);

            if (command == null) {
                ClientUtility.sendMessage("§cКоманда не найдена: " + commandName);
                return;
            }

            String[] args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, args.length);

            try {
                command.execute(args);
            } catch (Exception e) {
                ClientUtility.sendMessage("§cОшибка при выполнении команды: " + e.getMessage());
                System.err.println("Ошибка выполнения команды " + commandName + ":");
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Критическая ошибка в CommandManager:");
            e.printStackTrace();
            ClientUtility.sendMessage("§cПроизошла критическая ошибка при обработке команды");
        }
    }

    private Command getCommand(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        for (Command command : commands) {
            try {
                if (command.matchesAlias(name)) {
                    return command;
                }
            } catch (Exception e) {
                System.err.println("Ошибка при проверке команды " + command.getName() + ":");
                e.printStackTrace();
            }
        }
        return null;
    }

    public void registerCommand(Command command) {
        if (command != null) {
            commands.add(command);
        }
    }

    public List<String> getSuggestions(String input) {
        List<String> suggestions = new ArrayList<>();

        try {
            if (input == null || !input.startsWith(prefix)) {
                return suggestions;
            }

            String commandString = input.substring(prefix.length());
            String[] parts = commandString.split(" ", -1);

            if (parts.length == 0 || parts[0].isEmpty()) {
                for (Command command : commands) {
                    suggestions.add(prefix + command.getName());
                }
                return suggestions;
            }

            String commandName = parts[0];

            if (parts.length == 1) {
                for (Command command : commands) {
                    try {
                        if (command.getName().toLowerCase().startsWith(commandName.toLowerCase())) {
                            suggestions.add(prefix + command.getName());
                        }
                        for (String alias : command.getAliases()) {
                            if (alias.toLowerCase().startsWith(commandName.toLowerCase())) {
                                suggestions.add(prefix + alias);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Ошибка при получении предложений для команды " + command.getName());
                        e.printStackTrace();
                    }
                }
            } else {
                Command command = getCommand(commandName);
                if (command != null) {
                    String[] args = new String[parts.length - 1];
                    System.arraycopy(parts, 1, args, 0, args.length);

                    try {
                        List<String> commandSuggestions = command.getSuggestions(args);
                        if (commandSuggestions != null) {
                            for (String suggestion : commandSuggestions) {
                                suggestions.add(prefix + commandName + " " + suggestion);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Ошибка при получении подсказок команд " + commandName);
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка в getSuggestions:");
            e.printStackTrace();
        }

        return suggestions;
    }
}