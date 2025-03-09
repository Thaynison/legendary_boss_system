package www.legendarycommunity.com.br.legendary_boss_system.mobsSystem;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class spawnPecados implements Listener {

    private final JavaPlugin plugin;
    private final Set<UUID> bossMobs = new HashSet<>();
    private final Map<UUID, Long> bossLastInteraction = new HashMap<>();
    private final Random random = new Random();

    public spawnPecados(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        iniciarScheduler();
    }

    private void verificarJogadoresProximos() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID bossId : bossMobs) {
                    Entity boss = Bukkit.getEntity(bossId);
                    if (boss != null && !boss.isDead()) {
                        for (Player player : boss.getWorld().getPlayers()) {
                            if (player.getLocation().distanceSquared(boss.getLocation()) <= 25) { // 5 blocos de distância
                                bossLastInteraction.put(bossId, System.currentTimeMillis()); // Atualiza tempo de atividade
                                break;
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Executa a cada 1 segundo
    }


    private void iniciarScheduler() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::spawnPecadoAleatorio, 0L, 432000L); // 6 horas em ticks
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Iterator<UUID> iterator = bossLastInteraction.keySet().iterator();
                while (iterator.hasNext()) {
                    UUID bossId = iterator.next();
                    if (now - bossLastInteraction.get(bossId) > 600000) { // 10 minutos (600.000ms)
                        Entity boss = Bukkit.getEntity(bossId);
                        if (boss != null && !boss.isDead()) {
                            boss.remove();
                            bossMobs.remove(bossId);
                            iterator.remove();
                            plugin.getLogger().info("Boss " + bossId + " removido por inatividade.");
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 60); // Verifica a cada 1 minuto

        // Chama a verificação de proximidade dos jogadores
        verificarJogadoresProximos();
    }


    private void spawnPecadoAleatorio() {
        Set<String> pecados = plugin.getConfig().getConfigurationSection("Pecados").getKeys(false);
        if (pecados.isEmpty()) return;

        String pecadoEscolhido = new ArrayList<>(pecados).get(random.nextInt(pecados.size()));
        spawnBoss(pecadoEscolhido);
    }

    private void spawnBoss(String pecadoId) {
        String mobType = plugin.getConfig().getString("Pecados." + pecadoId + ".mob");
        double vida = plugin.getConfig().getDouble("Pecados." + pecadoId + ".vida");
        int forca = plugin.getConfig().getInt("Pecados." + pecadoId + ".forca");
        String spawnCoord = plugin.getConfig().getString("Pecados." + pecadoId + ".spawnCoord");
        String nameTag = plugin.getConfig().getString("Pecados." + pecadoId + ".nameTag");

        if (spawnCoord == null || mobType == null || nameTag == null) return;

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
            zombie.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).setBaseValue(forca * 20);
            zombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.5);
            zombie.setCustomName(nameTag);
            zombie.setCustomNameVisible(true);
            zombie.setBaby(false);
            bossMobs.add(zombie.getUniqueId());
            bossLastInteraction.put(zombie.getUniqueId(), System.currentTimeMillis()); // Marca o tempo inicial
        }

        String formattedName = nameTag.substring(0, 1).toUpperCase() + nameTag.substring(1).toLowerCase();
        Bukkit.broadcastMessage("§f[§4Vaizel§f] " + ChatColor.GOLD + "Evento de Luta contra o Pecado " + ChatColor.RED + formattedName);

        plugin.getLogger().info("Boss " + nameTag + " spawnado com sucesso!");
    }

    @EventHandler
    public void onBossDamagePlayer(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Mob) || !(event.getEntity() instanceof Player)) return;
        Mob zombie = (Mob) event.getDamager();
        Player player = (Player) event.getEntity();
        if (!bossMobs.contains(zombie.getUniqueId())) return;

        bossLastInteraction.put(zombie.getUniqueId(), System.currentTimeMillis()); // Atualiza tempo de atividade

        double randomChance = Math.random() * 100;
        if (randomChance <= 5) {
            player.setHealth(Math.max(player.getHealth() - 10, 0));
            event.setDamage(0);
        }
    }

    @EventHandler
    public void onBossTakeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Zombie)) return;
        Zombie boss = (Zombie) event.getEntity();
        if (bossMobs.contains(boss.getUniqueId())) {
            bossLastInteraction.put(boss.getUniqueId(), System.currentTimeMillis()); // Atualiza tempo de atividade

            EntityDamageEvent.DamageCause cause = event.getCause();
            if (cause == EntityDamageEvent.DamageCause.FIRE ||
                    cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
                    cause == EntityDamageEvent.DamageCause.LAVA ||
                    cause == EntityDamageEvent.DamageCause.HOT_FLOOR) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBossCombust(EntityCombustEvent event) {
        if (bossMobs.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
