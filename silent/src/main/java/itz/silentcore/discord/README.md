# Discord RPC Implementation

Полная реализация Discord Rich Presence для Fabric 1.21.4.

## Структура

- `DiscordManager.java` - Основной класс для управления Discord RPC
- `DiscordIntegration.java` - Утилита для удобного использования
- `DiscordExample.java` - Примеры использования
- `utils/` - Вспомогательные классы:
  - `DiscordRPC.java` - JNA интерфейс к нативной библиотеке
  - `DiscordRichPresence.java` - Структура Rich Presence с Builder
  - `DiscordEventHandlers.java` - Обработчики событий Discord
  - `DiscordUser.java` - Информация о пользователе Discord
  - `RPCButton.java` - Кнопки для Rich Presence
  - `DiscordNativeLoader.java` - Загрузчик нативных библиотек
- `callbacks/` - Callback интерфейсы для обработки событий

## Использование

### Простой пример

```java
DiscordIntegration.initialize();
DiscordIntegration.updateStatus("Exploring", "In Game");
DiscordIntegration.shutdown();
```

### С кнопками

```java
RPCButton btn1 = RPCButton.create("Telegram", "t.me/example");
RPCButton btn2 = RPCButton.create("Discord", "https://discord.gg/example");
DiscordIntegration.updateStatusWithButtons("Playing", "Minecraft 1.21.4", btn1, btn2);
```

### Полный контроль

```java
DiscordManager manager = new DiscordManager();
manager.init("YOUR_APP_ID");

DiscordRichPresence presence = new DiscordRichPresence.Builder()
    .setDetails("Role: Admin")
    .setState("In Lobby")
    .setStartTimestamp(System.currentTimeMillis() / 1000)
    .setLargeImage("https://example.com/image.png", "silentcore Client")
    .setButtons(RPCButton.create("Visit", "example.com"))
    .build();

manager.updatePresence(presence);
```

## Конфигурация

Замените `YOUR_APP_ID_HERE` в `DiscordIntegration.java` на ID вашего Discord приложения.

## Нативные библиотеки

- Windows 32-bit: `src/main/resources/win32-x86/discord-rpc.dll`
- Windows 64-bit: `src/main/resources/win32-x86-64/discord-rpc.dll`

Библиотеки автоматически загружаются и извлекаются из ресурсов при запуске.

## Зависимости

- JNA 5.15.0 (уже добавлена в build.gradle)
- Minecraft 1.21.4
- Fabric Loader
- Fabric API

## Events

Поддерживаемые события Discord:
- Ready - когда Discord RPC инициализирован
- Disconnected - при отключении
- Errored - при ошибке
- JoinGame - при присоединении к игре (через кнопку)
- SpectateGame - при спектировании
- JoinRequest - при запросе присоединения
