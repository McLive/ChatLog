package eu.mclive.ChatLog.update;

import eu.mclive.ChatLog.ChatLog;
import eu.mclive.ChatLog.Permission;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Updater inspired from ViaVersion
 * https://github.com/ViaVersion/ViaVersion/tree/master/common/src/main/java/com/viaversion/viaversion/update
 */
public class UpdateListener implements Listener {

    private final ChatLog plugin;

    public UpdateListener(ChatLog plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (e.getPlayer().hasPermission(Permission.UPDATE)
			&& (plugin.getConfig().getBoolean("update-check"))) {
            UpdateUtil.sendUpdateMessage(e.getPlayer().getUniqueId(), plugin);
        }
    }
}