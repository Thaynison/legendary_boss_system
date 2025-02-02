package www.legendarycommunity.com.br.legendary_boss_system.mobsSystem;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class spawnMandamentos implements Listener {

    private JavaPlugin plugin;

    public spawnMandamentos(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerUseItem(PlayerInteractEvent event) {
        // Verifica se a interação é um clique direito (uso do item)
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return; // Sai do método se a ação não for um clique direito
        }

        // Verifica se o jogador está segurando um item
        if (event.getItem() != null && event.getItem().getType() == Material.PAPER) {
            ItemStack item = event.getItem();
            ItemMeta meta = item.getItemMeta();

            if (meta != null && meta.hasDisplayName()) {
                String nameTag = meta.getDisplayName();
                String strippedNameTag = ChatColor.stripColor(nameTag); // Remove as cores

                // Lista de bosses configurados
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

                // Verifica se o nameTag corresponde a algum boss no config.yml
                for (String bossKey : bosses) {
                    if (strippedNameTag.equals(plugin.getConfig().getString(bossKey))) {
                        // Spawna o boss e remove o papel
                        spawnBoss(bossKey.split("\\.")[1], event.getPlayer().getLocation());

                        // Remove 1 papel da mão do jogador
                        if (item.getAmount() > 1) {
                            item.setAmount(item.getAmount() - 1);
                        } else {
                            event.getPlayer().getInventory().setItemInMainHand(null);
                        }
                        return; // Sai do método após encontrar e processar um boss
                    }
                }
            }
        }
    }

    private void spawnBoss(String mandamento, Location playerLocation) {
        // Pega as informações do boss no config
        String mobType = plugin.getConfig().getString("Boss." + mandamento + ".mob");
        double vida = plugin.getConfig().getDouble("Boss." + mandamento + ".vida");
        int forca = plugin.getConfig().getInt("Boss." + mandamento + ".forca");
        String spawnCoord = plugin.getConfig().getString("Boss." + mandamento + ".spawnCoord");
        String nameTag = plugin.getConfig().getString("Boss." + mandamento + ".nameTag");

        // Pega a coordenada de spawn do config (formato "x y z")
        String[] coords = spawnCoord.split(" ");
        Location spawnLocation = new Location(Bukkit.getWorld("world"),
                Double.parseDouble(coords[0]),
                Double.parseDouble(coords[1]),
                Double.parseDouble(coords[2]));

        // Cria o mob (zumbi, conforme o config)
        Entity boss = Bukkit.getWorld("world").spawnEntity(spawnLocation, org.bukkit.entity.EntityType.valueOf(mobType.toUpperCase()));

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
            zombie.getEquipment().setHelmet(null);  // Remove o capacete
            zombie.getEquipment().setChestplate(null);  // Remove o capacete
            zombie.getEquipment().setLeggings(null);  // Remove o capacete
            zombie.getEquipment().setBoots(null);  // Remove o capacete
            zombie.getEquipment().setItemInMainHand(null);  // Remove o capacete
            zombie.getEquipment().setItemInOffHand(null);  // Remove o capacete

            bossMobs.add(zombie.getUniqueId());
        }

        // Log de criação do boss (para debugar)
        plugin.getLogger().info("Boss " + nameTag + " spawnado com sucesso!");
    }

    private final Set<UUID> bossMobs = new HashSet<>();

    @EventHandler
    public void onBossDamagePlayer(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Mob) || !(event.getEntity() instanceof Player)) return;

        Mob zombie = (Mob) event.getDamager();
        Player player = (Player) event.getEntity();

        // Verifica se o mob é um boss
        if (!bossMobs.contains(zombie.getUniqueId())) return;

        // Gera um número aleatório entre 1 e 100
        double randomChance = Math.random() * 100;

        // Se a chance for menor ou igual a 20, remove 4 corações (8 pontos de vida)
        if (randomChance <= 5) {
            double newHealth = Math.max(player.getHealth() - 10, 0); // Garante que a vida não fique negativa
            player.setHealth(newHealth);
            event.setDamage(0); // Cancela o dano normal do evento, já que aplicamos dano direto
        }
    }

}
