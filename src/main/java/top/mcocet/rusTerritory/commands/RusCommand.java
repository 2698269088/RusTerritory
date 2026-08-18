package top.mcocet.rusTerritory.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mcocet.rusTerritory.RusTerritory;
import top.mcocet.rusTerritory.listeners.SelectionListener;
import top.mcocet.rusTerritory.models.Territory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RusCommand implements CommandExecutor, TabCompleter {
    private final RusTerritory plugin;
    private final Map<UUID, String> deleteConfirm = new ConcurrentHashMap<>();

    public RusCommand(RusTerritory plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c此命令只能由玩家执行");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "ade" -> handleAde(player);
            case "create" -> handleCreate(player, args);
            case "delete" -> handleDelete(player, args);
            case "confirm" -> handleConfirm(player);
            case "list" -> handleList(player);
            case "tp" -> handleTp(player, args);
            case "info" -> handleInfo(player, args);
            case "set" -> handleSet(player, args);
            case "rename" -> handleRename(player, args);
            case "transfer" -> handleTransfer(player, args);
            case "help" -> sendHelp(player);
            default -> player.sendMessage("§c未知子命令，使用 /rus help 查看帮助");
        }
        return true;
    }

    private boolean canCreate(Player player) {
        return player.hasPermission("rusterry.admin") || plugin.getConfigManager().isPlayerCanCreate();
    }

    private boolean canDelete(Player player) {
        return player.hasPermission("rusterry.admin") || plugin.getConfigManager().isPlayerCanDelete();
    }

    private boolean isAdmin(Player player) {
        return player.hasPermission("rusterry.admin");
    }

    private SelectionListener getSelectionListener() {
        return plugin.getSelectionListener();
    }

    private boolean isValidName(String name) {
        if (name == null || name.isEmpty() || name.length() > 32) return false;
        return name.matches("^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$");
    }

    private void handleAde(Player player) {
        if (!canCreate(player)) {
            player.sendMessage("§c你没有权限创建领地");
            return;
        }
        SelectionListener listener = getSelectionListener();
        if (listener.isInSelectionMode(player)) {
            listener.setSelectionMode(player, false);
        } else {
            listener.setSelectionMode(player, true);
        }
    }

    private void handleCreate(Player player, String[] args) {
        if (!canCreate(player)) {
            player.sendMessage("§c你没有权限创建领地");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§c用法: /rus create <领地名称>");
            return;
        }

        String name = args[1];
        if (!isValidName(name)) {
            player.sendMessage("§c领地名称不合法 (只能包含字母、数字、下划线和中文，最长32字符)");
            return;
        }

        SelectionListener listener = getSelectionListener();
        Location pos1 = listener.getPos1(player);
        Location pos2 = listener.getPos2(player);

        if (pos1 == null || pos2 == null) {
            player.sendMessage("§c请先使用 /rus ade 进入选区模式，手持选区工具蹲下右键点击两个方块来设定领地范围");
            return;
        }
        if (!pos1.getWorld().equals(pos2.getWorld())) {
            player.sendMessage("§c两个点必须在同一世界");
            return;
        }
        if (plugin.getStorage().nameExists(name)) {
            player.sendMessage("§c领地名称已存在");
            return;
        }

        int maxSize = plugin.getConfigManager().getMaxSize();
        long volume = (long) (Math.abs(pos1.getBlockX() - pos2.getBlockX()) + 1)
                * (Math.abs(pos1.getBlockY() - pos2.getBlockY()) + 1)
                * (Math.abs(pos1.getBlockZ() - pos2.getBlockZ()) + 1);
        if (!isAdmin(player) && maxSize > 0 && volume > maxSize) {
            player.sendMessage("§c领地体积超出限制 (最大: " + maxSize + " 方块, 当前: " + volume + " 方块)");
            return;
        }

        int maxPerPlayer = plugin.getConfigManager().getMaxPerPlayer();
        if (!isAdmin(player) && maxPerPlayer > 0) {
            int count = plugin.getStorage().getTerritoryCount(player.getUniqueId());
            if (count >= maxPerPlayer) {
                player.sendMessage("§c你已达到领地数量上限 (最多 " + maxPerPlayer + " 个)");
                return;
            }
        }

        Territory territory = new Territory(
                name,
                pos1.getWorld().getName(),
                pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(),
                pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ(),
                player.getUniqueId()
        );

        if (!isAdmin(player) && plugin.getStorage().isOverlapping(territory)) {
            player.sendMessage("§c该区域与其他领地重叠，无法创建");
            return;
        }

        plugin.getStorage().addTerritory(territory);
        listener.clearSelection(player);
        player.sendMessage("§a成功创建领地 §f" + name + " §a(ID: §f" + territory.getId() + "§a)");
        player.sendMessage("§a体积: §f" + territory.getVolume() + " §a方块");
    }

    private void handleDelete(Player player, String[] args) {
        if (!canDelete(player)) {
            player.sendMessage("§c你没有权限删除领地");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§c用法: /rus delete <领地名称>");
            return;
        }
        Territory territory = plugin.getStorage().getTerritoryByName(args[1]);
        if (territory == null) {
            player.sendMessage("§c未找到该领地");
            return;
        }
        if (!isAdmin(player) && !territory.getOwner().equals(player.getUniqueId())) {
            player.sendMessage("§c你只能删除自己的领地");
            return;
        }
        deleteConfirm.put(player.getUniqueId(), territory.getId());
        player.sendMessage("§e你确定要删除领地 §f" + territory.getName() + " §e吗？");
        player.sendMessage("§e输入 §f/rus confirm §e确认删除，或等待30秒自动取消");
    }

    private void handleConfirm(Player player) {
        String territoryId = deleteConfirm.remove(player.getUniqueId());
        if (territoryId == null) {
            player.sendMessage("§c没有待确认的删除操作");
            return;
        }
        Territory territory = plugin.getStorage().getTerritory(territoryId);
        if (territory == null) {
            player.sendMessage("§c领地不存在");
            return;
        }
        plugin.getStorage().removeTerritory(territoryId);
        player.sendMessage("§a已删除领地 §f" + territory.getName());
    }

    private void handleList(Player player) {
        List<Territory> list;
        if (isAdmin(player)) {
            list = new ArrayList<>(plugin.getStorage().getAllTerritories());
        } else {
            list = plugin.getStorage().getTerritoriesByOwner(player.getUniqueId());
        }
        if (list.isEmpty()) {
            player.sendMessage("§e当前没有任何领地");
            return;
        }
        player.sendMessage("§6========== 我的领地列表 ==========");
        int i = 1;
        for (Territory t : list) {
            player.sendMessage("§a" + (i++) + ". §f" + t.getName() + " §7(ID: " + t.getId() + ", 世界: " + t.getWorldName() + ")");
        }
        player.sendMessage("§6=================================");
    }

    private void handleTp(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c用法: /rus tp <领地名称>");
            return;
        }
        Territory territory = plugin.getStorage().getTerritoryByName(args[1]);
        if (territory == null) {
            player.sendMessage("§c未找到该领地");
            return;
        }
        if (!isAdmin(player) && !territory.getOwner().equals(player.getUniqueId())) {
            player.sendMessage("§c你只能传送到自己的领地");
            return;
        }
        World world = Bukkit.getWorld(territory.getWorldName());
        if (world == null) {
            player.sendMessage("§c目标世界不存在");
            return;
        }
        Location center = territory.getCenter(world);
        player.sendMessage("§a正在传送至领地 §f" + territory.getName() + "§a...");
        player.teleportAsync(center).thenAccept(success -> {
            if (success) {
                player.sendMessage("§a已传送至领地 §f" + territory.getName());
            } else {
                player.sendMessage("§c传送失败");
            }
        });
    }

    private void handleInfo(Player player, String[] args) {
        Territory territory;
        if (args.length >= 2) {
            territory = plugin.getStorage().getTerritoryByName(args[1]);
            if (territory != null && !isAdmin(player) && !territory.getOwner().equals(player.getUniqueId())) {
                player.sendMessage("§c你只能查看自己的领地信息");
                return;
            }
        } else {
            territory = plugin.getStorage().getTerritoryAt(player.getLocation());
        }
        if (territory == null) {
            player.sendMessage(args.length >= 2 ? "§c未找到该领地" : "§c你当前不在任何领地内");
            return;
        }
        player.sendMessage("§6========== 领地信息 ==========");
        player.sendMessage("§a名称: §f" + territory.getName());
        player.sendMessage("§aID: §f" + territory.getId());
        player.sendMessage("§a世界: §f" + territory.getWorldName());
        player.sendMessage("§a范围: §f(" + territory.getMinX() + ", " + territory.getMinY() + ", " + territory.getMinZ() + ") §a到 §f(" + territory.getMaxX() + ", " + territory.getMaxY() + ", " + territory.getMaxZ() + ")");
        player.sendMessage("§a体积: §f" + territory.getVolume() + " §a方块");
        player.sendMessage("§aPVP: §f" + (territory.isPvp() ? "§c允许" : "§a禁止"));
        player.sendMessage("§a建造: §f" + (territory.isBuild() ? "§a允许" : "§c禁止"));
        player.sendMessage("§a破坏: §f" + (territory.isDestroy() ? "§a允许" : "§c禁止"));
        player.sendMessage("§a交互: §f" + (territory.isInteract() ? "§a允许" : "§c禁止"));
        player.sendMessage("§a进入: §f" + (territory.isEnter() ? "§a允许" : "§c禁止"));
        player.sendMessage("§6==============================");
    }

    private void handleSet(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§c用法: /rus set <领地名称> <pvp|build|destroy|interact|enter> <true|false>");
            return;
        }
        Territory territory = plugin.getStorage().getTerritoryByName(args[1]);
        if (territory == null) {
            player.sendMessage("§c未找到该领地");
            return;
        }
        if (!isAdmin(player) && !territory.getOwner().equals(player.getUniqueId())) {
            player.sendMessage("§c你只能设置自己的领地属性");
            return;
        }
        String property = args[2].toLowerCase();
        boolean value = Boolean.parseBoolean(args[3]);
        switch (property) {
            case "pvp" -> territory.setPvp(value);
            case "build" -> territory.setBuild(value);
            case "destroy" -> territory.setDestroy(value);
            case "interact" -> territory.setInteract(value);
            case "enter" -> territory.setEnter(value);
            default -> {
                player.sendMessage("§c未知属性，可用: pvp, build, destroy, interact, enter");
                return;
            }
        }
        plugin.getStorage().addTerritory(territory);
        player.sendMessage("§a已设置 §f" + territory.getName() + " §a的 §f" + property + " §a为 §f" + value);
    }

    private void handleRename(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§c用法: /rus rename <旧名称> <新名称>");
            return;
        }
        Territory territory = plugin.getStorage().getTerritoryByName(args[1]);
        if (territory == null) {
            player.sendMessage("§c未找到该领地");
            return;
        }
        if (!isAdmin(player) && !territory.getOwner().equals(player.getUniqueId())) {
            player.sendMessage("§c你只能重命名自己的领地");
            return;
        }
        String newName = args[2];
        if (!isValidName(newName)) {
            player.sendMessage("§c领地名称不合法 (只能包含字母、数字、下划线和中文，最长32字符)");
            return;
        }
        if (plugin.getStorage().nameExists(newName)) {
            player.sendMessage("§c该名称已被使用");
            return;
        }
        String oldName = territory.getName();
        territory.setName(newName);
        plugin.getStorage().addTerritory(territory);
        player.sendMessage("§a已将领地 §f" + oldName + " §a重命名为 §f" + newName);
    }

    private void handleTransfer(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§c用法: /rus transfer <领地名称> <玩家>");
            return;
        }
        Territory territory = plugin.getStorage().getTerritoryByName(args[1]);
        if (territory == null) {
            player.sendMessage("§c未找到该领地");
            return;
        }
        if (!isAdmin(player) && !territory.getOwner().equals(player.getUniqueId())) {
            player.sendMessage("§c你只能转让自己的领地");
            return;
        }
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            player.sendMessage("§c目标玩家不在线");
            return;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§c不能将领地转让给自己");
            return;
        }
        UUID oldOwner = territory.getOwner();
        territory.setOwner(target.getUniqueId());
        plugin.getStorage().addTerritory(territory);
        player.sendMessage("§a已将领地 §f" + territory.getName() + " §a转让给 §f" + target.getName());
        target.sendMessage("§a你已获得领地 §f" + territory.getName() + " §a的所有权");
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6========== RusTerritory 玩家帮助 ==========");
        player.sendMessage("§a/rus ade §7- 进入/退出选区模式");
        player.sendMessage("§a/rus create <名称> §7- 使用选区创建领地");
        player.sendMessage("§a/rus delete <名称> §7- 删除自己的领地");
        player.sendMessage("§a/rus confirm §7- 确认删除领地");
        player.sendMessage("§a/rus list §7- 列出自己的领地");
        player.sendMessage("§a/rus tp <名称> §7- 传送至自己的领地中心");
        player.sendMessage("§a/rus info [名称] §7- 查看领地信息");
        player.sendMessage("§a/rus set <名称> <属性> <true|false> §7- 设置领地属性");
        player.sendMessage("§a/rus rename <旧名称> <新名称> §7- 重命名领地");
        player.sendMessage("§a/rus transfer <名称> <玩家> §7- 转让领地");
        player.sendMessage("§a/rus help §7- 显示此帮助");
        player.sendMessage("§6==========================================");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return List.of();
        if (args.length == 1) {
            return List.of("ade", "create", "delete", "confirm", "list", "tp", "info", "set", "rename", "transfer", "help")
                    .stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2 && List.of("delete", "tp", "info", "set", "rename", "transfer").contains(args[0].toLowerCase())) {
            List<Territory> list = isAdmin(player)
                    ? new ArrayList<>(plugin.getStorage().getAllTerritories())
                    : plugin.getStorage().getTerritoriesByOwner(player.getUniqueId());
            return list.stream()
                    .map(Territory::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            return List.of("pvp", "build", "destroy", "interact", "enter")
                    .stream().filter(s -> s.startsWith(args[2].toLowerCase())).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("transfer")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[2].toLowerCase()))
                    .toList();
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("set")) {
            return List.of("true", "false")
                    .stream().filter(s -> s.startsWith(args[3].toLowerCase())).toList();
        }
        return List.of();
    }
}
