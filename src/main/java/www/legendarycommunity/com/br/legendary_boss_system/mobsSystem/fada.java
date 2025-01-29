package www.legendarycommunity.com.br.legendary_boss_system.mobsSystem;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class fada {

    private final JavaPlugin plugin;

    public fada(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void onBlockBreak(Player player, Material brokenBlock) {
        // Verifica se o bloco quebrado é um tipo de madeira
        if (isWood(brokenBlock)) {
            Random random = new Random();
            if (random.nextInt(100) < 2) { // 10% de chance
                spawnFada(player);
            }
        }
    }

    private boolean isWood(Material material) {
        // Lista de tipos de madeira
        return material == Material.OAK_LOG ||
                material == Material.SPRUCE_LOG ||
                material == Material.BIRCH_LOG ||
                material == Material.JUNGLE_LOG ||
                material == Material.ACACIA_LOG ||
                material == Material.DARK_OAK_LOG;
    }

    private void spawnFada(Player player) {
        FileConfiguration config = plugin.getConfig();
        String mobType = config.getString("MobsSystem.Fada1.mob", "ZOMBIE");
        double health = config.getDouble("MobsSystem.Fada1.vida", 50);
        double damage = config.getDouble("MobsSystem.Fada1.forca", 2);
        String nameTag = config.getString("MobsSystem.Fada1.nameTag", "fada");

        // Remove os colchetes, se presentes
        nameTag = nameTag.replace("[", "").replace("]", "");

        // Obtém a localização do jogador
        Location playerLocation = player.getLocation();
        World world = playerLocation.getWorld();

        if (world == null) return;

        // Gera uma posição aleatória em um raio de 5 blocos ao redor do jogador
        Random random = new Random();
        double xOffset = random.nextDouble() * 10 - 5; // Gera um valor entre -5 e 5
        double zOffset = random.nextDouble() * 10 - 5; // Gera um valor entre -5 e 5
        Location spawnLocation = playerLocation.clone().add(xOffset, 0, zOffset);

        // Ajusta a localização para o bloco mais alto no local gerado
        spawnLocation.setY(world.getHighestBlockYAt(spawnLocation));

        // Spawna a fada na nova localização
        LivingEntity fada = (LivingEntity) world.spawnEntity(spawnLocation, EntityType.valueOf(mobType.toUpperCase()));
        fada.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        fada.setHealth(health);
        fada.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(damage);
        fada.setCustomName(nameTag);
        fada.setCustomNameVisible(true);

        // Garantir que o mob não tenha armadura ou armas
        if (fada instanceof LivingEntity) {
            LivingEntity livingFada = (LivingEntity) fada;
            if (livingFada instanceof Mob) {
                Mob mobFada = (Mob) livingFada;
                mobFada.getEquipment().setHelmet(null);  // Remove o capacete
                mobFada.getEquipment().setChestplate(null);  // Remove a armadura do torso
                mobFada.getEquipment().setLeggings(null);  // Remove a calça
                mobFada.getEquipment().setBoots(null);  // Remove as botas
                mobFada.getEquipment().setItemInMainHand(null);  // Remove a arma principal
                mobFada.getEquipment().setItemInOffHand(null);  // Remove a arma secundária
            }
        }

        // Adiciona comportamento para atacar o jogador
        if (fada instanceof Mob) {
            Mob mobFada = (Mob) fada;
            Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (mobFada.isValid() && player.isOnline()) {
                    double distance = mobFada.getLocation().distance(player.getLocation());
                    if (distance <= 5) {
                        mobFada.setTarget(player);
                    }
                }
            }, 0L, 20L); // Executa a cada segundo
        }
    }
}
