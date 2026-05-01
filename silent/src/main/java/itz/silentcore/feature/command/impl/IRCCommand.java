package itz.silentcore.feature.command.impl;

import itz.silentcore.SilentCore;
import itz.silentcore.feature.command.Command;
import itz.silentcore.utils.client.ClientUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class IRCCommand extends Command {

    private static final ScheduledExecutorService DELAY_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private static final AtomicBoolean CAN_SEND = new AtomicBoolean(true);
    private static final Pattern LINK_PATTERN = Pattern.compile(
            "(?i)\\b((?:https?://|www\\.)?[a-z0-9-]+(?:\\.[a-z0-9-]+)*\\.[a-z]{2,}(?::\\d{2,5})?(?:/[^\\s]*)?)"
    );
    private static final Pattern SPAM_PATTERN = Pattern.compile(
            "(?i)\\b(\\w+)\\1{2,}\\b"
    );

    public IRCCommand() {
        super("irc", "Отправить сообщение в IRC чат", "@irc");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            ClientUtility.sendMessage("Использование: .irc <сообщение> или @irc <сообщение>");
            return;
        }

        String message = String.join(" ", args);

        if (!CAN_SEND.compareAndSet(true, false)) {
            ClientUtility.sendMessage("Подождите 2 секунды перед отправкой следующего сообщения");
            return;
        }

        if (LINK_PATTERN.matcher(message).find()) {
            ClientUtility.sendMessage("Запрещено отправлять ссылки в IRC");
            resetCooldown();
            return;
        }

        if (SPAM_PATTERN.matcher(message).find()) {
            ClientUtility.sendMessage("Спам запрещён");
            resetCooldown();
            return;
        }

        if (message.length() > 200) {
            ClientUtility.sendMessage("Сообщение слишком длинное (макс. 200 символов)");
            resetCooldown();
            return;
        }

        SilentCore.getInstance().ircManager.sendIRCMessage(message);

        DELAY_EXECUTOR.schedule(() -> CAN_SEND.set(true), 2, TimeUnit.SECONDS);
    }

    private void resetCooldown() {
        DELAY_EXECUTOR.schedule(() -> CAN_SEND.set(true), 2, TimeUnit.SECONDS);
    }

    @Override
    public List<String> getSuggestions(String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 0 || (args.length == 1 && args[0].isEmpty())) {
            suggestions.add("<сообщение>");
        }

        return suggestions;
    }
}