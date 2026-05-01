# Гайд по разработке Minecraft чита на Fabric 1.21.4

> Этот гайд написан на основе реального клиента **SilentCore** из папки `silent/`.
> Изучай его код параллельно с чтением гайда.

---

## Содержание

1. [Структура проекта](#1-структура-проекта)
2. [Как работает Fabric мод](#2-как-работает-fabric-мод)
3. [Система событий (EventBus)](#3-система-событий-eventbus)
4. [Миксины — внедрение в код Minecraft](#4-миксины--внедрение-в-код-minecraft)
5. [Создание модуля](#5-создание-модуля)
6. [Настройки модуля (Settings)](#6-настройки-модуля-settings)
7. [Рендер 2D](#7-рендер-2d)
8. [Рендер 3D](#8-рендер-3d)
9. [Примеры реальных модулей](#9-примеры-реальных-модулей)
10. [Полезные классы Minecraft](#10-полезные-классы-minecraft)

---

## 1. Структура проекта

```
src/
└── main/
    ├── java/com/example/client/
    │   ├── ExampleClient.java          ← Точка входа мода
    │   ├── manager/
    │   │   └── ModuleManager.java      ← Управление модулями
    │   ├── feature/
    │   │   ├── event/                  ← Система событий
    │   │   └── module/
    │   │       ├── api/                ← Базовые классы
    │   │       └── impl/               ← Реальные модули
    │   │           ├── combat/
    │   │           ├── movement/
    │   │           ├── render/
    │   │           └── misc/
    │   ├── mixin/                      ← Миксины (внедрение в MC)
    │   └── utils/
    │       ├── render/                 ← Утилиты рендера
    │       └── client/                 ← Утилиты клиента
    └── resources/
        ├── fabric.mod.json             ← Конфиг мода
        └── minecraftclient.mixins.json ← Список миксинов
```

**Аналог в SilentCore:** [`silent/src/main/java/itz/silentcore/`](silent/src/main/java/itz/silentcore/)

---

## 2. Как работает Fabric мод

### Точка входа

Каждый мод начинается с класса, реализующего `ModInitializer`.
В SilentCore это [`SilentCore.java`](silent/src/main/java/itz/silentcore/SilentCore.java):

```java
public class SilentCore implements ModInitializer {
    public static SilentCore instance;
    public ModuleManager moduleManager;
    public EventBus eventBus;

    @Override
    public void onInitialize() {
        instance = this;
        eventBus = new EventBus();           // Создаём шину событий
        moduleManager = new ModuleManager(); // Загружаем все модули
    }
}
```

Метод `onInitialize()` вызывается один раз при запуске игры.
Здесь нужно инициализировать все менеджеры.

### Доступ к Minecraft

Чтобы не писать `MinecraftClient.getInstance()` везде, в SilentCore есть интерфейс
[`IMinecraft.java`](silent/src/main/java/itz/silentcore/utils/client/IMinecraft.java):

```java
public interface IMinecraft {
    MinecraftClient mc = MinecraftClient.getInstance();
}
```

Любой класс, реализующий этот интерфейс, получает доступ к `mc` напрямую:

```java
public class MyModule extends Module implements IMinecraft {
    public void doSomething() {
        mc.player.setSprinting(true); // Прямой доступ
    }
}
```

---

## 3. Система событий (EventBus)

SilentCore использует **Google Guava EventBus** для передачи событий между миксинами и модулями.

### Как это работает

```
Minecraft tick()
    └── MinecraftClientMixin.onTick()   ← Миксин перехватывает
            └── new TickEvent().hook()  ← Создаёт событие
                    └── eventBus.post() ← Рассылает всем подписчикам
                            └── Module.onTick() ← Модуль получает
```

### Базовый класс события

[`Event.java`](silent/src/main/java/itz/silentcore/feature/event/Event.java):

```java
public class Event {
    private boolean cancelled = false;
    private boolean pre; // true = до действия, false = после

    public void hook() {
        SilentCore.getInstance().eventBus.post(this); // Отправить событие
    }

    public void cancel() {
        cancelled = true; // Отменить действие в игре
    }
}
```

### Создание своего события

```java
// 1. Создай класс события
public class TickEvent extends Event {
    public TickEvent(boolean pre) {
        super(pre);
    }
}

// 2. В миксине — отправь событие
@Inject(method = "tick", at = @At("HEAD"))
public void onTick(CallbackInfo ci) {
    new TickEvent(true).hook(); // Отправляет всем подписчикам
}

// 3. В модуле — подпишись на событие
@Subscribe
public void onTick(TickEvent event) {
    // Этот метод вызовется каждый тик
    if (mc.player != null) {
        mc.player.setSprinting(true);
    }
}
```

### Подписка модуля на события

Модуль подписывается на EventBus только когда **включён**:

```java
// В базовом классе Module.java
public void enable() {
    SilentCore.getInstance().eventBus.register(this); // Подписаться
}

public void disable() {
    SilentCore.getInstance().eventBus.unregister(this); // Отписаться
}
```

Это значит: если модуль выключен — его методы `@Subscribe` не вызываются.

### Список основных событий в SilentCore

| Событие | Когда вызывается | Файл |
|---------|-----------------|------|
| `TickEvent` | Каждый игровой тик | [`TickEvent.java`](silent/src/main/java/itz/silentcore/feature/event/impl/TickEvent.java) |
| `WorldRenderEvent` | При рендере мира (3D) | [`WorldRenderEvent.java`](silent/src/main/java/itz/silentcore/feature/event/impl/WorldRenderEvent.java) |
| `Render2DEvent` | При рендере HUD (2D) | [`Render2DEvent.java`](silent/src/main/java/itz/silentcore/feature/event/impl/Render2DEvent.java) |
| `KeyEvent` | При нажатии клавиши | [`KeyEvent.java`](silent/src/main/java/itz/silentcore/feature/event/impl/KeyEvent.java) |
| `PacketEvent` | При получении пакета | [`PacketEvent.java`](silent/src/main/java/itz/silentcore/feature/event/impl/PacketEvent.java) |

---

## 4. Миксины — внедрение в код Minecraft

Миксины — это главный инструмент для модификации Minecraft без изменения его исходников.

### Как работает миксин

```java
@Mixin(MinecraftClient.class)  // Указываем класс Minecraft который модифицируем
public class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("HEAD")) // Внедряемся в начало метода tick()
    public void onTick(CallbackInfo ci) {
        // Наш код выполнится ПЕРЕД оригинальным tick()
        new TickEvent(true).hook();
    }
}
```

### Аннотации @At — куда внедряться

| Значение | Описание |
|----------|----------|
| `@At("HEAD")` | В самом начале метода |
| `@At("TAIL")` | В самом конце метода |
| `@At("RETURN")` | Перед каждым `return` |
| `@At(value = "INVOKE", target = "...")` | Перед вызовом конкретного метода |

### Отмена оригинального метода

```java
@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
public void onTick(CallbackInfo ci) {
    ci.cancel(); // Оригинальный tick() НЕ выполнится
}
```

### Изменение возвращаемого значения

```java
@Inject(method = "getWindowTitle", at = @At("HEAD"), cancellable = true)
private void changeTitle(CallbackInfoReturnable<String> cir) {
    cir.setReturnValue("My Custom Title"); // Возвращаем своё значение
}
```

### Доступ к приватным полям через @Shadow

```java
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow  // Получаем доступ к приватному полю
    private float zoom;

    @Inject(method = "renderWorld", at = @At("HEAD"))
    public void onRender(CallbackInfo ci) {
        System.out.println("Zoom: " + zoom); // Теперь можем читать zoom
    }
}
```

### Регистрация миксина

Каждый миксин нужно добавить в файл [`minecraftclient.mixins.json`](src/main/resources/minecraftclient.mixins.json):

```json
{
    "client": [
        "ExampleMixin",
        "MinecraftClientMixin",
        "GameRendererMixin"
    ]
}
```

### Реальные примеры миксинов из SilentCore

| Миксин | Что делает |
|--------|-----------|
| [`MinecraftClientMixin`](silent/src/main/java/itz/silentcore/mixin/MinecraftClientMixin.java) | Перехватывает tick, меняет заголовок окна |
| [`GameRendererMixin`](silent/src/main/java/itz/silentcore/mixin/GameRendererMixin.java) | Перехватывает рендер мира, отправляет WorldRenderEvent |
| [`EntityMixin`](silent/src/main/java/itz/silentcore/mixin/EntityMixin.java) | Модифицирует поведение сущностей |
| [`LivingEntityMixin`](silent/src/main/java/itz/silentcore/mixin/LivingEntityMixin.java) | Модифицирует живые существа |
| [`ClientPlayerEntityMixin`](silent/src/main/java/itz/silentcore/mixin/ClientPlayerEntityMixin.java) | Модифицирует игрока |
| [`InGameHudMixin`](silent/src/main/java/itz/silentcore/mixin/InGameHudMixin.java) | Перехватывает рендер HUD |
| [`MouseMixin`](silent/src/main/java/itz/silentcore/mixin/MouseMixin.java) | Перехватывает движение мыши |
| [`KeyboardMixin`](silent/src/main/java/itz/silentcore/mixin/KeyboardMixin.java) | Перехватывает нажатия клавиш |

---

## 5. Создание модуля

### Базовый класс Module

Все модули наследуются от [`Module.java`](silent/src/main/java/itz/silentcore/feature/module/api/Module.java).

Аннотация `@ModuleAnnotation` обязательна — по ней ModuleManager находит и регистрирует модуль автоматически:

```java
@ModuleAnnotation(
    name = "AutoSprint",        // Имя модуля
    category = Category.MOVEMENT, // Категория
    description = "Авто спринт" // Описание
)
public class AutoSprint extends Module {

    // Вызывается когда модуль включается
    @Override
    public void onEnable() {
        System.out.println("AutoSprint включён!");
    }

    // Вызывается когда модуль выключается
    @Override
    public void onDisable() {
        System.out.println("AutoSprint выключен!");
    }

    // Подписка на тик — работает только пока модуль включён
    @Subscribe
    public void onTick(TickEvent event) {
        if (mc.player == null) return;
        if (mc.player.forwardSpeed > 0) {
            mc.player.setSprinting(true);
        }
    }
}
```

### Категории модулей

[`Category.java`](silent/src/main/java/itz/silentcore/feature/module/api/Category.java):

```java
public enum Category {
    COMBAT,    // Боевые модули (KillAura, Aura, TriggerBot)
    MOVEMENT,  // Движение (AutoSprint, Speed, Fly)
    PLAYER,    // Игрок (NoFall, FastEat)
    RENDER,    // Визуал (ESP, Tracers, HUD)
    MISC       // Разное (DiscordRPC, AutoRespawn)
}
```

### Как ModuleManager находит модули

[`ModuleManager.java`](silent/src/main/java/itz/silentcore/manager/ModuleManager.java) сканирует пакет `feature.module.impl` и автоматически регистрирует все классы с `@ModuleAnnotation`:

```java
// Тебе не нужно вручную регистрировать модули!
// Просто создай класс в правильном пакете с аннотацией — он подхватится сам.
```

---

## 6. Настройки модуля (Settings)

Настройки позволяют менять поведение модуля через GUI без перекомпиляции.

### BooleanSetting — вкл/выкл

```java
private final BooleanSetting onlyOnGround = new BooleanSetting("Only On Ground", true);

@Subscribe
public void onTick(TickEvent event) {
    if (onlyOnGround.isEnabled() && !mc.player.isOnGround()) return;
    // логика
}
```

### NumberSetting — число с ползунком

```java
// Название, значение по умолчанию, минимум, максимум, шаг
private final NumberSetting range = new NumberSetting("Range", 3.0f, 1.0f, 6.0f, 0.1f);

@Subscribe
public void onTick(TickEvent event) {
    float attackRange = range.getCurrent(); // Получить текущее значение
}
```

### ModeSetting — выбор режима

```java
private final ModeSetting mode = new ModeSetting("Mode", "Normal", "Fast", "Slow");

@Subscribe
public void onTick(TickEvent event) {
    switch (mode.get()) {
        case "Normal" -> doNormal();
        case "Fast"   -> doFast();
        case "Slow"   -> doSlow();
    }
}
```

### MultiBooleanSetting — несколько чекбоксов

```java
// Используется в ESP для выбора типов сущностей
private final MultiBooleanSetting entityType = new MultiBooleanSetting("Entity Type",
    new MultiBooleanSetting.Value("Player", true),  // включён по умолчанию
    new MultiBooleanSetting.Value("Mob", false),
    new MultiBooleanSetting.Value("Item", true)
);

@Subscribe
public void onTick(TickEvent event) {
    if (entityType.isEnable("Player")) {
        // рендерить игроков
    }
}
```

---

## 7. Рендер 2D

2D рендер используется для HUD — текст, прямоугольники, иконки поверх экрана.

### Получение события

Событие `Render2DEvent` отправляется из миксина [`InGameHudMixin`](silent/src/main/java/itz/silentcore/mixin/InGameHudMixin.java):

```java
@Subscribe
public void onRender2D(Render2DEvent event) {
    DrawContext context = event.getContext().getContext();
    int screenWidth = mc.getWindow().getScaledWidth();
    int screenHeight = mc.getWindow().getScaledHeight();

    // Нарисовать прямоугольник
    context.fill(10, 10, 110, 30, 0x80000000); // x1, y1, x2, y2, цвет ARGB

    // Нарисовать текст
    context.drawText(mc.textRenderer, "Hello World", 15, 15, 0xFFFFFFFF, true);
}
```

### Формат цвета ARGB

```java
// 0xAARRGGBB
0xFFFFFFFF  // Белый, непрозрачный
0x80FF0000  // Красный, полупрозрачный (alpha = 0x80 = 128)
0xFF00FF00  // Зелёный
0xFF0000FF  // Синий
0x00000000  // Полностью прозрачный
```

### Перевод 3D координат в 2D (для ESP текста)

```java
// Получить позицию игрока на экране
Vec3d worldPos = new Vec3d(player.getX(), player.getY() + player.getHeight() + 0.3, player.getZ());
Vec3d screenPos = Projection.worldToScreen(worldPos);

if (screenPos.z > 0 && screenPos.z < 1) { // Если на экране
    int x = (int) screenPos.x;
    int y = (int) screenPos.y;
    context.drawText(mc.textRenderer, player.getName().getString(), x, y, 0xFFFFFFFF, true);
}
```

---

## 8. Рендер 3D

3D рендер используется для ESP боксов, линий, трейсеров — всего что рисуется в мире.

### Как работает Render3D

[`Render3D.java`](silent/src/main/java/itz/silentcore/utils/render/Render3D.java) работает через очередь:
1. В `WorldRenderEvent` ты добавляешь объекты в список (`LINE`, `QUAD`, `TEXTURE`)
2. В конце кадра `onWorldRender()` рисует всё из списка и очищает его

### Нарисовать бокс вокруг сущности

```java
@Subscribe
public void onWorldRender(WorldRenderEvent event) {
    if (mc.world == null) return;

    for (PlayerEntity player : mc.world.getPlayers()) {
        if (player == mc.player) continue;

        Box box = player.getBoundingBox(); // Хитбокс игрока

        // Цвет: 0xAARRGGBB
        int fillColor  = 0x3300FF00; // Зелёный, прозрачный (заливка)
        int lineColor  = 0xFF00FF00; // Зелёный, непрозрачный (контур)

        // drawBox(box, цвет, толщина линии, рисовать линии, рисовать заливку, depth test)
        Render3D.drawBox(box, fillColor, 1.5f, true, true, false);
        Render3D.drawBox(box, lineColor, 1.5f, true, false, false);
    }
}
```

### Нарисовать линию (трейсер)

```java
@Subscribe
public void onWorldRender(WorldRenderEvent event) {
    for (PlayerEntity player : mc.world.getPlayers()) {
        if (player == mc.player) continue;

        Vec3d from = mc.player.getEyePos();           // От глаз игрока
        Vec3d to   = player.getPos().add(0, 1, 0);    // До цели

        // drawLine(от, до, цвет, толщина, depth)
        Render3D.drawLine(from, to, 0xFF00FFFF, 1.5f, false);
    }
}
```

### depth параметр

```java
Render3D.drawBox(box, color, 1f, true, true, false); // false = видно сквозь стены
Render3D.drawBox(box, color, 1f, true, true, true);  // true  = скрыто за стенами
```

---

## 9. Примеры реальных модулей

### AutoSprint (простейший модуль)

**Файл:** [`silent/.../movement/AutoSprint.java`](silent/src/main/java/itz/silentcore/feature/module/impl/movement/AutoSprint.java)

```java
@ModuleAnnotation(name = "AutoSprint", category = Category.MOVEMENT)
public class AutoSprint extends Module {

    @Subscribe
    public void onTick(TickEvent event) {
        if (mc.player == null) return;
        // Если идём вперёд — спринтуем
        if (mc.player.forwardSpeed > 0 && !mc.player.isSneaking()) {
            mc.player.setSprinting(true);
        }
    }
}
```

---

### ESP (рендер игроков)

**Файл:** [`silent/.../render/ESP.java`](silent/src/main/java/itz/silentcore/feature/module/impl/render/ESP.java)

Ключевые части:
1. В `onTick` — собираем список игроков
2. В `onWorldRender` — рисуем 3D боксы
3. В `onRender2D` — рисуем имена и здоровье

```java
@ModuleAnnotation(name = "ESP", category = Category.RENDER)
public class ESP extends Module {

    private final List<PlayerEntity> players = new ArrayList<>();

    @Subscribe
    public void onTick(TickEvent e) {
        players.clear();
        if (mc.world != null) {
            mc.world.getPlayers().stream()
                .filter(p -> p != mc.player)
                .forEach(players::add);
        }
    }

    @Subscribe
    public void onWorldRender(WorldRenderEvent e) {
        for (PlayerEntity player : players) {
            Box box = player.getBoundingBox();
            Render3D.drawBox(box, 0x3300FF00, 1.5f, true, true, false);
            Render3D.drawBox(box, 0xFF00FF00, 1.5f, true, false, false);
        }
    }
}
```

---

### Aura (боевой модуль)

**Файл:** [`silent/.../combat/Aura.java`](silent/src/main/java/itz/silentcore/feature/module/impl/combat/Aura.java)

Принцип работы:
1. В `onTick(pre=true)` — ищем цель, поворачиваем камеру
2. В `onTick(pre=false)` — атакуем цель

```java
@Subscribe
public void onTick(TickEvent event) {
    if (event.isPre()) {
        // Найти ближайшего игрока
        target = mc.world.getPlayers().stream()
            .filter(p -> p != mc.player)
            .min(Comparator.comparingDouble(p -> mc.player.distanceTo(p)))
            .orElse(null);
    } else {
        // Атаковать
        if (target != null && mc.player.distanceTo(target) <= 3.0f) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
```

---

## 10. Полезные классы Minecraft

### Доступ к основным объектам

```java
MinecraftClient mc = MinecraftClient.getInstance();

mc.player          // Твой игрок (ClientPlayerEntity)
mc.world           // Текущий мир (ClientWorld)
mc.interactionManager  // Взаимодействие с миром
mc.gameRenderer    // Рендерер игры
mc.textRenderer    // Рендерер текста
mc.getWindow()     // Окно игры
```

### Игрок (mc.player)

```java
mc.player.getX(), getY(), getZ()     // Позиция
mc.player.getEyePos()                // Позиция глаз
mc.player.getHealth()                // Здоровье (0-20)
mc.player.getYaw(), getPitch()       // Угол камеры
mc.player.setSprinting(true)         // Включить спринт
mc.player.isSneaking()               // Крадётся ли
mc.player.isOnGround()               // На земле ли
mc.player.getVelocity()              // Скорость (Vec3d)
mc.player.getBoundingBox()           // Хитбокс
mc.player.distanceTo(entity)         // Дистанция до сущности
mc.player.hasStatusEffect(effect)    // Есть ли эффект
```

### Мир (mc.world)

```java
mc.world.getPlayers()                // Список всех игроков
mc.world.getEntities()               // Список всех сущностей
mc.world.getBlockState(pos)          // Блок на позиции
mc.world.isAir(pos)                  // Воздух ли блок
mc.world.getTime()                   // Время в тиках
```

### Взаимодействие

```java
// Атаковать сущность
mc.interactionManager.attackEntity(mc.player, target);
mc.player.swingHand(Hand.MAIN_HAND); // Анимация удара

// Использовать предмет
mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

// Кликнуть по блоку
mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHitResult);
```

### Пакеты

```java
// Отправить пакет на сервер
mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
    x, y, z, yaw, pitch, onGround
));
```

---

## Быстрый старт — создай свой первый модуль

1. Создай файл в `src/main/java/com/example/client/feature/module/impl/movement/`
2. Назови его `MyModule.java`
3. Вставь этот код:

```java
package com.example.client.feature.module.impl.movement;

import com.google.common.eventbus.Subscribe;
import com.example.client.feature.event.impl.TickEvent;
import com.example.client.feature.module.api.Category;
import com.example.client.feature.module.api.Module;
import com.example.client.feature.module.api.ModuleAnnotation;

@ModuleAnnotation(name = "MyModule", category = Category.MOVEMENT, description = "Мой первый модуль")
public class MyModule extends Module {

    @Subscribe
    public void onTick(TickEvent event) {
        if (mc.player == null) return;
        // Твой код здесь
        mc.player.setSprinting(true);
    }
}
```

4. Запусти `./gradlew runClient` — модуль появится в GUI автоматически!

---

## Команды Gradle

```bash
./gradlew runClient    # Запустить Minecraft с модом
./gradlew build        # Собрать JAR файл
./gradlew genSources   # Декомпилировать исходники Minecraft
./gradlew --stop       # Остановить Gradle daemon
```
