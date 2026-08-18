package top.mcocet.rusTerritory.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import top.mcocet.rusTerritory.RusTerritory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SelectionListener implements Listener {
    private final RusTerritory plugin;
    private final Map<UUID, Location> pos1Map = new ConcurrentHashMap<>();
    private final Map<UUID, Location> pos2Map = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> selectionMode = new ConcurrentHashMap<>();

    public SelectionListener(RusTerritory plugin) {
        this.plugin = plugin;
    }

    public void setSelectionMode(Player player, boolean enabled) {
        if (enabled) {
            selectionMode.put(player.getUniqueId(), true);
            pos1Map.remove(player.getUniqueId());
            pos2Map.remove(player.getUniqueId());
            String toolName = plugin.getConfigManager().getSelectionTool().toLowerCase().replace("_", " ");
            player.sendMessage("§a已进入选区模式，请手持" + toolName + "并蹲下右键点击方块来设定坐标");
            player.sendMessage("§7提示: 第一次点击设定起始坐标，第二次点击设定结束坐标");
        } else {
            selectionMode.remove(player.getUniqueId());
            pos1Map.remove(player.getUniqueId());
            pos2Map.remove(player.getUniqueId());
            player.sendMessage("§e已退出选区模式");
        }
    }

    public boolean isInSelectionMode(Player player) {
        return selectionMode.getOrDefault(player.getUniqueId(), false);
    }

    public Location getPos1(Player player) {
        return pos1Map.get(player.getUniqueId());
    }

    public Location getPos2(Player player) {
        return pos2Map.get(player.getUniqueId());
    }

    public void clearSelection(Player player) {
        pos1Map.remove(player.getUniqueId());
        pos2Map.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isInSelectionMode(player)) return;
        if (!player.isSneaking()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Material tool = Material.valueOf(plugin.getConfigManager().getSelectionTool());
        if (player.getInventory().getItemInMainHand().getType() != tool) return;

        event.setCancelled(true);
        Location loc = event.getClickedBlock().getLocation();

        if (pos1Map.get(player.getUniqueId()) == null) {
            pos1Map.put(player.getUniqueId(), loc);
            player.sendMessage("§a已设定起始坐标: §f" + formatLoc(loc));
            player.sendMessage("§7请继续蹲下右键点击另一个方块设定结束坐标");
        } else if (pos2Map.get(player.getUniqueId()) == null) {
            Location pos1 = pos1Map.get(player.getUniqueId());
            if (!pos1.getWorld().equals(loc.getWorld())) {
                player.sendMessage("§c两个坐标必须在同一世界");
                return;
            }
            pos2Map.put(player.getUniqueId(), loc);
            player.sendMessage("§a已设定结束坐标: §f" + formatLoc(loc));

            long dx = Math.abs(pos1.getBlockX() - loc.getBlockX()) + 1;
            long dy = Math.abs(pos1.getBlockY() - loc.getBlockY()) + 1;
            long dz = Math.abs(pos1.getBlockZ() - loc.getBlockZ()) + 1;
            long volume = dx * dy * dz;
            player.sendMessage("§a选区体积: §f" + volume + " §a方块");

            int maxSize = plugin.getConfigManager().getMaxSize();
            if (!player.hasPermission("rusterry.admin") && maxSize > 0 && volume > maxSize) {
                player.sendMessage("§c警告: 选区体积超出限制 (最大: " + maxSize + " 方块)");
            }

            player.sendMessage("§a选区完成！请使用 §f/rus create <名称>§a创建领地");
            selectionMode.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        selectionMode.remove(uuid);
        pos1Map.remove(uuid);
        pos2Map.remove(uuid);
    }

    private String formatLoc(Location loc) {
        return String.format("[%s, %d, %d, %d]", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
