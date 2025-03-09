package www.legendarycommunity.com.br.legendary_boss_system.mobs;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class dropsModifyMobs implements Listener {

    private JavaPlugin plugin;

    public dropsModifyMobs(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            // Carregar a configuração
            FileConfiguration config = plugin.getConfig();

            // Iterar pelos drops configurados
            for (String key : config.getConfigurationSection("Drops").getKeys(false)) {
                String mobName = config.getString("Drops." + key + ".mob");
                String mobNameTag = config.getString("Drops." + key + ".mobNameTag");

                // Verificar se o nome do mob corresponde e o mob tem um nome customizado
                if (entity.getType().name().equalsIgnoreCase(mobName)) {
                    if (entity instanceof Mob) {
                        Mob mob = (Mob) entity;
                        if (mobNameTag != null && mob.getCustomName() != null && mob.getCustomName().equalsIgnoreCase(mobNameTag)) {
                            // Verificar a chance de drop
                            double chance = config.getDouble("Drops." + key + ".chanceDrop");
                            if (Math.random() <= chance) {
                                // Criar o item
                                String itemName = config.getString("Drops." + key + ".itemDrop");
                                String displayName = config.getString("Drops." + key + ".itemDisplayName");

                                ItemStack item = new ItemStack(Material.getMaterial(itemName));
                                if (item != null) {
                                    // Alterar o nome do item
                                    ItemMeta meta = item.getItemMeta();
                                    if (meta != null) {
                                        meta.setDisplayName(ChatColor.RED + displayName);
                                        item.setItemMeta(meta);  // Aplica o ItemMeta alterado no ItemStack
                                    }

                                    // Adicionar ao inventário do jogador ou drop
                                    if (entity instanceof Player) {
                                        Player player = (Player) entity;
                                        player.getInventory().addItem(item);
                                    } else {
                                        entity.getWorld().dropItemNaturally(entity.getLocation(), item);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
