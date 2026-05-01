package itz.silentcore.feature.ui.screen.mainmenu;

import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.RenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ru.kotopushka.compiler.sdk.classes.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static itz.silentcore.utils.render.Fonts.icons;
import static itz.silentcore.utils.render.Fonts.sf_pro;

public class AltManager extends Screen implements IMinecraft {

    private final Identifier backTexture = Identifier.of("silentcore", "textures/background.png");
    private final List<AltEntry> altList = new ArrayList<>();
    private String selectedAlt = Profile.getUsername();
    private String inputText = "";
    private boolean inputActive = false;
    private final Screen previousScreen;

    // Скролл
    private float scrollOffset = 0f;
    private float targetScrollOffset = 0f;
    private long lastScrollTime = System.currentTimeMillis();
    private final Random random = new Random();

    // Кнопки
    private Button inputFieldButton;
    private Button addButton;
    private Button randomButton;
    private Button backButton;

    public AltManager(Screen previousScreen) {
        super(Text.literal("Alt Manager"));
        this.previousScreen = previousScreen;

        // Добавляем текущий ник
        addAltEntry(Profile.getUsername(), true);
        selectedAlt = Profile.getUsername();
    }

    private void addAltEntry(String name, boolean loadSkin) {
        if (altList.stream().noneMatch(e -> e.name.equals(name))) {
            AltEntry entry = new AltEntry(name);
            altList.add(entry);
            if (loadSkin) {
               // loadSkinsilentcore(entry);
            }
        }
    }

    private void loadSkinsilentcore(AltEntry entry) {

        /*
        CompletableFuture.runsilentcore(() -> {
            try {
                // Загружаем скин через Minecraft Session Service (как в игре)
                SkinTextures textures = mc.getSkinProvider().fetchSkinTextures(entry.name);
                if (textures != null && textures.texture() != null) {
                    NativeImage image = NativeImage.read(textures.texture().getInputStream());
                    NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
                    Identifier id = Identifier.of("altmanager", entry.name.toLowerCase());
                    mc.getTextureManager().registerTexture(id, texture);
                    entry.skinId = id;
                }
            } catch (Exception ignored) {}
        });

         */
    }

    @Override
    protected void init() {
        updateButtons();
    }

    @Override
    public void tick() {
        updateButtons();
        updateScroll();
    }

    private void updateScroll() {
        long currentTime = System.currentTimeMillis();
        float delta = (currentTime - lastScrollTime) / 1000f;
        lastScrollTime = currentTime;

        if (Math.abs(targetScrollOffset - scrollOffset) > 0.5f) {
            scrollOffset += (targetScrollOffset - scrollOffset) * Math.min(delta * 20f, 1f);
        } else {
            scrollOffset = targetScrollOffset;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        var renderContext = new RenderContext(context);
        var backgroundTexture = MinecraftClient.getInstance().getTextureManager().getTexture(backTexture);

        // Фон
        renderContext.drawTexture(
                0, 0,
                mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(),
                0, ColorRGBA.of(255, 255, 255), 0, 0, 1, 1,
                backgroundTexture.getGlId()
        );

        int centerX = mc.getWindow().getScaledWidth() / 2;
        int centerY = mc.getWindow().getScaledHeight() / 2;

        // Логотип (иконка)
        renderContext.drawText("i", icons, centerX - icons.getWidth("i", 50f) / 2, centerY - 180, 50f, ColorRGBA.of(164, 143, 255));

        // Заголовок
        renderContext.drawText("AltManager", sf_pro, centerX - sf_pro.getWidth("AltManager", 22f) / 2, centerY - 120, 22f, ColorRGBA.of(255, 255, 255));

        // Поле ввода
        inputFieldButton.render(renderContext, mouseX, mouseY);

        // Кнопки управления
        addButton.render(renderContext, mouseX, mouseY);
        randomButton.render(renderContext, mouseX, mouseY);
        backButton.render(renderContext, mouseX, mouseY);

        // Список алтов
        renderAltList(renderContext, centerX, centerY - 30);
    }

    private void renderAltList(RenderContext context, int centerX, int centerY) {
        int listX = centerX - 140;
        int listY = centerY;
        int itemWidth = 280;
        int itemHeight = 40;
        int maxVisible = 6;
        int totalHeight = itemHeight * altList.size();

        float maxScroll = Math.max(0, totalHeight - (itemHeight * maxVisible));
        targetScrollOffset = Math.max(0, Math.min(targetScrollOffset, maxScroll));

        int startY = (int) (listY - scrollOffset);

        // Фон списка
        context.drawBlur(listX, listY - 10, itemWidth, itemHeight * maxVisible + 20, 12f, 16, ColorRGBA.of(255, 255, 255, 255));
        context.drawRect(listX, listY - 10, itemWidth, itemHeight * maxVisible + 20, 12f, ColorRGBA.of(30, 30, 40, 200));
        context.drawBorder(listX, listY - 10, itemWidth, itemHeight * maxVisible + 20, 12f, 0.5f, ColorRGBA.of(100, 100, 150, 180), 0.6f, 0.6f);

        for (int i = 0; i < altList.size(); i++) {
            AltEntry entry = altList.get(i);
            int y = startY + (i * itemHeight);

            if (y + itemHeight < listY - 10 || y > listY + itemHeight * maxVisible + 10) continue;

            boolean isSelected = entry.name.equals(selectedAlt);
            boolean hovered = isHovered(listX, y, itemWidth, itemHeight);

            ColorRGBA bgColor = isSelected ? ColorRGBA.of(96, 67, 222, 180) :
                    hovered ? ColorRGBA.of(50, 50, 70, 200) : ColorRGBA.of(40, 40, 50, 180);

            context.drawRect(listX, y, itemWidth, itemHeight, 10f, bgColor);
            context.drawBorder(listX, y, itemWidth, itemHeight, 10f, 0.4f, ColorRGBA.of(120, 120, 180, 150), 0.5f, 0.5f);

            // Скин
            // if (entry.skinId != null) {
            //     context.drawTexture(entry.skinId, listX + 8, y + 8, 24, 24, 8, 8, 8, 8, 64, 64);
            //     context.drawTexture(entry.skinId, listX + 8, y + 8, 24, 24, 40, 8, 8, 8, 64, 64); // Шляпа
            // } else {
                // Заглушка
                context.drawRect(listX + 8, y + 8, 24, 24, 0, ColorRGBA.of(80, 80, 80, 255));
            // }

            // Ник
            context.drawText(entry.name, sf_pro, listX + 40, y + itemHeight / 2 - 6f, 12f,
                    isSelected ? ColorRGBA.of(255, 255, 255) : ColorRGBA.of(220, 220, 255));

            // Крестик удаления
            context.drawText("x", icons, listX + itemWidth - 25, y + itemHeight / 2 - 6f, 14f,
                    ColorRGBA.of(200, 100, 100));
        }
    }

    private void updateButtons() {
        int centerX = mc.getWindow().getScaledWidth() / 2;
        int centerY = mc.getWindow().getScaledHeight() / 2;
        int width = 280;
        int height = 36;

        inputFieldButton = new Button(centerX - width / 2, centerY - 80, width, height, "Type nickname here...", () -> {
            inputActive = true;
            return null;
        });

        int btnWidth = 80;
        addButton = new Button(centerX - btnWidth - 5, centerY - 30, btnWidth, height, "Add", () -> {
            addAlt();
            return null;
        });

        randomButton = new Button(centerX + 5, centerY - 30, btnWidth, height, "Random", () -> {
            generateRandomNickname();
            return null;
        });

        backButton = new Button(centerX - btnWidth / 2, centerY + 140, btnWidth * 2 + 10, height, "Back", () -> {
            mc.setScreen(previousScreen);
            return null;
        });
    }

    class Button {
        int x, y, width, height;
        final String text;
        final java.util.function.Supplier<Void> action;

        public Button(int x, int y, int width, int height, String text, java.util.function.Supplier<Void> action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
            this.action = action;
        }

        public void render(RenderContext context, int mouseX, int mouseY) {
            boolean hovered = isHovered(x, y, width, height);

            context.drawBlur(x, y, width, height, 10f, 14, ColorRGBA.of(255, 255, 255, 255));
            context.drawRect(x, y, width, height, 10f, ColorRGBA.of(40, 40, 50, 200));

            String displayText = this == inputFieldButton ? (inputText.isEmpty() ? text : inputText) : text;
            if (displayText.length() > 30) displayText = displayText.substring(0, 30);

            ColorRGBA textColor = this == inputFieldButton && inputText.isEmpty() ?
                    ColorRGBA.of(150, 150, 180) : ColorRGBA.of(220, 220, 255);

            context.drawText(displayText, sf_pro, x + 15, y + height / 2 - 6f, 12f, textColor);

            // Иконки
            String icon = getIconSymbol(text);
            if (this == inputFieldButton) icon = "H";
            context.drawText(icon, icons, x + width - 28, y + height / 2 - 6f, 14f, ColorRGBA.of(180, 180, 220));

            context.drawBorder(x, y, width, height, 10f, 0.5f, ColorRGBA.of(100, 100, 150, 180), 0.6f, 0.6f);

            // Курсор
            if (this == inputFieldButton && inputActive && (System.currentTimeMillis() / 500) % 2 == 0) {
                float cursorX = x + 15 + sf_pro.getWidth(displayText, 12f);
                context.drawRect(cursorX, y + 10, 1, height - 20, 0, ColorRGBA.of(255, 255, 255));
            }
        }

        public void onClick(int mouseX, int mouseY) {
            if (isHovered(x, y, width, height)) {
                action.get();
            }
        }
    }

    private String getIconSymbol(String text) {
        return switch (text) {
            case "Add" -> "+";
            case "Random" -> "N";
            case "Back" -> "F";
            case "Type nickname here..." -> "H";
            default -> "";
        };
    }

    private boolean isHovered(int x, int y, int width, int height) {
        double mouseX = mc.mouse.getX() * (double) mc.getWindow().getScaledWidth() / (double) mc.getWindow().getWidth();
        double mouseY = mc.mouse.getY() * (double) mc.getWindow().getScaledHeight() / (double) mc.getWindow().getHeight();
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        inputFieldButton.onClick((int) mouseX, (int) mouseY);
        addButton.onClick((int) mouseX, (int) mouseY);
        randomButton.onClick((int) mouseX, (int) mouseY);
        backButton.onClick((int) mouseX, (int) mouseY);

        // Клик по алту
        int centerX = mc.getWindow().getScaledWidth() / 2;
        int centerY = mc.getWindow().getScaledHeight() / 2;
        int listX = centerX - 140;
        int listY = centerY - 30;
        int itemHeight = 40;
        int startY = (int) (listY - scrollOffset);

        for (int i = 0; i < altList.size(); i++) {
            int y = startY + (i * itemHeight);
            if (isHovered(listX, y, 280, itemHeight)) {
                if (mouseX >= listX + 255) {
                    // Удаление
                    altList.remove(i);
                    if (altList.isEmpty()) {
                        selectedAlt = Profile.getUsername();
                        addAltEntry(selectedAlt, true);
                    } else if (selectedAlt.equals(altList.get(i).name)) {
                        selectedAlt = altList.get(0).name;
                    }
                } else {
                    selectedAlt = altList.get(i).name;
                }
                break;
            }
        }

        if (!isHovered(inputFieldButton.x, inputFieldButton.y, inputFieldButton.width, inputFieldButton.height)) {
            inputActive = false;
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        targetScrollOffset -= verticalAmount * 25;
        return true;
    }

    private void addAlt() {
        String trimmed = inputText.trim();
        if (trimmed.length() >= 3 && trimmed.length() <= 16 && altList.stream().noneMatch(e -> e.name.equals(trimmed))) {
            addAltEntry(trimmed, true);
            inputText = "";
            inputActive = false;
        }
    }

    private void generateRandomNickname() {
        String[] prefixes = {"xX", "Pro", "Noob", "Epic", "Dark", "Light", "Cool", "Fast", "Mr", "Dr"};
        String[] suffixes = {"Gamer", "Player", "Killer", "Ninja", "King", "Lord", "God", "Devil"};

        StringBuilder sb = new StringBuilder();
        sb.append(prefixes[random.nextInt(prefixes.length)])
                .append(suffixes[random.nextInt(suffixes.length)])
                .append(random.nextInt(9999));
        String result = sb.toString();
        if (result.length() > 16) result = result.substring(0, 16);
        inputText = result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!inputActive) return super.keyPressed(keyCode, scanCode, modifiers);

        if (keyCode == 256) { // ESC
            inputActive = false;
            return true;
        }

        if (keyCode == 257 || keyCode == 335) { // Enter
            addAlt();
            inputActive = false;
            return true;
        }

        if (keyCode == 259) { // Backspace
            if (!inputText.isEmpty()) {
                inputText = inputText.substring(0, inputText.length() - 1);
            }
            return true;
        }

        char c = (char) scanCode;
        if (inputText.length() < 16 && isValidChar(c)) {
            inputText += c;
        }

        return true;
    }

    private boolean isValidChar(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-';
    }

    @Override
    public void close() {
        mc.setScreen(previousScreen);
    }

    static class AltEntry {
        final String name;
        Identifier skinId;

        AltEntry(String name) {
            this.name = name;
        }
    }
}