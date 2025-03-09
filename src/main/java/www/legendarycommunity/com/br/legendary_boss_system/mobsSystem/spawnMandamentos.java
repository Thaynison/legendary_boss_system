package www.legendarycommunity.com.br.legendary_boss_system.mobsSystem;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class spawnMandamentos implements Listener {

    private JavaPlugin plugin;
    private final Set<UUID> bossMobs = new HashSet<>();

    public spawnMandamentos(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerUseItem(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getItem() != null && event.getItem().getType() == Material.PAPER) {
            ItemStack item = event.getItem();
            ItemMeta meta = item.getItemMeta();

            if (meta != null && meta.hasDisplayName()) {
                String nameTag = meta.getDisplayName();
                String strippedNameTag = ChatColor.stripColor(nameTag);

                String[] bosses = {
                        "Boss.Mandamento1.itemUseSpawn.nameTag",
                        "Boss.Mandamento2.itemUseSpawn.nameTag",
                        "Boss.Mandamento3.itemUseSpawn.nameTag",
                        "Boss.Mandamento4.itemUseSpawn.nameTag",
                        "Boss.Mandamento5.itemUseSpawn.nameTag",
                        "Boss.Mandamento6.itemUseSpawn.nameTag",
                        "Boss.Mandamento7.itemUseSpawn.nameTag",
                        "Boss.Mandamento8.itemUseSpawn.nameTag",
                        "Boss.Mandamento9.itemUseSpawn.nameTag",
                        "Boss.Mandamento10.itemUseSpawn.nameTag",
                        "Boss.Arcanjo1.itemUseSpawn.nameTag",
                        "Boss.Arcanjo2.itemUseSpawn.nameTag",
                        "Boss.Arcanjo3.itemUseSpawn.nameTag",
                        "Boss.Arcanjo4.itemUseSpawn.nameTag"
                };

                for (String bossKey : bosses) {
                    if (strippedNameTag.equals(plugin.getConfig().getString(bossKey))) {
                        spawnBoss(bossKey.split("\\.")[1], event.getPlayer().getLocation());

                        if (item.getAmount() > 1) {
                            item.setAmount(item.getAmount() - 1);
                        } else {
                            event.getPlayer().getInventory().setItemInMainHand(null);
                        }
                        return;
                    }
                }
            }
        }
    }

    private void spawnBoss(String mandamento, Location playerLocation) {
        String mobType = plugin.getConfig().getString("Boss." + mandamento + ".mob");
        double vida = plugin.getConfig().getDouble("Boss." + mandamento + ".vida");
        int forca = plugin.getConfig().getInt("Boss." + mandamento + ".forca");
        String spawnCoord = plugin.getConfig().getString("Boss." + mandamento + ".spawnCoord");
        String nameTag = plugin.getConfig().getString("Boss." + mandamento + ".nameTag");

        String[] coords = spawnCoord.split(" ");
        Location spawnLocation = new Location(Bukkit.getWorld("world"),
                Double.parseDouble(coords[0]),
                Double.parseDouble(coords[1]),
                Double.parseDouble(coords[2]));

        Entity boss = Bukkit.getWorld("world").spawnEntity(spawnLocation, EntityType.valueOf(mobType.toUpperCase()));

        if (boss instanceof Zombie) {
            Zombie zombie = (Zombie) boss;

            zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(vida);
            zombie.setHealth(vida);
            zombie.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(forca);
            zombie.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).setBaseValue(forca);
            zombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.5);

            zombie.setCustomName(nameTag);
            zombie.setCustomNameVisible(true);
            zombie.setBaby(false);

            bossMobs.add(zombie.getUniqueId());
        }

        plugin.getLogger().info("Boss " + nameTag + " spawnado com sucesso!");
    }

    @EventHandler
    public void onBossDamagePlayer(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Mob) || !(event.getEntity() instanceof Player)) return;

        Mob zombie = (Mob) event.getDamager();
        Player player = (Player) event.getEntity();

        if (!bossMobs.contains(zombie.getUniqueId())) return;

        double randomChance = Math.random() * 100;

        if (randomChance <= 5) {
            double newHealth = Math.max(player.getHealth() - 10, 0);
            player.setHealth(newHealth);
            event.setDamage(0);
        }
    }

    /**
     * Impede que os Mandamentos tomem dano de fogo, lava ou sol.
     */
    @EventHandler
    public void onBossTakeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Zombie)) return;

        Zombie boss = (Zombie) event.getEntity();

        if (bossMobs.contains(boss.getUniqueId())) {
            EntityDamageEvent.DamageCause cause = event.getCause();

            if (cause == EntityDamageEvent.DamageCause.FIRE ||
                    cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
                    cause == EntityDamageEvent.DamageCause.LAVA ||
                    cause == EntityDamageEvent.DamageCause.HOT_FLOOR) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Impede que os Mandamentos peguem fogo.
     */
    @EventHandler
    public void onBossCombust(EntityCombustEvent event) {
        if (bossMobs.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
