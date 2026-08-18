package top.mcocet.rusTerritory.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import top.mcocet.rusTerritory.RusTerritory;
import top.mcocet.rusTerritory.models.Territory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TerritoryListener implements Listener {
    private final RusTerritory plugin;
    private final Map<UUID, Territory> playerTerritory = new HashMap<>();

    public TerritoryListener(RusTerritory plugin) {
        this.plugin = plugin;
    }

    private boolean isAdmin(Player player) {
        return player.hasPermission("rusterry.admin");
    }

    private Territory getTerritory(Location location) {
        return plugin.getStorage().getTerritoryAt(location);
    }

    private void sendDeny(Player player, String msg) {
        player.sendActionBar(net.kyori.adventure.text.Component.text(msg));
    }

    private boolean canBypass(Player player, Territory territory) {
        return isAdmin(player) || territory.getOwner().equals(player.getUniqueId());
    }

    // ========== 方块保护 ==========

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        Player player = event.getPlayer();
        if (isAdmin(player)) return;
        Territory territory = getTerritory(event.getBlock().getLocation());
        if (territory != null && !territory.isDestroy() && !canBypass(player, territory)) {
            event.setCancelled(true);
            sendDeny(player, plugin.getConfigManager().getMsgDenyDestroy());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getConfigManager().isProtectBuild()) return;
        Player player = event.getPlayer();
        if (isAdmin(player)) return;
        Territory territory = getTerritory(event.getBlock().getLocation());
        if (territory != null && !territory.isBuild() && !canBypass(player, territory)) {
            event.setCancelled(true);
            sendDeny(player, plugin.getConfigManager().getMsgDenyBuild());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        Territory territory = getTerritory(event.getBlock().getLocation());
        if (territory != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        Territory territory = getTerritory(event.getBlock().getLocation());
        if (territory != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        Territory territory = getTerritory(event.getBlock().getLocation());
        if (territory != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        Territory territory = getTerritory(event.getBlock().getLocation());
        if (territory != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        Territory territory = getTerritory(event.getBlock().getLocation());
        if (territory != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        Territory territory = getTerritory(event.getBlock().getLocation());
        if (territory != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        Territory territory = getTerritory(event.getToBlock().getLocation());
        if (territory != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        for (Block block : event.getBlocks()) {
            Territory territory = getTerritory(block.getLocation());
            if (territory != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        for (Block block : event.getBlocks()) {
            Territory territory = getTerritory(block.getLocation());
            if (territory != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // ========== 爆炸保护 ==========

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        List<Block> blocks = event.blockList();
        List<Block> toRemove = new ArrayList<>();
        for (Block block : blocks) {
            Territory territory = getTerritory(block.getLocation());
            if (territory != null) {
                toRemove.add(block);
            }
        }
        blocks.removeAll(toRemove);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        List<Block> blocks = event.blockList();
        List<Block> toRemove = new ArrayList<>();
        for (Block block : blocks) {
            Territory territory = getTerritory(block.getLocation());
            if (territory != null) {
                toRemove.add(block);
            }
        }
        blocks.removeAll(toRemove);
    }

    // ========== 实体保护 ==========

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfigManager().isProtectPvp()) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (isAdmin(attacker)) return;

        Territory territory = getTerritory(victim.getLocation());
        if (territory != null && !territory.isPvp() && !canBypass(attacker, territory)) {
            event.setCancelled(true);
            sendDeny(attacker, plugin.getConfigManager().getMsgDenyPvp());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByProjectile(EntityDamageByEntityEvent event) {
        if (!plugin.getConfigManager().isProtectPvp()) return;
        if (!(event.getDamager() instanceof Projectile projectile)) return;
        if (!(projectile.getShooter() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (isAdmin(attacker)) return;

        Territory territory = getTerritory(victim.getLocation());
        if (territory != null && !territory.isPvp() && !canBypass(attacker, territory)) {
            event.setCancelled(true);
            sendDeny(attacker, plugin.getConfigManager().getMsgDenyPvp());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        Territory territory = getTerritory(event.getBlock().getLocation());
        if (territory != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityBreakDoor(EntityBreakDoorEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        Territory territory = getTerritory(event.getBlock().getLocation());
        if (territory != null) {
            event.setCancelled(true);
        }
    }

    // ========== 交互保护 ==========

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getConfigManager().isProtectInteract()) return;
        Player player = event.getPlayer();
        if (isAdmin(player)) return;
        if (event.getClickedBlock() == null) return;
        Territory territory = getTerritory(event.getClickedBlock().getLocation());
        if (territory != null && !territory.isInteract() && !canBypass(player, territory)) {
            event.setCancelled(true);
            sendDeny(player, plugin.getConfigManager().getMsgDenyInteract());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!plugin.getConfigManager().isProtectBuild()) return;
        Player player = event.getPlayer();
        if (isAdmin(player)) return;
        Territory territory = getTerritory(event.getBlock().getLocation());
        if (territory != null && !territory.isBuild() && !canBypass(player, territory)) {
            event.setCancelled(true);
            sendDeny(player, plugin.getConfigManager().getMsgDenyBuild());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        Player player = event.getPlayer();
        if (isAdmin(player)) return;
        Territory territory = getTerritory(event.getBlock().getLocation());
        if (territory != null && !territory.isDestroy() && !canBypass(player, territory)) {
            event.setCancelled(true);
            sendDeny(player, plugin.getConfigManager().getMsgDenyDestroy());
        }
    }

    // ========== 悬挂实体保护 ==========

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (!plugin.getConfigManager().isProtectBuild()) return;
        Player player = event.getPlayer();
        if (player == null || isAdmin(player)) return;
        Territory territory = getTerritory(event.getBlock().getLocation());
        if (territory != null && !territory.isBuild() && !canBypass(player, territory)) {
            event.setCancelled(true);
            sendDeny(player, plugin.getConfigManager().getMsgDenyBuild());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        Territory territory = getTerritory(event.getEntity().getLocation());
        if (territory != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        Territory territory = getTerritory(event.getEntity().getLocation());
        if (territory != null) {
            if (event.getRemover() instanceof Player player) {
                if (!canBypass(player, territory)) {
                    event.setCancelled(true);
                    sendDeny(player, plugin.getConfigManager().getMsgDenyDestroy());
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    // ========== 载具保护 ==========

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!plugin.getConfigManager().isProtectInteract()) return;
        if (!(event.getEntered() instanceof Player player)) return;
        if (isAdmin(player)) return;
        Territory territory = getTerritory(event.getVehicle().getLocation());
        if (territory != null && !territory.isInteract() && !canBypass(player, territory)) {
            event.setCancelled(true);
            sendDeny(player, plugin.getConfigManager().getMsgDenyInteract());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        if (!(event.getAttacker() instanceof Player player)) return;
        if (isAdmin(player)) return;
        Territory territory = getTerritory(event.getVehicle().getLocation());
        if (territory != null && !territory.isDestroy() && !canBypass(player, territory)) {
            event.setCancelled(true);
            sendDeny(player, plugin.getConfigManager().getMsgDenyDestroy());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (!plugin.getConfigManager().isProtectDestroy()) return;
        Territory territory = getTerritory(event.getVehicle().getLocation());
        if (territory != null) {
            event.setCancelled(true);
        }
    }

    // ========== 玩家退出清理 ==========

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerTerritory.remove(event.getPlayer().getUniqueId());
    }

    // ========== 进入保护 ==========

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfigManager().isProtectEnter()) return;
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Territory fromTerr = getTerritory(from);
        Territory toTerr = getTerritory(to);
        UUID uuid = player.getUniqueId();
        Territory lastTerr = playerTerritory.get(uuid);

        if (toTerr != null && toTerr != lastTerr) {
            if (!toTerr.isEnter() && !canBypass(player, toTerr)) {
                event.setCancelled(true);
                sendDeny(player, plugin.getConfigManager().getMsgDenyEnter());
                return;
            }
            String ownerName = Bukkit.getOfflinePlayer(toTerr.getOwner()).getName();
            String msg = plugin.getConfigManager().getMsgEnter()
                    .replace("{player}", player.getName())
                    .replace("{owner}", ownerName != null ? ownerName : "未知")
                    .replace("{territory}", toTerr.getName());
            player.sendActionBar(net.kyori.adventure.text.Component.text(msg));
        }

        if (fromTerr != null && toTerr != fromTerr && lastTerr == fromTerr) {
            String ownerName = Bukkit.getOfflinePlayer(fromTerr.getOwner()).getName();
            String msg = plugin.getConfigManager().getMsgLeave()
                    .replace("{player}", player.getName())
                    .replace("{owner}", ownerName != null ? ownerName : "未知")
                    .replace("{territory}", fromTerr.getName());
            player.sendActionBar(net.kyori.adventure.text.Component.text(msg));
        }

        playerTerritory.put(uuid, toTerr);
    }
}
