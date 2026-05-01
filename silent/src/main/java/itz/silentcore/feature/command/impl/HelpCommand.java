package itz.silentcore.feature.command.impl;

import itz.silentcore.SilentCore;
import itz.silentcore.feature.command.Command;
import itz.silentcore.utils.client.ClientUtility;

import java.util.List;

public class HelpCommand extends Command {
    
    public HelpCommand() {
        super("help", "Показать список команд", "h", "?");
    }

    @Override
    public void execute(String[] args) {
        List<Command> commands = SilentCore.getInstance().commandManager.getCommands();
        
        ClientUtility.sendMessage("§7§m                                    ");
        ClientUtility.sendMessage("§fДоступные команды:");
        ClientUtility.sendMessage("");
        
        for (Command command : commands) {
            String aliases = command.getAliases().length > 0 
                ? " §7(" + String.join(", ", command.getAliases()) + ")" 
                : "";
            ClientUtility.sendMessage("§a." + command.getName() + aliases + " §7- §f" + command.getDescription());
        }
        
        ClientUtility.sendMessage("");
        ClientUtility.sendMessage("§7§m                                    ");
    }
}
