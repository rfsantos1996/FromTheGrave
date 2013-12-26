package com.jabyftw.ftg;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 *
 * @author Rafael
 */
public class FromTheGrave extends JavaPlugin implements Listener {

    private Team team;
    private Scoreboard sb;
    Map<Player, Integer> ghosts = new HashMap();
    private int respawnTime;
    private boolean teleportOnRespawn;
    private Location respawn;

    @Override
    public void onEnable() {
        sb = getServer().getScoreboardManager().getNewScoreboard();
        team = sb.registerNewTeam("team");
        team.setCanSeeFriendlyInvisibles(true);
        team.setAllowFriendlyFire(true);
        getLogger().log(Level.INFO, "Registered invisible team.");
        FileConfiguration config = getConfig();
        config.addDefault("config.respawnTimeInMinutes", 3);
        config.addDefault("config.teleportOnRespawn", true);
        config.addDefault("config.respawnLocation", "world;5;64;2");
        config.addDefault("config.youDied", "&4You died. &cWait %time minutes until the respawn.");
        config.options().copyDefaults(true);
        saveConfig();
        respawnTime = config.getInt("config.respawnTimeInMinutes");
        teleportOnRespawn = config.getBoolean("config.teleportOnRespawn");
        respawn = getLocationFromString(config.getString("config.respawnLocation"));
        getLogger().log(Level.INFO, "Loaded configuration.");
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().log(Level.INFO, "Registered events.");
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "Disabled.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onDeath(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player damager = (Player) e.getDamager();
            if (ghosts.containsKey(damager)) {
                e.setCancelled(true);
                return;
            }
        } else if (e.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) e.getDamager();
            if (proj.getShooter() instanceof Player) {
                Player damager = (Player) proj.getShooter();
                if (ghosts.containsKey(damager)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (ghosts.containsKey(p)) {
                e.setCancelled(true);
                return;
            }
            if (p.getHealth() - e.getDamage() < 1) {
                setGhost(p);
                p.setHealth(p.getMaxHealth());
                for (ItemStack is : p.getInventory().getContents()) {
                    if (is != null) {
                        if (!is.getType().equals(Material.AIR)) {
                            p.getLocation().getWorld().dropItemNaturally(p.getLocation(), is);
                        }
                    }
                }
                for (ItemStack is : p.getInventory().getArmorContents()) {
                    if (is != null) {
                        if (!is.getType().equals(Material.AIR)) {
                            p.getLocation().getWorld().dropItemNaturally(p.getLocation(), is);
                        }
                    }
                }
                p.getInventory().clear();
                p.setAllowFlight(true);
                p.teleport(p.getLocation().add(0, 2, 0));
                p.setFlying(true);
                p.sendMessage(getConfig().getString("config.youDied").replaceAll("&", "§").replaceAll("%time", Integer.toString(respawnTime)));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (ghosts.containsKey(p)) {
                e.setCancelled(true);
                return;
            }
            if (p.getHealth() - e.getDamage() < 1) {
                setGhost(p);
                p.setHealth(p.getMaxHealth());
                for (ItemStack is : p.getInventory().getContents()) {
                    if (is != null) {
                        if (!is.getType().equals(Material.AIR)) {
                            p.getLocation().getWorld().dropItemNaturally(p.getLocation(), is);
                        }
                    }
                }
                for (ItemStack is : p.getInventory().getArmorContents()) {
                    if (is != null) {
                        if (!is.getType().equals(Material.AIR)) {
                            p.getLocation().getWorld().dropItemNaturally(p.getLocation(), is);
                        }
                    }
                }
                p.getInventory().clear();
                p.setAllowFlight(true);
                p.teleport(p.getLocation().add(0, 2, 0));
                p.setFlying(true);
                p.sendMessage(getConfig().getString("config.youDied").replaceAll("&", "§").replaceAll("%time", Integer.toString(respawnTime)));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickUp(InventoryPickupItemEvent e) {
        if (e.getInventory().getHolder() instanceof Player) {
            Player p = (Player) e.getInventory().getHolder();
            if (ghosts.containsKey(p)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof Player) {
            Player p = (Player) e.getInventory().getHolder();
            if (ghosts.containsKey(p)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (ghosts.containsKey(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        team.addPlayer(p);
        p.setScoreboard(sb);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        team.removePlayer(e.getPlayer());
        if (ghosts.containsKey(e.getPlayer())) {
            respawn(e.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (ghosts.containsKey(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (ghosts.containsKey(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        team.removePlayer(e.getPlayer());
        if (ghosts.containsKey(e.getPlayer())) {
            respawn(e.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetEvent e) {
        if (e.getTarget() instanceof Player) {
            Player p = (Player) e.getTarget();
            if (ghosts.containsKey(p)) {
                e.setCancelled(true);
            }
        }
    }

    private void setGhost(Player p) {
        ghosts.put(p, getServer().getScheduler().scheduleSyncDelayedTask(this, new RespawnRunnable(p), (respawnTime * 60) * 20));
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));
    }

    private Location getLocationFromString(String s) {
        String[] st = s.split(";");
        return new Location(getServer().getWorld(st[0]), Integer.parseInt(st[1]), Integer.parseInt(st[2]), Integer.parseInt(st[3]));
    }

    private void respawn(Player p) {
        ghosts.remove(p);
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
        if (teleportOnRespawn) {
            p.teleport(respawn);
        }
        p.setLevel(0);
        p.setExp(0);
        p.getInventory().clear();
        p.getEquipment().setArmorContents(null);
        p.setMaxHealth(20);
        p.setHealth(p.getMaxHealth());
        p.setFoodLevel(20);
        p.setExhaustion(0);
        p.setFlying(false); // make them fall
        final Player p2 = p;
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

            @Override
            public void run() {
                p2.setAllowFlight(false);
            }
        }, 20 * 5);
    }

    private class RespawnRunnable extends BukkitRunnable {

        private final Player p;

        public RespawnRunnable(Player p) {
            this.p = p;
        }

        @Override
        public void run() {
            respawn(p);
        }
    }
}
