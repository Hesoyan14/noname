package code.essence.features.impl.misc;

import code.essence.display.screens.autobuy.AutoBuyScreen;
import code.essence.display.screens.autobuy.history.HistoryRenderer;
import code.essence.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import code.essence.display.screens.clickgui.components.implement.autobuy.manager.AutoBuyManager;
import code.essence.events.container.HandledScreenEvent;
import code.essence.events.keyboard.GuiScrollEvent;
import code.essence.features.impl.misc.autobuy.*;
import code.essence.features.impl.misc.autobuy.holyworld.HolyWorldAuctionHandler;
import code.essence.features.impl.misc.autobuy.spookytime.SpookyTimeAuctionHandler;
import code.essence.features.impl.misc.autobuy.parser.FunTimePriceParser;
import code.essence.features.impl.misc.autobuy.parser.HolyWorldPriceParser;
import code.essence.features.impl.misc.autobuy.parser.SpookyTimePriceParser;
import code.essence.features.module.ModuleCategory;
import code.essence.features.module.setting.implement.BindSetting;
import code.essence.features.module.setting.implement.BooleanSetting;
import code.essence.features.module.setting.implement.RadioSetting;
import code.essence.features.module.setting.implement.SelectSetting;
import code.essence.features.module.setting.implement.SliderSettings;
import code.essence.utils.client.Instance;
import code.essence.utils.client.managers.event.EventHandler;
import code.essence.events.keyboard.KeyEvent;
import code.essence.events.packet.PacketEvent;
import code.essence.events.player.InputEvent;
import code.essence.events.player.TickEvent;
import code.essence.features.module.Module;
import code.essence.features.impl.misc.autobuy.CommandSender;
import code.essence.utils.client.chat.ChatMessage;
import code.essence.utils.client.packet.network.Network;
import code.essence.utils.math.calc.Calculate;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AutoBuy extends Module {

    public static AutoBuy getInstance() {
        return Instance.get(AutoBuy.class);
    }


    private final RadioSetting serverMode = new RadioSetting("Сервер", "", new String[]{"FunTime", "SpookyTime", "HolyWorld"}, "FunTime");

    private final SelectSetting leaveType = new SelectSetting("Тип обхода", "Покупающий")
            .value("Покупающий", "Проверяющий").visible(() -> serverMode.get().equals("FunTime"));

    BindSetting autobuyGui = new BindSetting("Открыть гуи","");


    private final SliderSettings timer2 = new SliderSettings("Таймер", "")
            .setValue(350).range(350, 750).visible(() -> serverMode.get().equals("FunTime"));



    private final RadioSetting versionSetting = new RadioSetting("Версия", "", new String[]{"1.16.5", "1.21.4"}, "1.16.5").visible(() -> serverMode.get().equals("FunTime"));
    private final BooleanSetting autoStorage = new BooleanSetting("Автоскладирование", "").visible(() -> !serverMode.get().equals("HolyWorld"));

    private final AutoBuyManager autoBuyManager = AutoBuyManager.getInstance();
    private final NetworkManager networkManager;
    private final ServerManager serverManager;
    private final StorageManager storageManager;
    private final AuctionHandler auctionHandler;
    private final HolyWorldAuctionHandler holyWorldAuctionHandler;
    private final SpookyTimeAuctionHandler spookyTimeAuctionHandler;
    private final AfkHandler afkHandler;

    private final Random random = new Random();
    private final TimerUtil openTimer = TimerUtil.create();
    private final TimerUtil updateTimer = TimerUtil.create();
    private final TimerUtil buyTimer = TimerUtil.create();
    private final TimerUtil switchTimer = TimerUtil.create();
    private final TimerUtil enterDelayTimer = TimerUtil.create();
    private final TimerUtil ahSpamTimer = TimerUtil.create();
    private final TimerUtil connectionCheckTimer = TimerUtil.create();
    private final TimerUtil auctionRequestTimer = TimerUtil.create();
    private final TimerUtil reconnectTimer = TimerUtil.create();


    private boolean open = false;
    private boolean serverInAuction = false;

    /** Цикл #goto: 2 мин → 4 мин → 8 мин → 12 мин → снова 2 мин. Координаты по кругу. */
    public static final long[] GOTO_DELAYS_MS = { 120_000, 240_000, 180_000, 240_000, };
    private static final double[][] GOTO_WAYPOINTS = {
            { -11, 71, -46 },
            { 23, 69, -25 },
            {  -8, 69, 24 },
            { 50, 69, 57 }
    };
    private static final double GOTO_ARRIVAL_RADIUS = 2;
    private static final long GOTO_AFTER_ARRIVAL_MS = 1000;
    private final TimerUtil gotoTimer = TimerUtil.create();
    private int gotoStepIndex = 0;
    private boolean waitingForGotoArrival = false;
    private long gotoArrivedAtMs = 0;
    private double gotoTargetX, gotoTargetY, gotoTargetZ;
    private boolean pendingGotoAfterClose = false;
    private long lastGotoRemainingLogMs = 0;
    private boolean justEntered = false;
    private boolean spammingAh = false;
    private boolean waitingForAuctionOpen = false;
    private boolean waitingForReconnect = false;
    private String lastServerAddress = "mc.funtime.su";
    private final List<AutoBuyableItem> cachedEnabledItems = new ArrayList<>();
    private long randomizedTimerValue = 350;

    public AutoBuy() {
        super("AutoBuy", "AutoBuy", ModuleCategory.MISC);

        setup(serverMode, leaveType, timer2, versionSetting, autoStorage, autobuyGui);

        networkManager = new NetworkManager();
        serverManager  = new ServerManager(versionSetting);
        storageManager = new StorageManager(autoStorage);
        auctionHandler = new AuctionHandler(autoBuyManager);
        holyWorldAuctionHandler = new HolyWorldAuctionHandler(autoBuyManager);
        spookyTimeAuctionHandler = new SpookyTimeAuctionHandler(autoBuyManager);
        afkHandler     = new AfkHandler();
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public boolean isBuyerMode() {
        return leaveType != null && leaveType.isSelected("Покупающий");
    }


    public ServerMode getServerMode() {
        if (serverMode == null) return ServerMode.FUNTIME;
        String selected = serverMode.get();
        if ("SpookyTime".equals(selected)) return ServerMode.SPOOKYTIME;
        if ("HolyWorld".equals(selected)) return ServerMode.HOLYWORLD;
        return ServerMode.FUNTIME;
    }

    public boolean isFunTimeMode() {
        return getServerMode() == ServerMode.FUNTIME;
    }

    public boolean isSpookyTimeMode() {
        return getServerMode() == ServerMode.SPOOKYTIME;
    }

    public boolean isHolyWorldMode() {
        return getServerMode() == ServerMode.HOLYWORLD;
    }


    public enum ServerMode {
        FUNTIME("FunTime"),
        SPOOKYTIME("SpookyTime"),
        HOLYWORLD("HolyWorld");

        private final String displayName;

        ServerMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Override
    public void activate() {
        super.activate();
        resetTimers();
        resetState();


        if (leaveType.isSelected("Покупающий") && (versionSetting.get().equals("1.16.5") || versionSetting.get().equals("1.21.4"))) {
            mc.options.pauseOnLostFocus = false;
        }


        if (mc.getCurrentServerEntry() != null) {
            lastServerAddress = mc.getCurrentServerEntry().address;
        }

        cacheEnabledItems();

        if (getServerMode() == ServerMode.FUNTIME) {
            networkManager.start(leaveType.getSelected());
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();

        if (getServerMode() == ServerMode.FUNTIME) {
            networkManager.stop();
        }
        serverManager.reset();
        storageManager.reset();
        afkHandler.resetMovementKeys(mc.options);
    }

    private void resetTimers() {
        openTimer.resetCounter();
        updateTimer.resetCounter();
        buyTimer.resetCounter();
        switchTimer.resetCounter();
        enterDelayTimer.resetCounter();
        ahSpamTimer.resetCounter();
        connectionCheckTimer.resetCounter();
        auctionRequestTimer.resetCounter();
        reconnectTimer.resetCounter();
        gotoTimer.resetCounter();

        serverManager.resetTimers();
        storageManager.resetTimers();
        afkHandler.resetTimers();
    }

    private void resetState() {
        open = false;
        serverInAuction = false;
        justEntered = false;
        spammingAh = false;
        waitingForAuctionOpen = false;
        waitingForReconnect = false;
        waitingForGotoArrival = false;
        gotoArrivedAtMs = 0;
        pendingGotoAfterClose = false;
        cachedEnabledItems.clear();

        if (getServerMode() == ServerMode.FUNTIME) {
            networkManager.clearQueues();
        }
        auctionHandler.clear();
        holyWorldAuctionHandler.clear();
        spookyTimeAuctionHandler.clear();
    }

    private void generateRandomizedTimer() {
        float baseValue = timer2.getValue();

        float randomFactor = 0.85f + (float) (Math.random() * 0.3f);
        randomizedTimerValue = (long) (baseValue * randomFactor);
    }

    private void cacheEnabledItems() {
        cachedEnabledItems.clear();


        List<AutoBuyableItem> itemsToCheck;
        ServerMode mode = getServerMode();

        switch (mode) {
            case HOLYWORLD:
                itemsToCheck = code.essence.display.screens.clickgui.components.implement.autobuy.originalitems.ItemRegistry.getHolyWorld();
                break;
            case SPOOKYTIME:
                itemsToCheck = code.essence.display.screens.clickgui.components.implement.autobuy.originalitems.ItemRegistry.getSpookyTime();
                break;
            case FUNTIME:
            default:

                itemsToCheck = code.essence.display.screens.clickgui.components.implement.autobuy.originalitems.ItemRegistry.getFunTimeItems();
                break;
        }

        for (AutoBuyableItem item : itemsToCheck) {
            if (item == null) {
                continue;
            }
            if (item.isEnabled()) {
                cachedEnabledItems.add(item);
            }
        }
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        if (e.isKeyDown(autobuyGui.getKey())) {
            AutoBuyScreen.INSTANCE.openGui();
        }
    }

    @EventHandler
    public void onHandledScreen(HandledScreenEvent e) {
        HistoryRenderer.getInstance().render(e.getDrawContext(), e.getMouseX(), e.getMouseY(), e.getTickDelta());
    }

    @EventHandler
    public void onGuiScroll(GuiScrollEvent e) {

        if (mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen) {
            return;
        }
        HistoryRenderer.getInstance().mouseScrolled(e.getMouseX(), e.getMouseY(), e.getVertical());
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (e.getPacket() instanceof GameMessageS2CPacket gameMessage) {
            Text content = gameMessage.content();
            String message = content.getString();


            if (message.contains("Вы уже подключены к этому серверу!")) {

                if (getServerMode() == ServerMode.FUNTIME) {
                    serverManager.switchToNextServer(mc.player, networkManager, leaveType.isSelected("Покупающий"));
                }
                return;
            }


            if (leaveType.isSelected("Покупающий") || isHolyWorldMode() || isSpookyTimeMode()) {
                PurchaseHandler.handlePurchaseMessage(message, auctionHandler, holyWorldAuctionHandler, spookyTimeAuctionHandler);
            }
        }


        if (e.getPacket() instanceof DisconnectS2CPacket disconnectPacket && leaveType.isSelected("Проверяющий")) {
            String reason = disconnectPacket.reason().getString();
            // ChatMessage.brandmessage("Сервер отключился: " + reason);
            // ChatMessage.brandmessage("Ожидание переподключения...");
            waitingForReconnect = true;
            reconnectTimer.resetCounter();
        }
    }

    private void tickGotoCycle() {
        if (!isHolyWorldMode() || !autoBuyManager.isEnabled()) return;
        if (mc.player == null) return;

        if (pendingGotoAfterClose) {
            if (!(mc.currentScreen instanceof GenericContainerScreen)) {
                double[] wp = GOTO_WAYPOINTS[gotoStepIndex];
                gotoTargetX = wp[0];
                gotoTargetY = wp[1];
                gotoTargetZ = wp[2];
                String cmd = "#goto " + (int) gotoTargetX + " " + (int) gotoTargetY + " " + (int) gotoTargetZ;
                mc.getNetworkHandler().sendChatMessage(cmd);
                waitingForGotoArrival = true;
                gotoArrivedAtMs = 0;
                gotoTimer.resetCounter();
                pendingGotoAfterClose = false;
            }
            return;
        }

        if (waitingForGotoArrival) {
            double dx = mc.player.getX() - gotoTargetX;
            double dy = mc.player.getY() - gotoTargetY;
            double dz = mc.player.getZ() - gotoTargetZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist <= GOTO_ARRIVAL_RADIUS) {
                if (gotoArrivedAtMs == 0) gotoArrivedAtMs = System.currentTimeMillis();
                if (System.currentTimeMillis() - gotoArrivedAtMs >= GOTO_AFTER_ARRIVAL_MS) {
                    waitingForGotoArrival = false;
                    gotoArrivedAtMs = 0;
                    // ChatMessage.brandmessage("§e[AutoBuy HW] §fПрибыл, открываю /ah");
                    CommandSender.sendCommand(mc.player, "/ah");
                    gotoStepIndex = (gotoStepIndex + 1) % 4;
                    gotoTimer.resetCounter();
                }
            } else {
                gotoArrivedAtMs = 0;
            }
            return;
        }

        long delay = GOTO_DELAYS_MS[gotoStepIndex];
        long elapsed = gotoTimer.getTime();
        long remaining = delay - elapsed;

        if (!gotoTimer.hasTimeElapsed(delay)) {
            long now = System.currentTimeMillis();
            if (now - lastGotoRemainingLogMs >= 30_000) {
                ChatMessage.brandmessage(String.format("[AutoBuy #goto] до след. #goto: %d мс (шаг %d, delay=%d мс, прошло=%d мс)", remaining/1000, gotoStepIndex, delay, elapsed));
                lastGotoRemainingLogMs = now;
            }
            return;
        }

        if (mc.currentScreen instanceof GenericContainerScreen) {
            mc.player.closeHandledScreen();
            pendingGotoAfterClose = true;
        } else {
            double[] wp = GOTO_WAYPOINTS[gotoStepIndex];
            gotoTargetX = wp[0];
            gotoTargetY = wp[1];
            gotoTargetZ = wp[2];
            String cmd = "#goto " + (int) gotoTargetX + " " + (int) gotoTargetY + " " + (int) gotoTargetZ;
            mc.getNetworkHandler().sendChatMessage(cmd);
            waitingForGotoArrival = true;
            gotoArrivedAtMs = 0;
        }
        gotoTimer.resetCounter();
    }

    @EventHandler
    public void onTick(TickEvent e) {

        if (leaveType.isSelected("Проверяющий")) {
            handleCheckerReconnect();
        }

        if (mc.player == null || mc.world == null) return;

        tickGotoCycle();


        boolean holyWorldParserEnabled = HolyWorldPriceParser.isEnabled() &&
                getServerMode() == ServerMode.HOLYWORLD;


        boolean spookyTimeParserEnabled = SpookyTimePriceParser.isEnabled() &&
                getServerMode() == ServerMode.SPOOKYTIME;


        boolean parserEnabled = FunTimePriceParser.isEnabled() &&
                leaveType != null && leaveType.isSelected("Покупающий");



        if (!autoBuyManager.isEnabled() && !parserEnabled && !holyWorldParserEnabled && !spookyTimeParserEnabled) return;


        if (autoBuyManager.isEnabled()) {
            handleConnectionStatus();
            afkHandler.handle(mc);
            storageManager.handle(mc, open);

            if (storageManager.isActive()) {

                if (!parserEnabled) {
                    return;
                }
            }

            if (storageManager.handlePostStorage(mc, enterDelayTimer, ahSpamTimer)) {
                justEntered = true;
            }

            boolean wasInHub = serverManager.isInHub();
            serverManager.updateHubStatus(mc.world);


            if (getServerMode() == ServerMode.FUNTIME && serverManager.shouldJoinAnarchy() && !FunTimePriceParser.isEnabled()) {
                serverManager.joinAnarchyFromHub(mc.player);
            }

            if (wasInHub && !serverManager.isInHub()) {
                handleServerSwitch();
            }

            if (serverManager.isWaitingForServerLoad() || ServerSwitchHandler.isWaitingForServerLoad()) {
                if (ServerSwitchHandler.hasTimedOut() || (!wasInHub && !serverManager.isInHub())) {
                    serverManager.setWaitingForServerLoad(false);
                    ServerSwitchHandler.setWaitingForServerLoad(false);
                    handleServerSwitch();
                }
            }

            handleAhSpam();
        }




        if (holyWorldParserEnabled) {
            if (!(mc.currentScreen instanceof GenericContainerScreen)) {
                HolyWorldPriceParser.tickNoContainer(mc, this);
            }
        }

        if (spookyTimeParserEnabled) {
            if (!(mc.currentScreen instanceof GenericContainerScreen)) {
                SpookyTimePriceParser.tickNoContainer(mc, this);
            }
        }


        if (getServerMode() == ServerMode.HOLYWORLD && autoBuyManager.isEnabled()) {
            if (!(mc.currentScreen instanceof GenericContainerScreen)) {
                holyWorldAuctionHandler.tickReconnect();
            }
        }


        handleAuction();


        if (parserEnabled && !(mc.currentScreen instanceof GenericContainerScreen)) {
            FunTimePriceParser.tickNoContainer(mc, this, networkManager);
        }

        handleServerAutoSwitch();
        handleCheckerAuctionRequest();
    }

    private void handleCheckerReconnect() {

        if (mc.currentScreen instanceof DisconnectedScreen) {
            if (!waitingForReconnect) {
                waitingForReconnect = true;
                reconnectTimer.resetCounter();
                // ChatMessage.brandmessage("Обнаружен дисконнект, ожидание переподключения...");
            }
        }


        if (waitingForReconnect && reconnectTimer.hasTimeElapsed(5000)) {
            if (mc.currentScreen instanceof DisconnectedScreen || mc.currentScreen instanceof MultiplayerScreen || mc.currentScreen instanceof TitleScreen) {
                // ChatMessage.brandmessage("Попытка переподключения к серверу...");
                reconnectToServer();
                reconnectTimer.resetCounter();
            }
        }


        if (waitingForReconnect && mc.player != null && mc.world != null) {
            waitingForReconnect = false;
            // ChatMessage.brandmessage("Успешно переподключен к серверу!");
        }
    }

    private void reconnectToServer() {
        try {
            ServerInfo serverInfo = new ServerInfo("AutoBuy Server", lastServerAddress, ServerInfo.ServerType.OTHER);
            ServerAddress serverAddress = ServerAddress.parse(lastServerAddress);
            ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), mc, serverAddress, serverInfo, false, null);
        } catch (Exception ex) {
            // ChatMessage.brandmessage("Ошибка переподключения: " + ex.getMessage());
        }
    }

    private void handleConnectionStatus() {

        if (getServerMode() != ServerMode.FUNTIME) {
            return;
        }

        if (leaveType.isSelected("Проверяющий")) {
            if (connectionCheckTimer.hasTimeElapsed(5000)) {
                if (!networkManager.isConnectedToServer()) {
                    networkManager.start(leaveType.getSelected());
                }
                connectionCheckTimer.resetCounter();
            }
        }
    }

    private void handleServerSwitch() {
        justEntered = true;
        enterDelayTimer.resetCounter();
        switchTimer.resetCounter();
        storageManager.resetMaxShulkers();
        waitingForAuctionOpen = false;
        auctionRequestTimer.resetCounter();
    }

    private void handleAhSpam() {

        if (getServerMode() == ServerMode.HOLYWORLD) {

            if (spammingAh) {
                spammingAh = false;
            }
            return;
        }

        if (!(leaveType.isSelected("Покупающий") && (versionSetting.get().equals("1.16.5") || versionSetting.get().equals("1.21.4")))) {
            return;
        }

        if (justEntered && enterDelayTimer.hasTimeElapsed(2000)) {
            if (!spammingAh) {
                spammingAh = true;
                ahSpamTimer.resetCounter();
            }
        }

        if (spammingAh && !afkHandler.isPerformingAction()) {
            if (ahSpamTimer.hasTimeElapsed(5000)) {
                if (mc.player != null && mc.player.networkHandler != null) {

                }
                ahSpamTimer.resetCounter();
            }
        }
    }

    private void handleCheckerAuctionRequest() {

        if (getServerMode() != ServerMode.FUNTIME) {
            return;
        }

        if (!leaveType.isSelected("Проверяющий")) return;


        if (!open && !waitingForAuctionOpen) {
            if (auctionRequestTimer.hasTimeElapsed(3000)) {
                if (networkManager.isConnectedToServer()) {
                    CommandSender.openAuction();
                    waitingForAuctionOpen = true;
                    auctionRequestTimer.resetCounter();
                }
            }
        }


        if (waitingForAuctionOpen && auctionRequestTimer.hasTimeElapsed(5000)) {
            waitingForAuctionOpen = false;
            auctionRequestTimer.resetCounter();
        }
    }

    private void handleAuction() {
        handleCheckerAuctionRequest();

        if (mc.currentScreen instanceof GenericContainerScreen screen) {
            String title = screen.getTitle().getString();
            int syncId = screen.getScreenHandler().syncId;
            List<Slot> slots = screen.getScreenHandler().slots;

            if (title.contains("Аукцион") || title.contains("Аукционы") || title.contains("Поиск")) {
                if (!open) {
                    enterAuction();
                    return;
                }

                storageManager.handleAuctionEnter();


                ServerMode currentMode = getServerMode();


                if (currentMode == ServerMode.SPOOKYTIME) {

                    if (SpookyTimePriceParser.isEnabled()) {
                        SpookyTimePriceParser.tickAuction(mc, slots, title, this);
                        return;
                    }
                    if (autoBuyManager.isEnabled()) {
                        handleSpookyTimeMode(syncId, slots);
                    }
                    return;
                }


                if (currentMode == ServerMode.HOLYWORLD) {

                    if (HolyWorldPriceParser.isEnabled()) {
                        HolyWorldPriceParser.tickAuction(mc, slots, title, this);
                        return;
                    }
                    handleHolyWorldMode(syncId, slots);
                    return;
                }


                if (currentMode == ServerMode.FUNTIME &&
                        (Network.isFunTime() || (Network.isCopyTime() && !Network.isSpookyTime())) &&
                        FunTimePriceParser.isEnabled()) {

                    FunTimePriceParser.tickAuction(mc, slots, title, this, networkManager);
                    return;
                }

                if (leaveType.isSelected("Покупающий")) {
                    handleBuyerMode(screen, syncId, slots);
                } else if (leaveType.isSelected("Проверяющий")) {

                    if (FunTimePriceParser.isEnabled() &&
                            (Network.isFunTime() || (Network.isCopyTime() && !Network.isSpookyTime()))) {
                        handleCheckerParserMode(syncId, slots, title);
                    } else {
                        try {
                            handleCheckerMode(slots);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            } else if (title.contains("Подозрительная цена") || title.contains("Подтверждение") || title.contains("Confirm") ||
                    title.contains("Покупка предмета") ) {



                if (isHolyWorldMode()) {
                    holyWorldAuctionHandler.handleConfirmation(syncId, slots, title);
                    long refreshDelay = holyWorldAuctionHandler.getRefreshDelayMs();
                    long buyDelay = 0;
                    holyWorldAuctionHandler.tick(syncId, slots, cachedEnabledItems, refreshDelay, buyDelay);
                } else {

                    auctionHandler.handleSuspiciousPrice(mc, syncId, slots);
                }
                openTimer.resetCounter();
                buyTimer.resetCounter();
            } else {
                exitAuction();
            }
        } else {
            exitAuction();
        }
    }

    private void enterAuction() {
        open = true;
        openTimer.resetCounter();
        updateTimer.resetCounter();
        buyTimer.resetCounter();
        generateRandomizedTimer();

        storageManager.notifyAuctionEnter();
        serverInAuction = true;
        auctionHandler.clear();

        justEntered = false;
        spammingAh = false;
        waitingForAuctionOpen = false;
        storageManager.clearStorageCompleted();

        if (!storageManager.getPostStorageTimer().hasTimeElapsed(2000)) {
            storageManager.disableStartStorage();
        }

        cacheEnabledItems();


        if (getServerMode() == ServerMode.FUNTIME) {
            if (leaveType.isSelected("Проверяющий")) {
                networkManager.notifyAuctionEnter();
            }
            if (leaveType.isSelected("Покупающий")) {
                networkManager.requestAuctionOpen();
            }
        }
    }

    private void exitAuction() {
        if (open) {
            open = false;
            serverInAuction = false;
            auctionHandler.clear();


            if (getServerMode() == ServerMode.FUNTIME && leaveType.isSelected("Проверяющий")) {
                networkManager.notifyAuctionLeave();
            }
        }
    }

    private void handleBuyerMode(GenericContainerScreen screen, int syncId, List<Slot> slots) {

        if (getServerMode() != ServerMode.FUNTIME) {
            return;
        }

        long clientCount = networkManager.getClientInAuctionCount();



        if (networkManager.getQueueSize() > 30) {
            auctionHandler.updateAuction(mc, syncId);
            networkManager.sendUpdateToClients();
            updateTimer.resetCounter();
            networkManager.clearQueues();
            return;
        }

        if (!storageManager.hasReachedMaxShulkers()) {
            BuyRequest request = networkManager.pollRequest();
            if (request != null) {

                auctionHandler.handleBuyRequest(mc, syncId, slots, request, networkManager);
            } else {
            }
        } else {
        }


        if (auctionHandler.shouldUpdate()) {
            auctionHandler.updateAuction(mc, syncId);
            networkManager.sendUpdateToClients();
            updateTimer.resetCounter();
            networkManager.clearQueues();
        }


        if (updateTimer.hasTimeElapsed(randomizedTimerValue) &&
                serverInAuction &&
                clientCount > 0 &&
                networkManager.isQueuesEmpty()) {

            auctionHandler.updateAuction(mc, syncId);
            networkManager.sendUpdateToClients();
            updateTimer.resetCounter();
            generateRandomizedTimer();
        }
    }

    private void handleCheckerMode(List<Slot> slots) {


        if (cachedEnabledItems.isEmpty()) {
            return;
        }

        List<Slot> bestSlots = auctionHandler.findMatchingSlots(slots, cachedEnabledItems);

        if (!bestSlots.isEmpty()) {
            auctionHandler.processBestSlots(bestSlots, networkManager);
            buyTimer.resetCounter();
        } else {

        }
    }


    private void handleCheckerParserMode(int syncId, List<Slot> slots, String screenTitle) {

        if (getServerMode() != ServerMode.FUNTIME) {
            return;
        }

        if (slots == null || screenTitle == null) return;


        String currentItemName = FunTimePriceParser.getCurrentItemName();
        if (currentItemName == null || currentItemName.isEmpty()) {
            return;
        }


        int cheapestPerItem = FunTimePriceParser.findCheapestPerItemStatic(slots, currentItemName);

        if (cheapestPerItem > 0) {

            int discountPercent = FunTimePriceParser.getDiscountPercent();
            int newPrice = (int) Math.floor(cheapestPerItem * (100.0 - discountPercent) / 100.0);
            if (newPrice < 0) newPrice = 0;

            networkManager.sendParserResult(currentItemName, cheapestPerItem, newPrice);
        } else {
            networkManager.sendParserResult(currentItemName, -1, -1);
        }


        auctionHandler.updateAuction(mc, syncId);
    }

    private void handleServerAutoSwitch() {

        if (FunTimePriceParser.isEnabled()) {
            return;
        }



        if (getServerMode() == ServerMode.FUNTIME &&
                leaveType.isSelected("Покупающий") && (versionSetting.get().equals("1.16.5") || versionSetting.get().equals("1.21.4"))) {
            if (!serverManager.isInHub() && switchTimer.hasTimeElapsed(60000)) {
                serverManager.switchToNextServer(mc.player, networkManager, true);
            }
        }
    }


    private void handleHolyWorldMode(int syncId, List<Slot> slots) {
        holyWorldAuctionHandler.clearStaleConfirmIfOnAuction();
        if (cachedEnabledItems.isEmpty()) {
            return;
        }

        long refreshDelay = holyWorldAuctionHandler.getRefreshDelayMs();
        long buyDelay = 0;

        holyWorldAuctionHandler.tick(syncId, slots, cachedEnabledItems, refreshDelay, buyDelay);
    }


    private void handleSpookyTimeMode(int syncId, List<Slot> slots) {
        if (!autoBuyManager.isEnabled()) {
            return;
        }


        ServerMode currentMode = getServerMode();
        if (currentMode != ServerMode.SPOOKYTIME) {

            cacheEnabledItems();
            return;
        }


        if (cachedEnabledItems.isEmpty()) {
            cacheEnabledItems();
        }

        long refreshDelay = 300;
        long buyDelay = 0;

        spookyTimeAuctionHandler.tick(syncId, slots, cachedEnabledItems, refreshDelay, buyDelay);
    }


    public HolyWorldAuctionHandler getHolyWorldAuctionHandler() {
        return holyWorldAuctionHandler;
    }


    public SpookyTimeAuctionHandler getSpookyTimeAuctionHandler() {
        return spookyTimeAuctionHandler;
    }


    public AuctionHandler getAuctionHandler() {
        return auctionHandler;
    }
}