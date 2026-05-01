package code.essence.features.impl.misc.newAutoBuy.receiver;

import code.essence.events.packet.PacketEvent;
import code.essence.events.player.TickEvent;
import code.essence.features.module.Module;
import code.essence.features.module.ModuleCategory;
import code.essence.utils.client.managers.event.EventHandler;

/**
 * @author nikitavodolaz
 * @since 08.02.2026
 */

public class NewAutoBuyModule extends Module {

    /**
     * @TODO
     *
     * @Architecture
     * Controller -> Хранит все объекты для быстрого доступа
     * Storage -> Должен имень Pool из элементов доступных к покупке
     *
     */

    public NewAutoBuyModule() {
        super("NewAutoBuy", ModuleCategory.MISC);
    }

    @EventHandler
    public void packetEventReceive(PacketEvent packetEvent) {

    }

    @EventHandler
    public void tickEventReceive(TickEvent tickEvent) {

    }
}
