package www.legendarycommunity.com.br.legendary_boss_system.mobsSystem;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockBreakListener implements Listener {
    private final fada fadaHandler;

    public BlockBreakListener(JavaPlugin plugin) {
        this.fadaHandler = new fada(plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material brokenBlock = event.getBlock().getType();
        fadaHandler.onBlockBreak(player, brokenBlock);
    }
}
