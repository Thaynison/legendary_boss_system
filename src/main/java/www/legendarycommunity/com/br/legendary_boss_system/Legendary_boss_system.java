package www.legendarycommunity.com.br.legendary_boss_system;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public final class Legendary_boss_system extends JavaPlugin {

    // Armazena o tempo do último ataque aos mobs
    private final Map<Mob, Long> lastAttackedTime = new HashMap<>();

    @Override
    public void onEnable() {
        // Salva o config.yml padrão, se não existir
        saveDefaultConfig();
        getLogger().info("Legendary Boss System iniciado!");
        spawnBossesFromConfig();
        spawnRaidFromConfig();
        // Agendamos a verificação dos mobs
        startMobCleanupTask();
    }

    @Override
    public void onDisable() {
        getLogger().info("Legendary Boss System desativado!");
    }

    private void spawnBossesFromConfig() {
        FileConfiguration config = getConfig();
        Map<String, Object> bosses = config.getConfigurationSection("Boses").getValues(false);

        for (String bossKey : bosses.keySet()) {
            Map<String, Object> bossConfig = config.getConfigurationSection("Boses." + bossKey).getValues(false);

            String worldName = (String) bossConfig.get("mundo");
            int quantidade = Integer.parseInt((String) bossConfig.get("quantidade"));
            EntityType mobType = EntityType.valueOf((String) bossConfig.get("mob"));
            double vida = Double.parseDouble((String) bossConfig.get("vida"));
            double forca = Double.parseDouble((String) bossConfig.get("forca"));
            String nameTag = (String) bossConfig.get("nameTag");
            int timeSpawnMinutes = Integer.parseInt((String) bossConfig.get("timespawn"));

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                getLogger().warning("O mundo '" + worldName + "' não foi encontrado. Ignorando o boss: " + bossKey);
                continue;
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    world.getPlayers().forEach(player -> {
                        for (int i = 0; i < quantidade; i++) {
                            Location spawnLocation = getNearbyLocation(player.getLocation(), 20);

                            // Spawna o mob próximo ao jogador
                            Mob mob = (Mob) world.spawnEntity(spawnLocation, mobType);
                            mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(vida);
                            mob.setHealth(vida);
                            mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(forca);
                            mob.setCustomName(nameTag);
                            mob.setCustomNameVisible(true);

                            lastAttackedTime.put(mob, System.currentTimeMillis());
                        }
                    });
                }
            }.runTaskTimer(this, 0, timeSpawnMinutes * 60 * 20L); // Converte minutos para ticks
        }
    }

    private Location getNearbyLocation(Location playerLocation, int radius) {
        double xOffset = (Math.random() * radius * 2) - radius;
        double zOffset = (Math.random() * radius * 2) - radius;
        Location randomLocation = playerLocation.clone().add(xOffset, 0, zOffset);
        randomLocation.setY(playerLocation.getWorld().getHighestBlockYAt(randomLocation));
        return randomLocation;
    }

    private void startMobCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                // Collect mobs to remove
                List<Mob> mobsToRemove = new ArrayList<>();

                for (Mob mob : lastAttackedTime.keySet()) {
                    if (mob.isDead() || !mob.isValid()) {
                        mobsToRemove.add(mob);
                        continue;
                    }

                    boolean tooFar = Bukkit.getOnlinePlayers().stream()
                            .noneMatch(player -> player.getWorld().equals(mob.getWorld()) &&
                                    player.getLocation().distance(mob.getLocation()) <= 30);

                    boolean noRecentAttack = currentTime - lastAttackedTime.get(mob) > 60 * 1000;

                    if (tooFar || noRecentAttack) {
                        mob.remove();
                        mobsToRemove.add(mob);
                    }
                }

                mobsToRemove.forEach(lastAttackedTime::remove);
            }
        }.runTaskTimer(this, 20, 20);
    }

    private void spawnRaidFromConfig() {
        FileConfiguration config = getConfig();
        Map<String, Object> raids = config.getConfigurationSection("Raid").getValues(false);

        for (String raidKey : raids.keySet()) {
            Map<String, Object> raidConfig = config.getConfigurationSection("Raid." + raidKey).getValues(false);

            String worldName = (String) raidConfig.get("mundo");
            EntityType mobType = EntityType.valueOf((String) raidConfig.get("mob"));
            double vida = Double.parseDouble((String) raidConfig.get("vida"));
            double forca = Double.parseDouble((String) raidConfig.get("forca"));
            int timeSpawnMinutes = Integer.parseInt((String) raidConfig.get("timespawn"));

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                getLogger().warning("O mundo '" + worldName + "' não foi encontrado. Ignorando o raid: " + raidKey);
                continue;
            }

            List<String> nameTags = config.getStringList("Raid." + raidKey + ".nameTag");
            if (nameTags.isEmpty()) {
                getLogger().warning("Nenhuma nameTag foi encontrada para o raid: " + raidKey);
                continue;
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    world.getPlayers().forEach(player -> {
                        int tagIndex = 0;
                        boolean itemAssigned = false;

                        while (tagIndex < nameTags.size()) {
                            Location spawnLocation = getNearbyLocation(player.getLocation(), 20);

                            Mob mob = (Mob) world.spawnEntity(spawnLocation, mobType);
                            mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(vida);
                            mob.setHealth(vida);
                            mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(forca);

                            String nameTag = nameTags.get(tagIndex);
                            mob.setCustomName(nameTag);
                            mob.setCustomNameVisible(true);

                            if (!itemAssigned) {
                                ItemStack item1 = new ItemStack(Material.CARVED_PUMPKIN);
                                ItemMeta meta1 = item1.getItemMeta();
                                meta1.setDisplayName("§6Chapeu da Akatsuki");
                                List<String> lore1 = new ArrayList<>();
                                lore1.add("§7- §fInformação da §f[§6Relíquia§f].");
                                lore1.add("");
                                lore1.add("§aDescrição:");
                                lore1.add("§a❙ §7Recompensa por matar os boses da Akatsuki.");
                                lore1.add("");
                                lore1.add("§aEconomia:");
                                lore1.add("§a❙ §4Proibido a comercialização");
                                lore1.add("");
                                lore1.add("§a(!) Esse §dITEM §aé §6raro §amais informações acesse nosso site!");
                                meta1.setLore(lore1);
                                item1.setItemMeta(meta1);

                                mob.getWorld().dropItem(mob.getLocation(), item1);
                                itemAssigned = true; // Marca como já atribuído
                            }
                            tagIndex++;
                        }
                    });
                }
            }.runTaskTimer(this, 0, timeSpawnMinutes * 60 * 20L);

        }
    }



}
