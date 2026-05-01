package itz.silentcore.feature.command.impl;

import itz.silentcore.SilentCore;
import itz.silentcore.feature.command.Command;
import itz.silentcore.utils.client.ClientUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FriendCommand extends Command {
    public FriendCommand() {
        super("friend", "Управление друзьями", ".friend <add/save/remove/list> <ник>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            ClientUtility.sendMessage("§fИспользование: .friend <add/save/remove/list> <ник>");
            return;
        }

        String cmd = args[0].toLowerCase();
        switch (cmd) {
            case "add", "save" -> {
                if (args.length < 2) {
                    ClientUtility.sendMessage("§cИспользование: .friend " + cmd + " <ник>");
                    return;
                }
                SilentCore.getInstance().getFriendManager().addFriend(args[1]);
            }
            case "remove", "delete" -> {
                if (args.length < 2) {
                    ClientUtility.sendMessage("§cИспользование: .friend remove <ник>");
                    return;
                }
                SilentCore.getInstance().getFriendManager().removeFriend(args[1]);
            }
            case "list" -> SilentCore.getInstance().getFriendManager().listFriends();
            default -> ClientUtility.sendMessage("§cКоманда: add, save, remove, list");
        }
    }

    @Override
    public List<String> getSuggestions(String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            String p = args[0].toLowerCase();
            Stream.of("add", "save", "remove", "delete", "list")
                    .filter(s -> s.startsWith(p))
                    .forEach(list::add);
        }
        return list;
    }
}